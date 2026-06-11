# ARCHITECTURE-NOTES.md — First-pass repo map

> **First pass only.** This is a rough orientation map written during Phase 0 from a
> top-level inspection of the tree, not a deep code study. Anything marked
> _(uncertain)_ needs confirmation before being relied on. Directory contents were
> observed at baseline date 2026-06-11.

## Top-level layout

| Path | What it is |
| --- | --- |
| `Ghidra/` | The main Ghidra application: all framework, features, processors, debug, configs. |
| `GhidraBuild/` | Build-time support: `BuildFiles`, `EclipsePlugins`, `IDAPro`, `LaunchSupport`, `MarkdownSupport`, `Skeleton`, `patch`. |
| `GhidraDocs/` | End-user / developer documentation that ships with Ghidra. |
| `GPL/` | GPL-licensed components kept separate from the main (Apache-2.0) tree. _(uncertain on exact contents)_ |
| `gradle/` | Shared Gradle logic, including `gradle/support/fetchDependencies.gradle`. |
| `support/` _(uncertain)_ | Per `DevGuide`/Ghidra convention, runtime support scripts (e.g. `analyzeHeadless`, launch scripts) typically live under `Ghidra/RuntimeScripts/.../support`. A top-level `support/` was **not** observed at baseline — treat references to "support" as the runtime support scripts, location to confirm. |
| `docker/` | Docker build/run assets. |
| `eclipse/` | Eclipse workspace assets. |
| `licenses/` | Third-party license texts. |
| `build.gradle`, `settings.gradle`, `gradle.properties` | Root Gradle build definition. |
| `gradlew`, `gradlew.bat` | Gradle wrapper (Unix / Windows). |
| `README.md`, `DevGuide.md`, `CONTRIBUTING.md`, `DISCLAIMER.md`, `SECURITY.md`, `LICENSE`, `NOTICE` | Stock Ghidra top-level docs/metadata. |

## Inside `Ghidra/`

Observed subdirectories: `Configurations`, `Debug`, `Extensions`, `Features`,
`Framework`, `Processors`, `RuntimeScripts`, `Test`, plus `application.properties`
and `certification.local.manifest`.

### `Ghidra/Framework`

The platform layer everything else builds on. Observed modules:
`DB`, `Docking`, `Emulation`, `FileSystem`, `Generic`, `Graph`, `Gui`, `Help`,
`Project`, `Pty`, `SoftwareModeling`, `Utility`.

- `DB`, `SoftwareModeling`, `Project` — **danger zone.** These are where the program
  database (ProgramDB) and project/database format live. Do not edit early.
- `Docking`, `Gui` — the windowing / docking-action framework; relevant later for UX
  plugins, but core to every tool.

### `Ghidra/Features`

The large functional modules. Observed (non-exhaustive): `Base`, `Decompiler`,
`DecompilerDependent`, `FunctionGraph`, `FunctionGraphDecompilerExtension`,
`ByteViewer`, `BytePatterns`, `CodeCompare`, `DataGraph`, `FileFormats`, `FunctionID`,
`GhidraServer`, `GhidraGo`, `GnuDemangler`, `MicrosoftCodeAnalyzer`,
`MicrosoftDemangler`, `MicrosoftDmang`, `Objective-C`, `PDB`, `ProgramDiff`,
`ProgramGraph`, `PyGhidra`, `Recognizers`, `Rust`, `Sarif`, `SourceCodeLookup`,
`Swift`, `SystemEmulation`, `VersionTracking`, `VersionTrackingBSim`, `BSim`,
`BSimFeatureVisualizer`, `GraphServices`, `GraphFunctionCalls`, `DebugUtils`,
`WildcardAssembler`.

Key ones for this fork:

- **`Ghidra/Features/Decompiler`** — ⚠️ note: the decompiler lives **here**, under
  `Features`, **not** at a top-level `Ghidra/Decompiler`. Its native C++ source is
  under `Ghidra/Features/Decompiler/src/decompile/...`. **Danger zone — do not edit
  the C++.**
- **`Ghidra/Features/Base`** — the home of most stock plugins, analyzers, loaders,
  exporters, and GhidraScript infrastructure. Likely the most useful module to read
  when learning the extension surfaces below.
- **`Ghidra/Features/PyGhidra`** — Python integration; relevant to headless/AI phases.

### `Ghidra/Processors`

One module per processor family — the Sleigh specifications. Observed:
`6502`, `68000`, `8048`, `8051`, `8085`, `AARCH64`, `ARM`, `Atmel`, `BPF`, `CP1600`,
`CR16`, `DATA`, `Dalvik`, `HCS08`, `HCS12`, `Hexagon`, `JVM`, `Loongarch`, `M16C`,
`M8C`, `MC6800`, `MCS96`, `MIPS`, `NDS32`, `PA-RISC`, `PIC`, `PowerPC`, `RISCV`,
`Sparc`, `SuperH`, `SuperH4`, `TI_MSP430`, `Toy`, `V850`, `Xtensa`, `Z80`, `eBPF`,
`tricore`, `x86`.

- Individual `.slaspec`/`.sinc` processor files are sensitive but are *additive-safe*
  if you add a **new** processor module. Editing the **Sleigh compiler itself** is
  danger zone; editing existing processor specs risks correctness regressions.

### `Ghidra/Debug`

The debugger stack. Observed: `Debugger`, `Debugger-agent-dbgeng`,
`Debugger-agent-drgn`, `Debugger-agent-gdb`, `Debugger-agent-lldb`,
`Debugger-agent-x64dbg`, `Debugger-api`, `Debugger-importers`, `Debugger-isf`,
`Debugger-jpda`, `Debugger-rmi-trace`, `Framework-TraceModeling`, `ProposedUtils`,
`TaintAnalysis`, `AnnotationValidator`.

Largely out of scope for early fork phases, but `Framework-TraceModeling` and
`TaintAnalysis` are interesting for later analysis work. _(uncertain — not studied)_

### `Ghidra/Extensions`

The most fork-friendly area: self-contained extension modules. Observed:
`BSimElasticPlugin`, `Jython`, `Lisa`, `MachineLearning`, `SampleTablePlugin`,
`SleighDevTools`, `SymbolicSummaryZ3`, `bundle_examples`, `sample`.

- `sample`, `bundle_examples`, `SampleTablePlugin` — **read these first**; they are
  the templates for how a new extension/plugin is structured and packaged.
- `MachineLearning` — _(uncertain)_ worth examining before the Phase 6 AI work, as it
  may show how ML-ish features are already wired in.

### `Ghidra/Configurations`, `Ghidra/RuntimeScripts`, `Ghidra/Test`

- `Configurations` — application assembly/packaging configs. _(uncertain on detail)_
- `RuntimeScripts` — launch scripts and runtime `support/` scripts (this is where
  `analyzeHeadless` and friends generally live). Relevant to Phase 3 headless work.
- `Test` — test harness modules. Relevant to compatibility-test requirements.

## Likely extension surfaces

Where this fork's future functionality is expected to plug in (all **additive**,
none requiring core edits when done correctly):

| Surface | What it is | Where it lives (likely) |
| --- | --- | --- |
| **Plugins** | Tool-loaded UI/behavior units. | `Ghidra/Features/Base`, `Ghidra/Extensions/*`. |
| **Analyzers** | Auto-analysis units registered with the analyzer framework. | `Ghidra/Features/Base` (analyzers), new extension modules. |
| **Loaders** | Importers for executable/file formats. | `Ghidra/Features/Base`, `Ghidra/Features/FileFormats`. |
| **Exporters** | Writers that emit data out of a program. | `Ghidra/Features/Base`. |
| **GhidraScript** | Java/Python scripts run from the Script Manager / headless. | `ghidra_scripts/` dirs across modules. |
| **Docking actions** | Menu/toolbar/context actions in the UI. | `Ghidra/Framework/Docking` (framework); actions registered by plugins. |
| **Processor / Sleigh definitions** | New processor support. | New module under `Ghidra/Processors/*`. |
| **Headless automation** | Scripted, no-GUI analysis. | `analyzeHeadless` + scripts; runtime support scripts. |

> All of the above are confirmed as *categories* Ghidra supports; the **exact file
> paths** for each are first-pass guesses and should be confirmed in Phase 1 (the
> extension-inventory phase exists precisely to nail these down).

## Open questions to resolve in Phase 1

- Exact location and naming convention for analyzers vs. plugins in `Features/Base`.
- Where exporters/loaders register themselves (service/registry mechanism).
- Canonical minimal example for a new extension module (likely `Ghidra/Extensions/sample`).
- Whether a top-level `support/` exists or whether it is purely under
  `Ghidra/RuntimeScripts`.
- Contents of `GPL/` and what is licensed there.
