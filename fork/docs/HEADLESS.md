# HEADLESS.md — Fork headless automation (Phase 3)

Phase 3 adds a small, **scripts-first** headless automation layer on top of Ghidra's
`analyzeHeadless` workflow. It is additive: shell wrappers under `scripts/headless/`,
read-only fork-owned GhidraScripts under `fork/headless/ghidra_scripts/`, and these
docs. No new plugin, no new analyzer, no Ghidra core changes.

## What Phase 3 adds

| Component | Path | Purpose |
| --- | --- | --- |
| `common.sh` | `scripts/headless/common.sh` | Shared helpers: locate `analyzeHeadless`, validate inputs, prepare output dirs. Sourced by the others. |
| `analyze-one.sh` | `scripts/headless/analyze-one.sh` | Import + analyze a single binary. |
| `analyze-folder.sh` | `scripts/headless/analyze-folder.sh` | Import + analyze a folder (recursive); supports `-d` dry-run. |
| `export-triage.sh` | `scripts/headless/export-triage.sh` | Import + analyze, then run the export scripts to produce a triage bundle. |
| `ForkExportFunctions.java` | `fork/headless/ghidra_scripts/` | → `functions.csv` |
| `ForkExportStrings.java` | `fork/headless/ghidra_scripts/` | → `strings.csv` |
| `ForkExportImports.java` | `fork/headless/ghidra_scripts/` | → `imports.csv` |
| `ForkTriageReport.java` | `fork/headless/ghidra_scripts/` | → `triage.md` |

## Requirement: `GHIDRA_INSTALL_DIR` (the clean path)

These wrappers need a real `analyzeHeadless` launcher, which lives at
`<ghidra>/support/analyzeHeadless` in an **installed/extracted Ghidra distribution**.

> **Important:** a source checkout is *not* an installed distribution. `analyzeHeadless`
> in the source tree (`Ghidra/RuntimeScripts/support/`) is not guaranteed to run until a
> release has been built/extracted. So the wrappers treat `GHIDRA_INSTALL_DIR` as the
> supported path and **fail cleanly** otherwise — they never pretend a source-tree run
> succeeded.

Set it to the root of an installed Ghidra:

```bash
export GHIDRA_INSTALL_DIR=/opt/ghidra_12.2_DEV   # contains support/analyzeHeadless
```

### Installed Ghidra vs. a local source build

- **Installed/extracted release (recommended):** set `GHIDRA_INSTALL_DIR`. The wrappers
  use `$GHIDRA_INSTALL_DIR/support/analyzeHeadless`.
- **Local source build:** if you have built a distribution in this repo (`./gradlew
  buildGhidra` then extracted under `build/dist/`), the wrappers will **auto-detect**
  `build/dist/*/support/analyzeHeadless` when `GHIDRA_INSTALL_DIR` is unset. This is a
  convenience, not a requirement.

### How `common.sh` finds `analyzeHeadless`

1. If `GHIDRA_INSTALL_DIR` is set → use `$GHIDRA_INSTALL_DIR/support/analyzeHeadless`
   (error if missing).
2. Else → auto-detect `build/dist/*/support/analyzeHeadless` in the repo.
3. Else → print a clear error explaining both options and exit non-zero.

No machine-specific paths are hardcoded anywhere.

## Safety boundaries

- **Read-only analysis intent.** The fork GhidraScripts only *read* the program (no
  patching, renaming, comments, bookmarks, or other mutation) and write only the
  requested report files.
- **No network.** Nothing contacts a remote service.
- **No destructive deletes.** Wrappers create directories (`mkdir -p`) but never delete
  projects or files. They do not pass `-deleteProject` unless *you* add it after `--`.
- **Projects are kept by default.** Re-running may require `-overwrite` (pass it after
  `--`).
- **Legitimate use only.** This toolkit is for reverse engineering, education,
  diagnostics, binary triage, and authorized analysis — not offensive tooling.

## Output expectations

- **Ghidra project** is created under the `-p <project_dir>` you supply, with name
  `-n <project_name>`.
- **Reports** (from `export-triage.sh`) are written by the GhidraScripts into the
  `-r <report_dir>` you supply: `functions.csv`, `strings.csv`, `imports.csv`,
  `triage.md`. The report dir is resolved to an absolute path before the run.
- You choose all output locations; nothing is written outside them (besides Ghidra's
  own project/log files under the project dir).

## Examples

```bash
export GHIDRA_INSTALL_DIR=/opt/ghidra_12.2_DEV

# Analyze one binary
scripts/headless/analyze-one.sh -i ./samples/hello -p /tmp/ghproj -n demo

# Analyze a folder (recursive), dry-run first
scripts/headless/analyze-folder.sh -i ./samples -p /tmp/ghproj -n batch -d
scripts/headless/analyze-folder.sh -i ./samples -p /tmp/ghproj -n batch

# Produce a triage bundle (functions/strings/imports/triage.md)
scripts/headless/export-triage.sh -i ./samples/hello -p /tmp/ghproj -n demo -r ./out/hello

# Pass extra analyzeHeadless args (after --), e.g. overwrite + per-file timeout
scripts/headless/analyze-one.sh -i ./samples/hello -p /tmp/ghproj -n demo -- -overwrite -analysisTimeoutPerFile 120
```

See `fork/docs/HEADLESS-RECIPES.md` for more, including troubleshooting.

## What's deferred (not in this MVP)

Decompiled-output export, cross-reference graphs, data-type/struct catalogs, richer
function signatures, JSON output formats, and any timing/diagnostics integration. These
are intentionally out of scope for the Phase 3 MVP.
