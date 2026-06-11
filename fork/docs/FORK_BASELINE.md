# FORK_BASELINE.md — Recorded baseline

This file records the state of the fork at the moment Phase 0 baseline documentation
was created. It is the reference point everything else is measured against.

## Provenance

| Field | Value |
| --- | --- |
| Original source | **NSA Ghidra** (National Security Agency) |
| Upstream repo | https://github.com/NationalSecurityAgency/ghidra |
| Downstream repo | **MalloyTheDev/Reverse-Enginnering** |
| Fork type | Private downstream research/workflow fork |
| Baseline date | **2026-06-11** |

## Version / toolchain baseline

Source of truth: `Ghidra/application.properties`.

| Property | Value |
| --- | --- |
| `application.name` | Ghidra |
| `application.version` | **12.2** |
| `application.release.name` | DEV |
| `application.layout.version` | 3 |
| `application.gradle.min` | **8.5** |
| `application.gradle.max` | (unset — no upper bound declared) |
| `application.java.min` | **21** |
| `application.java.max` | (unset — no upper bound declared) |
| `application.java.compiler` | **21** |
| `application.python.supported` | **3.14, 3.13, 3.12, 3.11, 3.10, 3.9** |

### Requirements, restated plainly

- **Java:** JDK **21** required (compiler target 21, minimum 21, no max declared).
- **Gradle:** **8.5 or newer** (`application.gradle.min=8.5`, no max declared).
- **Python:** one of **3.9, 3.10, 3.11, 3.12, 3.13, 3.14** (for PyGhidra / Python
  scripting support).

### Observed environment at baseline capture

These are what the baseline machine happened to have; they are *observations*, not
requirements:

- `java -version` → OpenJDK **21.0.10** (2026-01-20). ✅ satisfies Java 21.
- `python3 --version` → Python **3.11.15**. ✅ within supported range.
- `./gradlew --version` → **not run**: the wrapper script was not executable on
  checkout (`Permission denied`). `scripts/bootstrap.sh` performs `chmod +x ./gradlew`
  to fix this. The wrapper itself pins the Gradle version used by the build.

## README status

The top-level `README.md` is **still the stock NSA Ghidra README** (verified by
inspection — it opens with the NSA Ghidra description, security-warning, and install
sections and the NSA `GHIDRA_3.png` banner). It has **not** been rebranded for this
fork. Any future rebrand should be deliberate and recorded here.

`DISCLAIMER.md`, `LICENSE`, `NOTICE`, `SECURITY.md`, and `CONTRIBUTING.md` are
likewise stock at baseline.

## What has changed so far

At baseline, the **only** additions relative to stock Ghidra are the Phase 0
scaffolding produced in this pass:

- `fork/docs/FORK.md`
- `fork/docs/FORK_BASELINE.md` (this file)
- `fork/docs/UPSTREAM.md`
- `fork/docs/BUILD-NOTES.md`
- `fork/docs/ARCHITECTURE-NOTES.md`
- `fork/docs/ROADMAP.md`
- `fork/docs/ADR/0001-fork-boundaries.md`
- `scripts/bootstrap.sh`
- `scripts/bootstrap.ps1`
- `fork/docs/gradle-tasks.txt` (generated during validation, if produced)

**No Ghidra core source files (Java, C++, Sleigh, Python) have been modified.** The
changes are documentation and two safe bootstrap scripts only.

## Compatibility goals

The fork commits to preserving stock Ghidra behavior. The following must remain
fully compatible with upstream Ghidra and must keep working unchanged:

- **Project loading** — create / open / save projects.
- **Program import** — all stock loaders.
- **Disassembly.**
- **Decompilation.**
- **Analyzers** — auto-analysis and individual analyzers.
- **Plugins** — the plugin/tool model and all stock plugins.
- **Scripts** — GhidraScript in Java and Python.
- **Sleigh processors** — all `Ghidra/Processors/*` specifications.
- **PyGhidra.**
- **Headless mode** — `analyzeHeadless`.
- **Extensions** — the extension packaging/loading model.

If any future change threatens one of these, it must be feature-flagged, isolated in
a new module, or rejected — per `fork/docs/ADR/0001-fork-boundaries.md`.
