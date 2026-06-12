# ENTROPY.md — Binary entropy analyzer (Phase 4)

Phase 4 delivers the fork's **first analyzer**, `BinaryEntropyAnalyzer`, in the additive
extension module `Ghidra/Extensions/ForkEntropy/`. It builds on the extension path
proven in Phase 2 and the analyzer-skeleton notes from Phase 1.

## Goal

Compute **Shannon entropy** per initialized memory block and surface high-entropy
blocks — a fast, classic triage signal for **packed, compressed, or encrypted** data.

## What it does

- Iterates initialized memory blocks (optionally limited to the address set being
  analyzed), reads their bytes in chunks, and computes entropy in **bits/byte (0–8)**.
- For blocks at/above the threshold, adds an **Analysis bookmark** and a **plate
  comment** at the block start.
- Logs every block's entropy to the analysis **MessageLog**.
- Optionally writes a **CSV report** if a report path is configured.

## Why a separate `ShannonEntropy` class

The calculation lives in `ShannonEntropy.java`, which has **no Ghidra dependency**, so
it can be compiled and unit-tested in isolation (same pattern as Phase 2's
`ForkDiagnosticsLog`). The analyzer is the thin Ghidra glue that reads memory and writes
annotations. Ghidra's built-in entropy code (`ghidra/app/plugin/core/overview/entropy`)
is coupled to the overview UI/palette, so a small standalone calculation is cleaner than
reusing it.

## Design choices (conservative)

| Choice | Value | Rationale |
| --- | --- | --- |
| Default enablement | **Off** | Specialized/opt-in; never changes stock auto-analysis unless enabled. |
| Analyzer type | `BYTE_ANALYZER` | Entropy is a byte-level property. |
| Priority | `LOW_PRIORITY` | Runs late, after core analysis. |
| One-time analysis | Supported | Can be run on demand from the GUI. |
| Threshold | `7.0` bits/byte | Common "looks compressed/encrypted" boundary; configurable. |
| Min block size | `256` bytes | Avoids noise from tiny blocks; configurable. |
| Report | Off unless path set | No surprise file writes. |

## Options

- **High-entropy threshold (bits/byte)** — default `7.0`.
- **Minimum block size (bytes)** — default `256`.
- **Entropy report file (optional)** — default empty (no file).

## Safety

- **Additive annotations only.** Bookmarks (type `Analysis`, category `Binary
  Entropy`) and plate comments. It does **not** modify disassembly, data, functions,
  symbol names, or types. Annotation failures are swallowed so analysis is never
  disrupted.
- **No network.** The only optional output is the local CSV report.
- **Additive module.** No Ghidra core edits; auto-discovered under
  `Ghidra/Extensions/`.

## How to use

1. Build/install the extension (or run within a Ghidra that includes this module).
2. In the CodeBrowser: **Analysis → Auto Analyze…**, enable **"Binary Entropy
   (fork)"**, optionally adjust its options, and run. Or run it as a one-time analysis.
3. Review the bookmarks (Bookmarks window, category *Binary Entropy*), the plate
   comments at flagged block starts, the MessageLog, and the CSV report if configured.

Headless: enable the analyzer via analysis options when invoking `analyzeHeadless`
(the fork headless wrappers from Phase 3 can drive the import/analysis).

## Validation status

- `ShannonEntropy` is dependency-free and unit-tested; its math is verified
  (identical bytes → 0.0, uniform over all 256 values → 8.0, 50/50 → 1.0).
- The analyzer compiles against the real Ghidra classpath and its module tests run via
  `./gradlew :ForkEntropy:compileJava :ForkEntropy:test` (after a one-time
  `fetchDependencies`). A full `buildGhidra` is not required to validate the module.
- A runtime check on a real binary (e.g. via the Phase 3 headless wrappers with the
  analyzer enabled) is the recommended next step before relying on results.

## Deferred

Sliding-window / sub-block entropy maps, per-section heuristics beyond a flat
threshold, automatic data-type or section labeling, and any GUI visualization are out
of scope for this MVP.
