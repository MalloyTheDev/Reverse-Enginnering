# ForkEntropy (fork Phase 4)

The fork's first analyzer: `BinaryEntropyAnalyzer` computes Shannon entropy per
initialized memory block and flags high-entropy blocks — a common indicator of
**packed, compressed, or encrypted** data.

> Full notes: `fork/docs/ENTROPY.md`.

## Behavior

- **Disabled by default.** It is a specialized, opt-in analyzer; enable it in
  *Analysis Options* (or via `-analysisTimeoutPerFile`/options in headless). It does
  not change stock auto-analysis unless enabled.
- For each initialized memory block at or above the entropy threshold, it adds:
  - an **Analysis bookmark** at the block start, and
  - a **plate comment** noting the entropy value.
- All per-block entropy values are written to the analysis **MessageLog**.
- If a report path is configured, it writes a **CSV** (`block,start,size_bytes,entropy_bits,flagged`).

## Options

| Option | Default | Meaning |
| --- | --- | --- |
| High-entropy threshold (bits/byte) | `7.0` | Blocks with entropy ≥ this (0–8) are flagged. |
| Minimum block size (bytes) | `256` | Smaller initialized blocks are skipped. |
| Entropy report file (optional) | _(empty)_ | If set, a CSV report is written there. |

## Safety

- **Additive annotations only** — bookmarks and plate comments. It does not change
  disassembly, data, functions, names, or types.
- **No network.** The only optional file write is the CSV report, when a path is set.

## Structure

- `src/main/java/ghidra/fork/entropy/ShannonEntropy.java` — dependency-free entropy
  calculation (unit-tested in isolation).
- `src/main/java/ghidra/fork/entropy/BinaryEntropyAnalyzer.java` — the analyzer.
- `src/test/java/ghidra/fork/entropy/ShannonEntropyTest.java` — unit tests.
