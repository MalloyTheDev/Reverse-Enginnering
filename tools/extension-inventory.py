#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
extension-inventory.py — Static extension-surface scanner for the Ghidra fork.

Phase 1 tool. It performs a READ-ONLY static scan of the repository's source
files and produces an advisory inventory of likely Ghidra extension surfaces:
plugins, analyzers, loaders, exporters, component providers, extension points,
docking actions, GhidraScripts, processor/Sleigh definitions, and script trees.

Hard guarantees (by design):
  * Python 3, standard library ONLY. No third-party packages.
  * Does NOT execute Java, run Gradle, or compile anything.
  * Read-only EXCEPT for the report files it writes under
    fork/docs/generated/.
  * Heuristic. It matches text patterns; it does not parse Java. Treat every
    result as a *candidate*, not ground truth.

Usage:
    python3 tools/extension-inventory.py            # write the Markdown report
    python3 tools/extension-inventory.py --json     # also write the JSON report
    python3 tools/extension-inventory.py --root DIR # scan a different repo root

The Markdown report is written to:
    fork/docs/generated/EXTENSIONPOINT_INVENTORY.md
The optional JSON report is written to:
    fork/docs/generated/extensionpoint_inventory.json
"""

from __future__ import annotations

import argparse
import datetime
import json
import os
import re
import sys

# --------------------------------------------------------------------------
# Configuration
# --------------------------------------------------------------------------

# Directories pruned from the walk (build output / VCS / IDE metadata). Pruning
# these keeps the scan to actual source and keeps it fast and deterministic.
PRUNE_DIRS = {
    ".git", "build", "dist", ".gradle", "node_modules",
    ".idea", ".settings", "out",
}

# Source file extensions we open and read for text-pattern matching.
JAVA_EXT = (".java",)
SLEIGH_EXTS = {
    ".slaspec": "slaspec",
    ".sinc": "sinc",
    ".pspec": "pspec",
    ".cspec": "cspec",
}

# Directory names that conventionally hold GhidraScripts.
SCRIPT_DIR_NAMES = {"ghidra_scripts", "developer_scripts", "dev_scripts"}

# Top-level containers whose immediate subdirectories are "modules".
MODULE_CONTAINERS = [
    "Ghidra/Features",
    "Ghidra/Framework",
    "Ghidra/Processors",
    "Ghidra/Debug",
    "Ghidra/Extensions",
    "GPL",
    "GhidraBuild",
    "support",
]

# How many candidate files to list per category in the Markdown report.
TOP_N = 25

# --------------------------------------------------------------------------
# Java text-pattern categories
#
# Each category maps to a list of compiled regexes. A source file is counted
# once per category if ANY of that category's patterns match. These are
# heuristics grounded in real Ghidra conventions, NOT a Java parser.
# --------------------------------------------------------------------------

def _c(pattern: str) -> "re.Pattern[str]":
    return re.compile(pattern, re.MULTILINE)

JAVA_CATEGORIES = {
    # Tool plugins: subclass Plugin/ProgramPlugin, or carry the @PluginInfo
    # annotation that Ghidra plugins are required to declare.
    "plugins": [
        _c(r"\bclass\s+\w+\s+extends\s+(?:ProgramPlugin|Plugin)\b"),
        _c(r"@PluginInfo\b"),
    ],
    # Auto-analysis units: subclass AbstractAnalyzer or implement Analyzer.
    # (Convention: such class names also end in "Analyzer".)
    "analyzers": [
        _c(r"\bclass\s+\w+\s+extends\s+(?:AbstractAnalyzer|AbstractBinaryFormatAnalyzer)\b"),
        _c(r"\bimplements\s+[^={]*\bAnalyzer\b"),
    ],
    # Importers / loaders.
    "loaders": [
        _c(r"\bclass\s+\w+\s+extends\s+(?:AbstractLibrarySupportLoader|AbstractProgramLoader|AbstractOrdinalSupportLoader)\b"),
        _c(r"\bimplements\s+[^={]*\bLoader\b"),
    ],
    # Exporters.
    "exporters": [
        _c(r"\bclass\s+\w+\s+extends\s+(?:Exporter|AbstractExporter)\b"),
    ],
    # UI docking windows.
    "component_providers": [
        _c(r"\bclass\s+\w+\s+extends\s+(?:ComponentProvider|ComponentProviderAdapter|DialogComponentProvider)\b"),
    ],
    # Discoverable extension points (the ClassSearcher mechanism).
    "extension_points": [
        _c(r"\bimplements\s+[^={]*\bExtensionPoint\b"),
        _c(r"\binterface\s+\w+\s+extends\s+[^={]*\bExtensionPoint\b"),
    ],
    # GhidraScripts written in Java.
    "ghidra_scripts_java": [
        _c(r"\bclass\s+\w+\s+extends\s+GhidraScript\b"),
    ],
    # Docking actions (menu/toolbar/context actions).
    "docking_actions": [
        _c(r"\bclass\s+\w+\s+extends\s+(?:DockingAction|ListingContextAction|DockingContextAction|ProgramContextAction)\b"),
        _c(r"\bnew\s+DockingAction\b"),
    ],
}

# Pattern to extract a file's primary type name for display.
PRIMARY_TYPE_RE = re.compile(
    r"\b(?:public\s+|final\s+|abstract\s+|sealed\s+)*"
    r"(?:class|interface|enum|record)\s+(\w+)"
)


# --------------------------------------------------------------------------
# Scanning
# --------------------------------------------------------------------------

class Inventory:
    def __init__(self, root: str):
        self.root = os.path.abspath(root)
        self.categories = {name: set() for name in JAVA_CATEGORIES}
        self.sleigh = {label: [] for label in SLEIGH_EXTS.values()}
        self.script_dirs = []          # relative dirs named like script trees
        self.python_scripts = []       # *.py within script trees
        self.modules = {}              # container -> [module names]
        self.errors = []               # {'path': rel, 'error': str}
        self.java_files_scanned = 0
        self.sleigh_files_scanned = 0

    def rel(self, path: str) -> str:
        return os.path.relpath(path, self.root).replace(os.sep, "/")

    def scan(self) -> None:
        script_dir_paths = set()
        for dirpath, dirnames, filenames in os.walk(self.root):
            # Prune unwanted directories in place.
            dirnames[:] = [d for d in dirnames if d not in PRUNE_DIRS]

            base = os.path.basename(dirpath)
            if base in SCRIPT_DIR_NAMES:
                rel = self.rel(dirpath)
                self.script_dirs.append(rel)
                script_dir_paths.add(dirpath)

            for fn in filenames:
                full = os.path.join(dirpath, fn)
                _, ext = os.path.splitext(fn)

                if ext in JAVA_EXT:
                    self._scan_java(full)
                elif ext in SLEIGH_EXTS:
                    self.sleigh_files_scanned += 1
                    self.sleigh[SLEIGH_EXTS[ext]].append(self.rel(full))
                elif ext == ".py":
                    # Only count Python files that live under a script tree.
                    if self._under_script_tree(full):
                        self.python_scripts.append(self.rel(full))

        # Sort everything for deterministic output.
        for label in self.sleigh:
            self.sleigh[label].sort()
        self.script_dirs.sort()
        self.python_scripts.sort()

        self._inventory_modules()

    def _under_script_tree(self, path: str) -> bool:
        parts = self.rel(path).split("/")
        return any(p in SCRIPT_DIR_NAMES for p in parts[:-1])

    def _scan_java(self, full: str) -> None:
        try:
            with open(full, "r", encoding="utf-8", errors="replace") as fh:
                text = fh.read()
        except (OSError, ValueError) as exc:  # pragma: no cover - defensive
            self.errors.append({"path": self.rel(full), "error": str(exc)})
            return

        self.java_files_scanned += 1
        rel = self.rel(full)
        for name, patterns in JAVA_CATEGORIES.items():
            for rx in patterns:
                if rx.search(text):
                    self.categories[name].add(rel)
                    break

    def _inventory_modules(self) -> None:
        for container in MODULE_CONTAINERS:
            cpath = os.path.join(self.root, container)
            if not os.path.isdir(cpath):
                self.modules[container] = None  # absent
                continue
            mods = sorted(
                d for d in os.listdir(cpath)
                if os.path.isdir(os.path.join(cpath, d)) and not d.startswith(".")
            )
            self.modules[container] = mods

    # ---- derived views ----------------------------------------------------

    def totals(self) -> dict:
        t = {name: len(files) for name, files in self.categories.items()}
        t["sleigh_slaspec"] = len(self.sleigh["slaspec"])
        t["sleigh_sinc"] = len(self.sleigh["sinc"])
        t["sleigh_pspec"] = len(self.sleigh["pspec"])
        t["sleigh_cspec"] = len(self.sleigh["cspec"])
        t["script_dirs"] = len(self.script_dirs)
        t["python_scripts_in_trees"] = len(self.python_scripts)
        return t

    def to_dict(self) -> dict:
        return {
            "meta": {
                "tool": "tools/extension-inventory.py",
                "generated_utc": _now_iso(),
                "repo_root": self.root,
                "java_files_scanned": self.java_files_scanned,
                "sleigh_files_scanned": self.sleigh_files_scanned,
                "advisory": (
                    "Static heuristic scan. Counts are candidates, not "
                    "authoritative. No Java was parsed or executed."
                ),
            },
            "totals": self.totals(),
            "categories": {
                name: sorted(files) for name, files in self.categories.items()
            },
            "sleigh": self.sleigh,
            "script_dirs": self.script_dirs,
            "python_scripts_in_trees": self.python_scripts,
            "modules": self.modules,
            "errors": self.errors,
        }


# --------------------------------------------------------------------------
# Reporting
# --------------------------------------------------------------------------

CATEGORY_LABELS = {
    "plugins": "Plugins (Plugin / ProgramPlugin / @PluginInfo)",
    "analyzers": "Analyzers (AbstractAnalyzer / implements Analyzer)",
    "loaders": "Loaders (AbstractLibrarySupportLoader / implements Loader)",
    "exporters": "Exporters (Exporter / AbstractExporter)",
    "component_providers": "Component Providers (UI docking windows)",
    "extension_points": "Extension Points (implements/extends ExtensionPoint)",
    "ghidra_scripts_java": "GhidraScripts in Java (extends GhidraScript)",
    "docking_actions": "Docking Actions (DockingAction subclasses / new)",
}


def _now_iso() -> str:
    return datetime.datetime.now(datetime.timezone.utc).strftime(
        "%Y-%m-%dT%H:%M:%SZ"
    )


def render_markdown(inv: Inventory) -> str:
    t = inv.totals()
    out = []
    w = out.append

    w("# Extension Point Inventory (generated)")
    w("")
    w("> **GENERATED FILE — do not hand-edit.** Regenerate with:")
    w("> `python3 tools/extension-inventory.py`")
    w("")
    w("> ⚠️ **This is a static heuristic scan, not a perfect reflection of")
    w("> reality.** It matches text patterns in source files; it does not parse")
    w("> or compile Java. Every entry is a *candidate*. Base classes, abstract")
    w("> classes, test doubles, and example code are included. Conversely, a")
    w("> surface declared in an unusual way may be missed. Use this to orient,")
    w("> then confirm against the real source before relying on any specific")
    w("> result.")
    w("")
    w(f"- **Generated (UTC):** {_now_iso()}")
    w(f"- **Repo root:** `{inv.root}`")
    w(f"- **Java files scanned:** {inv.java_files_scanned}")
    w(f"- **Sleigh/processor files scanned:** {inv.sleigh_files_scanned}")
    w(f"- **Read errors:** {len(inv.errors)}")
    w("")

    # ---- totals -----------------------------------------------------------
    w("## Totals by category")
    w("")
    w("| Category | Candidate count |")
    w("| --- | ---: |")
    for name in JAVA_CATEGORIES:
        w(f"| {CATEGORY_LABELS.get(name, name)} | {t[name]} |")
    w(f"| Sleigh `.slaspec` files | {t['sleigh_slaspec']} |")
    w(f"| Sleigh `.sinc` files | {t['sleigh_sinc']} |")
    w(f"| Processor `.pspec` files | {t['sleigh_pspec']} |")
    w(f"| Compiler `.cspec` files | {t['sleigh_cspec']} |")
    w(f"| Script directories | {t['script_dirs']} |")
    w(f"| Python scripts in script trees | {t['python_scripts_in_trees']} |")
    w("")

    # ---- module inventory -------------------------------------------------
    w("## Module inventory")
    w("")
    w("Immediate subdirectories of each top-level container (modules).")
    w("")
    w("| Container | Modules | Count |")
    w("| --- | --- | ---: |")
    for container in MODULE_CONTAINERS:
        mods = inv.modules.get(container)
        if mods is None:
            w(f"| `{container}` | _(absent)_ | 0 |")
        else:
            shown = ", ".join(mods) if mods else "_(none)_"
            w(f"| `{container}` | {shown} | {len(mods)} |")
    w("")

    # ---- per-category candidates -----------------------------------------
    w("## Candidate files by category")
    w("")
    w(f"Up to {TOP_N} candidates shown per category (sorted). Full lists are in")
    w("the JSON report when generated with `--json`.")
    w("")
    for name in JAVA_CATEGORIES:
        files = sorted(inv.categories[name])
        w(f"### {CATEGORY_LABELS.get(name, name)} — {len(files)}")
        w("")
        if not files:
            w("_No candidates found._")
            w("")
            continue
        for rel in files[:TOP_N]:
            w(f"- `{rel}`")
        if len(files) > TOP_N:
            w(f"- … and {len(files) - TOP_N} more (see JSON).")
        w("")

    # ---- sleigh / processors ---------------------------------------------
    w("## Processor / Sleigh definitions")
    w("")
    w("Processor support is added as whole modules under `Ghidra/Processors`.")
    w("These file counts indicate the size of that surface.")
    w("")
    for label, key in [
        (".slaspec (Sleigh spec)", "slaspec"),
        (".sinc (Sleigh include)", "sinc"),
        (".pspec (processor spec)", "pspec"),
        (".cspec (compiler spec)", "cspec"),
    ]:
        files = inv.sleigh[key]
        w(f"### {label} — {len(files)}")
        w("")
        for rel in files[:TOP_N]:
            w(f"- `{rel}`")
        if len(files) > TOP_N:
            w(f"- … and {len(files) - TOP_N} more (see JSON).")
        if not files:
            w("_None found._")
        w("")

    # ---- script trees -----------------------------------------------------
    w("## Script trees")
    w("")
    w(f"**Script directories ({len(inv.script_dirs)}):**")
    w("")
    for rel in inv.script_dirs[:TOP_N]:
        w(f"- `{rel}`")
    if len(inv.script_dirs) > TOP_N:
        w(f"- … and {len(inv.script_dirs) - TOP_N} more (see JSON).")
    w("")
    w(f"**Python scripts under script trees ({len(inv.python_scripts)}):** "
      "shown in JSON report.")
    w("")

    # ---- errors -----------------------------------------------------------
    w("## Scan errors")
    w("")
    if not inv.errors:
        w("None — all matched source files were read successfully.")
    else:
        w(f"{len(inv.errors)} file(s) could not be read:")
        w("")
        for e in inv.errors[:TOP_N]:
            w(f"- `{e['path']}` — {e['error']}")
        if len(inv.errors) > TOP_N:
            w(f"- … and {len(inv.errors) - TOP_N} more.")
    w("")

    # ---- uncertainty notes ------------------------------------------------
    w("## Uncertainty notes")
    w("")
    w("- **Heuristic, not authoritative.** Patterns match declarations like")
    w("  `extends AbstractAnalyzer` or `@PluginInfo`. Unusual declarations")
    w("  (generics, multi-interface `implements`, indirection through base")
    w("  classes) may be over- or under-counted.")
    w("- **Base/abstract/test/example classes are included.** e.g. an")
    w("  `Abstract...Analyzer` or a `TestAnalyzer` counts as a candidate.")
    w("- **`implements ... Analyzer` / `... Loader`** uses a loose match and")
    w("  may catch unrelated interfaces ending in those words.")
    w("- **`new DockingAction`** counts anonymous-action usages, so the docking")
    w("  action count reflects *usage sites*, not distinct action classes.")
    w("- **Counts can shift** when upstream Ghidra is re-synced; regenerate the")
    w("  report after any sync.")
    w("- This report is advisory input for Phase 2+ planning, not a contract.")
    w("")

    return "\n".join(out) + "\n"


# --------------------------------------------------------------------------
# Main
# --------------------------------------------------------------------------

def default_root() -> str:
    # tools/extension-inventory.py -> repo root is the parent of tools/.
    return os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(
        description="Static extension-surface scanner for the Ghidra fork "
                    "(read-only; stdlib only)."
    )
    parser.add_argument(
        "--root", default=default_root(),
        help="Repository root to scan (default: parent of this script's dir).",
    )
    parser.add_argument(
        "--json", action="store_true",
        help="Also write the JSON report alongside the Markdown report.",
    )
    args = parser.parse_args(argv)

    root = os.path.abspath(args.root)
    if not os.path.isfile(os.path.join(root, "Ghidra", "application.properties")):
        sys.stderr.write(
            f"WARNING: {root} does not look like a Ghidra repo root "
            "(Ghidra/application.properties not found). Scanning anyway.\n"
        )

    inv = Inventory(root)
    inv.scan()

    out_dir = os.path.join(root, "fork", "docs", "generated")
    os.makedirs(out_dir, exist_ok=True)

    md_path = os.path.join(out_dir, "EXTENSIONPOINT_INVENTORY.md")
    with open(md_path, "w", encoding="utf-8") as fh:
        fh.write(render_markdown(inv))

    json_path = None
    if args.json:
        json_path = os.path.join(out_dir, "extensionpoint_inventory.json")
        with open(json_path, "w", encoding="utf-8") as fh:
            json.dump(inv.to_dict(), fh, indent=2, sort_keys=True)
            fh.write("\n")

    # ---- short stdout summary --------------------------------------------
    t = inv.totals()
    print("Ghidra fork — extension inventory (static scan)")
    print(f"  repo root            : {root}")
    print(f"  java files scanned   : {inv.java_files_scanned}")
    print(f"  sleigh files scanned : {inv.sleigh_files_scanned}")
    print(f"  plugins              : {t['plugins']}")
    print(f"  analyzers            : {t['analyzers']}")
    print(f"  loaders              : {t['loaders']}")
    print(f"  exporters            : {t['exporters']}")
    print(f"  component providers  : {t['component_providers']}")
    print(f"  extension points     : {t['extension_points']}")
    print(f"  ghidra scripts (java): {t['ghidra_scripts_java']}")
    print(f"  docking actions      : {t['docking_actions']}")
    print(f"  sleigh .slaspec/.sinc: {t['sleigh_slaspec']}/{t['sleigh_sinc']}")
    print(f"  proc .pspec/.cspec   : {t['sleigh_pspec']}/{t['sleigh_cspec']}")
    print(f"  script dirs          : {t['script_dirs']}")
    print(f"  read errors          : {len(inv.errors)}")
    print(f"  -> markdown          : {os.path.relpath(md_path, root)}")
    if json_path:
        print(f"  -> json              : {os.path.relpath(json_path, root)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
