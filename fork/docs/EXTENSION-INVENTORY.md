# EXTENSION-INVENTORY.md — The extension inventory

Phase 1 deliverable. This document explains the extension-inventory tool: what it is
for, how to run it, what it scans, how to read its output, and what it cannot tell
you.

## Purpose

Before this fork builds plugins, analyzers, or automation, it needs an honest map of
**where Ghidra's extension surfaces actually live** in this tree and roughly how many
of each exist. `tools/extension-inventory.py` produces that map by statically scanning
source files and emitting a report. It exists to:

- Orient development — find real examples to copy from for each surface.
- Resolve the open questions left in `fork/docs/ARCHITECTURE-NOTES.md`.
- Give Phase 2+ planning concrete counts instead of guesses.
- Provide a regenerable baseline that can be re-run after each upstream re-sync.

It is deliberately **read-only and additive**: it runs no Java, no Gradle, compiles
nothing, and only writes reports under `fork/docs/generated/`.

## How to run it

```bash
# Markdown report (default):
python3 tools/extension-inventory.py

# Also emit the JSON report with full candidate lists:
python3 tools/extension-inventory.py --json

# Scan a different repo root:
python3 tools/extension-inventory.py --root /path/to/repo
```

Requirements: Python 3 and the standard library only. No packages to install.

Outputs:

- `fork/docs/generated/EXTENSIONPOINT_INVENTORY.md` — human-readable.
- `fork/docs/generated/extensionpoint_inventory.json` — machine-readable (with `--json`).

A short summary is printed to stdout on every run.

## What it scans

**Java extension surfaces** (text-pattern matches on `*.java`):

| Category | Signals matched |
| --- | --- |
| Plugins | `extends Plugin` / `extends ProgramPlugin`, `@PluginInfo` |
| Analyzers | `extends AbstractAnalyzer`, `implements …Analyzer` |
| Loaders | `extends AbstractLibrarySupportLoader` / `AbstractProgramLoader`, `implements …Loader` |
| Exporters | `extends Exporter` / `AbstractExporter` |
| Component providers | `extends ComponentProvider` / `ComponentProviderAdapter` / `DialogComponentProvider` |
| Extension points | `implements …ExtensionPoint`, `interface … extends …ExtensionPoint` |
| GhidraScripts (Java) | `extends GhidraScript` |
| Docking actions | `extends DockingAction` (and relatives), `new DockingAction` |

**Processor / Sleigh definitions** (by file extension): `.slaspec`, `.sinc`,
`.pspec`, `.cspec`.

**Script trees**: directories named `ghidra_scripts`, `developer_scripts`,
`dev_scripts`, and the Python scripts found within them.

**Module inventory**: the immediate subdirectories (modules) of `Ghidra/Features`,
`Ghidra/Framework`, `Ghidra/Processors`, `Ghidra/Debug`, `Ghidra/Extensions`, `GPL`,
`GhidraBuild`, and `support` (if present).

Build output, VCS, and IDE metadata directories (`.git`, `build`, `dist`, `.gradle`,
`node_modules`, …) are pruned from the walk.

## How to interpret results

- **Counts are candidate counts, not class counts.** A file is counted once per
  category if any signal for that category matches.
- **Use the category lists as a "where do I look" index.** The top candidates per
  category point you at real, copyable examples.
- **`docking_actions` counts usage sites too.** Because `new DockingAction` is
  matched, that number reflects how often actions are created, not how many distinct
  action classes exist.
- **The naming convention matters.** Ghidra's `ClassSearcher` finds analyzers by the
  convention that *analyzer class names end in `Analyzer`*; plugins end in `Plugin`.
  The scanner keys off declarations, which is close but not identical to what the
  runtime discovers.
- For exhaustive lists (beyond the top-N shown in Markdown), read the JSON report.

## How this supports future development

- **Phase 2 (diagnostics plugin):** the `plugins`, `component_providers`, and
  `docking_actions` candidates are the templates for building a diagnostics plugin.
  See `fork/docs/PLUGIN-SKELETON-NOTES.md`.
- **Phase 4 (first analyzer):** the `analyzers` candidates and the analyzer interface
  are the templates for `BinaryEntropyAnalyzer`. See
  `fork/docs/ANALYZER-SKELETON-NOTES.md`.
- **Phase 3 (headless):** the script trees and GhidraScript candidates show where
  automation scripts live.
- Every phase regenerates this inventory after an upstream re-sync to catch drift.

## Known limitations of static scanning

- **It is heuristic.** It matches text, not semantics. Generics, multi-interface
  `implements` clauses, and indirection through intermediate base classes can cause
  both false positives and false negatives.
- **It includes base/abstract/test/example classes.** An `Abstract…Analyzer` or a
  `TestAnalyzer` is counted as a candidate.
- **Loose `implements` matching.** `implements …Analyzer` / `…Loader` can catch
  unrelated interfaces whose names happen to end that way.
- **It does not see runtime registration.** Services registered via mechanisms the
  scanner doesn't model are invisible to it.
- **Counts drift with upstream.** Re-run after every Ghidra re-sync.

Treat the report as a fast, regenerable orientation aid — **not** an authoritative
contract. The generated file repeats this warning at the top by design.
