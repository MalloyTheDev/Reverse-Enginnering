# HEADLESS-RECIPES.md — Practical recipes

Copy-paste recipes for the Phase 3 headless toolkit. See `fork/docs/HEADLESS.md` for
concepts and safety boundaries. All recipes assume you run from the repo root and that
the wrappers are executable (`chmod +x scripts/headless/*.sh` if needed).

> Prerequisite for every recipe: a working `analyzeHeadless`, via either
> `GHIDRA_INSTALL_DIR` (installed Ghidra) or a built `build/dist/` (auto-detected).

```bash
export GHIDRA_INSTALL_DIR=/opt/ghidra_12.2_DEV   # adjust to your install
```

## 1. Analyze one benign binary

```bash
scripts/headless/analyze-one.sh \
  -i ./samples/hello \
  -p /tmp/ghproj \
  -n demo
```

Creates Ghidra project `demo` under `/tmp/ghproj` and runs auto-analysis on `hello`.

## 2. Analyze a folder

Dry-run first to see the exact command without executing:

```bash
scripts/headless/analyze-folder.sh -i ./samples -p /tmp/ghproj -n batch -d
```

Then for real (recursive import of everything Ghidra can load):

```bash
scripts/headless/analyze-folder.sh -i ./samples -p /tmp/ghproj -n batch
```

## 3. Export a triage report

```bash
scripts/headless/export-triage.sh \
  -i ./samples/hello \
  -p /tmp/ghproj \
  -n demo \
  -r ./out/hello
```

Outputs land in `./out/hello/`:

| File | Contents |
| --- | --- |
| `functions.csv` | name, entry, address range, size, param count, return type, calling convention, external/thunk flags |
| `strings.csv` | address, length, value (CSV-escaped) |
| `imports.csv` | external/import symbol name, address, library (header-only if none) |
| `triage.md` | program name, language, compiler spec, image base, block/function/string/import counts, timestamp |

## 4. Pass extra analyzeHeadless args

Everything after `--` is forwarded verbatim to `analyzeHeadless`:

```bash
# overwrite an existing program and cap analysis time per file
scripts/headless/analyze-one.sh -i ./samples/hello -p /tmp/ghproj -n demo -- \
  -overwrite -analysisTimeoutPerFile 120

# import without running analysis
scripts/headless/analyze-one.sh -i ./samples/hello -p /tmp/ghproj -n demo -- -noanalysis

# triage with overwrite
scripts/headless/export-triage.sh -i ./samples/hello -p /tmp/ghproj -n demo -r ./out/hello -- -overwrite
```

## 5. Run with a custom GHIDRA_INSTALL_DIR (one-off)

```bash
GHIDRA_INSTALL_DIR=/home/me/ghidra_12.2_DEV \
  scripts/headless/analyze-one.sh -i ./samples/hello -p /tmp/ghproj -n demo
```

## 6. Where outputs land

- **Ghidra project + logs:** under your `-p <project_dir>` (e.g. `/tmp/ghproj/demo.*`).
- **Triage reports:** under your `-r <report_dir>` (resolved to an absolute path).
- Nothing is written elsewhere; no files are deleted.

## 7. Troubleshooting

| Symptom | Likely cause / fix |
| --- | --- |
| `ERROR: Could not locate analyzeHeadless` | `GHIDRA_INSTALL_DIR` not set and no `build/dist/`. Set `GHIDRA_INSTALL_DIR` to an installed Ghidra. |
| `GHIDRA_INSTALL_DIR='…' but '…/support/analyzeHeadless' was not found` | The path isn't a full Ghidra install. Point it at the directory that contains `support/analyzeHeadless`. |
| `Permission denied` running a wrapper | `chmod +x scripts/headless/*.sh` (they should already be executable in git). |
| Program already exists / import fails on re-run | Add `-- -overwrite`. |
| `imports.csv` has only a header | The binary has no external symbols, or they weren't resolved; this is expected for some inputs (a note is printed to stderr). |
| Analysis hangs on a large/odd file | Add `-- -analysisTimeoutPerFile <seconds>`, or `-- -noanalysis` to import only. |
| Scripts not found by analyzeHeadless | Ensure you're using `export-triage.sh` (it sets `-scriptPath` automatically to `fork/headless/ghidra_scripts`). |
