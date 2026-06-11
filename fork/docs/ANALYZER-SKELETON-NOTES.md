# ANALYZER-SKELETON-NOTES.md — How Ghidra analyzers are shaped

Phase 1 note. This captures the **conceptual** structure of a Ghidra analyzer and
points at real examples found by `tools/extension-inventory.py`. **No analyzer is
generated here.** Phase 4 will use these notes to build `BinaryEntropyAnalyzer`.

> All paths below were located by the static scanner and the analyzer interface was
> read directly from source. Confirm details against the files before writing code.

## What a Ghidra analyzer is

An **analyzer** is an auto-analysis unit invoked by Ghidra's analysis framework over a
program (and an address set). Analyzers are discovered by the `ClassSearcher`.

> ⚠️ **Hard naming rule (from the source):** the `Analyzer` interface documents that
> *"ALL ANALYZER CLASSES MUST END IN 'Analyzer'"* — otherwise the `ClassSearcher` will
> not find them. Honor this for any new analyzer.

The relevant types live in `Ghidra/Features/Base/src/main/java/ghidra/app/services/`:

- `Analyzer.java` — the interface (`extends ExtensionPoint`).
- `AbstractAnalyzer.java` — the base class almost all analyzers extend; it provides
  sensible defaults so subclasses implement only what they need.

## Where existing examples were found

Analyzers (`extends AbstractAnalyzer` / `implements …Analyzer`) — 83 candidates.
Representative examples:

- `Ghidra/Features/Rust/src/main/java/ghidra/app/plugin/core/analysis/rust/RustStringAnalyzer.java`
- `Ghidra/Features/FileFormats/src/main/java/ghidra/macosx/analyzers/CFStringAnalyzer.java`
- `Ghidra/Features/FileFormats/src/main/java/ghidra/file/formats/cramfs/CramFsAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/AbstractBinaryFormatAnalyzer.java`
  (a base class — note it is a *candidate* too, illustrating why results are advisory).

The densest cluster of stock analyzers is under
`Ghidra/Features/Base/.../ghidra/app/plugin/core/analysis/...`.

## Minimal conceptual structure

A typical analyzer (subclassing `AbstractAnalyzer`) provides:

1. **`class FooAnalyzer extends AbstractAnalyzer`** — name ends in `Analyzer`. The
   constructor calls `super(name, description, AnalyzerType.…)` to declare what kind of
   analysis it performs (e.g. byte/instruction/function analysis).
2. **`canAnalyze(Program program)`** — return `true` only for programs this analyzer
   applies to (e.g. a given processor, format, or always).
3. **`added(Program program, AddressSetView set, TaskMonitor monitor, MessageLog log)`**
   — the core work; called when the relevant information is added. Returns `true` on
   success; must honor `monitor` for cancellation. (`removed(...)` is the counterpart.)
4. **`AnalysisPriority`** — via `setPriority(...)` / `getPriority()`, controls when it
   runs relative to other analyzers.
5. **Options (if any)** — `registerOptions(Options, Program)` and
   `optionsChanged(Options, Program)` to expose and react to user-configurable options;
   `getDefaultEnablement(Program)` decides whether it's on by default.
6. **Reporting / results** — analyzers surface findings by mutating the program through
   the normal APIs: creating data, **bookmarks**, **comments**, references, etc., and by
   writing to the `MessageLog`. For this fork, an analyzer may additionally export a
   report file (additive, non-destructive).

Other interface members with usable defaults from `AbstractAnalyzer`:
`getName`, `getAnalysisType`, `getDescription`, `supportsOneTimeAnalysis`,
`analysisEnded`, `isPrototype`.

## Notes for Phase 4 (`BinaryEntropyAnalyzer`)

- **Type/priority:** a byte-level entropy pass over memory blocks — likely a byte
  analyzer at low priority, `canAnalyze` returning `true` broadly.
- **Work:** compute entropy per memory block / region in `added(...)`, honoring the
  `TaskMonitor`.
- **Output:** mark high-entropy regions with **bookmarks** and **comments**, and export
  an entropy **report** — all additive, none of it altering disassembly/decompilation.
- **Options:** window size, entropy threshold, which block types to include.
- **Packaging:** ship as a **new additive module** (per ADR 0001); class name **must**
  end in `Analyzer`.
- **Do not build it yet** — this is planning input only.
