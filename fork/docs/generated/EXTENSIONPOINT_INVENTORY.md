# Extension Point Inventory (generated)

> **GENERATED FILE — do not hand-edit.** Regenerate with:
> `python3 tools/extension-inventory.py`

> ⚠️ **This is a static heuristic scan, not a perfect reflection of
> reality.** It matches text patterns in source files; it does not parse
> or compile Java. Every entry is a *candidate*. Base classes, abstract
> classes, test doubles, and example code are included. Conversely, a
> surface declared in an unusual way may be missed. Use this to orient,
> then confirm against the real source before relying on any specific
> result.

- **Generated (UTC):** 2026-06-12T04:07:16Z
- **Repo root:** `/home/user/Reverse-Enginnering`
- **Java files scanned:** 15613
- **Sleigh/processor files scanned:** 562
- **Read errors:** 0

## Totals by category

| Category | Candidate count |
| --- | ---: |
| Plugins (Plugin / ProgramPlugin / @PluginInfo) | 238 |
| Analyzers (AbstractAnalyzer / implements Analyzer) | 84 |
| Loaders (AbstractLibrarySupportLoader / implements Loader) | 22 |
| Exporters (Exporter / AbstractExporter) | 11 |
| Component Providers (UI docking windows) | 262 |
| Extension Points (implements/extends ExtensionPoint) | 72 |
| GhidraScripts in Java (extends GhidraScript) | 356 |
| Docking Actions (DockingAction subclasses / new) | 359 |
| Sleigh `.slaspec` files | 150 |
| Sleigh `.sinc` files | 210 |
| Processor `.pspec` files | 90 |
| Compiler `.cspec` files | 112 |
| Script directories | 37 |
| Python scripts in script trees | 25 |

## Module inventory

Immediate subdirectories of each top-level container (modules).

| Container | Modules | Count |
| --- | --- | ---: |
| `Ghidra/Features` | BSim, BSimFeatureVisualizer, Base, BytePatterns, ByteViewer, CodeCompare, DataGraph, DebugUtils, Decompiler, DecompilerDependent, FileFormats, FunctionGraph, FunctionGraphDecompilerExtension, FunctionID, GhidraGo, GhidraServer, GnuDemangler, GraphFunctionCalls, GraphServices, MicrosoftCodeAnalyzer, MicrosoftDemangler, MicrosoftDmang, Objective-C, PDB, ProgramDiff, ProgramGraph, PyGhidra, Recognizers, Rust, Sarif, SourceCodeLookup, Swift, SystemEmulation, VersionTracking, VersionTrackingBSim, WildcardAssembler | 36 |
| `Ghidra/Framework` | DB, Docking, Emulation, FileSystem, Generic, Graph, Gui, Help, Project, Pty, SoftwareModeling, Utility | 12 |
| `Ghidra/Processors` | 6502, 68000, 8048, 8051, 8085, AARCH64, ARM, Atmel, BPF, CP1600, CR16, DATA, Dalvik, HCS08, HCS12, Hexagon, JVM, Loongarch, M16C, M8C, MC6800, MCS96, MIPS, NDS32, PA-RISC, PIC, PowerPC, RISCV, Sparc, SuperH, SuperH4, TI_MSP430, Toy, V850, Xtensa, Z80, eBPF, tricore, x86 | 39 |
| `Ghidra/Debug` | AnnotationValidator, Debugger, Debugger-agent-dbgeng, Debugger-agent-drgn, Debugger-agent-gdb, Debugger-agent-lldb, Debugger-agent-x64dbg, Debugger-api, Debugger-importers, Debugger-isf, Debugger-jpda, Debugger-rmi-trace, Framework-TraceModeling, ProposedUtils, TaintAnalysis | 15 |
| `Ghidra/Extensions` | BSimElasticPlugin, ForkDiagnostics, ForkEntropy, Jython, Lisa, MachineLearning, SampleTablePlugin, SleighDevTools, SymbolicSummaryZ3, bundle_examples, sample | 11 |
| `GPL` | DMG, DemanglerGnu, GnuDisassembler, Icons, licenses | 5 |
| `GhidraBuild` | BuildFiles, EclipsePlugins, IDAPro, LaunchSupport, MarkdownSupport, Skeleton, patch | 7 |
| `support` | _(absent)_ | 0 |

## Candidate files by category

Up to 25 candidates shown per category (sorted). Full lists are in
the JSON report when generated with `--json`.

### Plugins (Plugin / ProgramPlugin / @PluginInfo) — 238

- `Ghidra/Debug/Debugger-rmi-trace/src/main/java/ghidra/app/plugin/core/debug/gui/tracermi/connection/TraceRmiConnectionManagerPlugin.java`
- `Ghidra/Debug/Debugger-rmi-trace/src/main/java/ghidra/app/plugin/core/debug/gui/tracermi/launcher/TraceRmiLauncherServicePlugin.java`
- `Ghidra/Debug/Debugger-rmi-trace/src/main/java/ghidra/app/plugin/core/debug/service/tracermi/TraceRmiPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/AbstractDebuggerPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/disassemble/DebuggerDisassemblerPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/DebuggerBreakpointMarkerPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/DebuggerBreakpointsPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/timeline/BreakpointTimelinePlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/console/DebuggerConsolePlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/control/DebuggerControlPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/control/DebuggerMethodActionsPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/copying/DebuggerCopyActionsPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/diff/DebuggerTraceViewDiffPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/internal/RStarDiagnosticsPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/listing/DebuggerListingPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memory/DebuggerMemoryBytesPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memory/DebuggerRegionsPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memview/DebuggerMemviewPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/model/DebuggerModelPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/modules/DebuggerModulesPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/modules/DebuggerStaticMappingPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/pcode/DebuggerPcodeStepperPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/platform/DebuggerPlatformPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/register/DebuggerRegistersPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/stack/DebuggerStackPlugin.java`
- … and 213 more (see JSON).

### Analyzers (AbstractAnalyzer / implements Analyzer) — 84

- `Ghidra/Extensions/ForkEntropy/src/main/java/ghidra/fork/entropy/BinaryEntropyAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/AbstractBinaryFormatAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/AppleSingleDoubleAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/CoffAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/CoffArchiveAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/CondenseFillerBytesAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/ElfAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/MachoAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/PefAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/analyzers/PortableExecutableAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/AbstractDemanglerAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/ApplyDataArchiveAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/ArmSymbolAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/CliMetadataTokenAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/ConstantPropagationAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/DWARFAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/EmbeddedMediaAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/ExternalSymbolResolverAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/FindNoReturnFunctionsAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/GolangStringAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/GolangSymbolAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/MachoFunctionStartsAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/MingwRelocationAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/NoReturnFunctionAnalyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/OperandReferenceAnalyzer.java`
- … and 59 more (see JSON).

### Loaders (AbstractLibrarySupportLoader / implements Loader) — 22

- `Ghidra/Debug/Debugger-importers/src/main/java/ghidra/app/util/opinion/TenetLoader.java`
- `Ghidra/Debug/Debugger-importers/src/main/java/ghidra/app/util/opinion/TenetPlusPlusLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/importer/SingleLoaderFilter.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/AbstractLibrarySupportLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/AbstractOrdinalSupportLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/AbstractPeDebugLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/AbstractProgramLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/AbstractProgramWrapperLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/BinaryLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/CoffLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/ComLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/DecompileDebugXmlLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/ElfLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/GdtLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/GzfLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/IntelHexLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/MachoLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/MotorolaHexLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/MzLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/NeLoader.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/opinion/XmlLoader.java`
- `Ghidra/Features/Sarif/src/main/java/sarif/SarifLoader.java`

### Exporters (Exporter / AbstractExporter) — 11

- `Ghidra/Features/Base/src/main/java/ghidra/app/util/exporter/AsciiExporter.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/exporter/BinaryExporter.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/exporter/GdtExporter.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/exporter/GzfExporter.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/exporter/HtmlExporter.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/exporter/IntelHexExporter.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/exporter/OriginalFileExporter.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/exporter/XmlExporter.java`
- `Ghidra/Features/Decompiler/src/main/java/ghidra/app/util/exporter/CppExporter.java`
- `Ghidra/Features/Sarif/src/main/java/sarif/export/SarifExporter.java`
- `GhidraBuild/Skeleton/src/main/java/skeleton/SkeletonExporter.java`

### Component Providers (UI docking windows) — 262

- `Ghidra/Debug/Debugger-rmi-trace/src/main/java/ghidra/app/plugin/core/debug/gui/tracermi/connection/TraceRmiConnectionManagerProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/AbstractDebuggerSleighInputDialog.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/DebuggerBreakpointsProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/DebuggerPlaceBreakpointDialog.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/timeline/BreakpointTimelineActions.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/timeline/BreakpointTimelineProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/console/DebuggerConsoleProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/internal/RStarPlotProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/internal/RStarTreeProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memory/DebuggerRegionsProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memview/MemviewProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/model/DebuggerModelProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/modules/DebuggerModulesProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/modules/DebuggerStaticMappingProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/pcode/DebuggerPcodeStepperProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/platform/DebuggerSelectPlatformOfferDialog.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/register/DebuggerRegistersProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/stack/DebuggerStackProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/thread/DebuggerThreadsProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/time/DebuggerTimeProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/time/DebuggerTimeSelectionDialog.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/tracecalltree/TraceCallTreeProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/watch/DebuggerWatchesProvider.java`
- `Ghidra/Extensions/MachineLearning/src/main/java/ghidra/machinelearning/functionfinding/ProgramAssociatedComponentProviderAdapter.java`
- `Ghidra/Extensions/SampleTablePlugin/src/main/java/ghidra/examples/SampleTableProvider.java`
- … and 237 more (see JSON).

### Extension Points (implements/extends ExtensionPoint) — 72

- `Ghidra/Debug/Debugger-api/src/main/java/ghidra/debug/api/action/AutoMapSpec.java`
- `Ghidra/Debug/Debugger-api/src/main/java/ghidra/debug/api/action/AutoReadMemorySpecFactory.java`
- `Ghidra/Debug/Debugger-api/src/main/java/ghidra/debug/api/action/LocationTrackingSpecFactory.java`
- `Ghidra/Debug/Debugger-api/src/main/java/ghidra/debug/api/emulation/EmulatorFactory.java`
- `Ghidra/Debug/Debugger-rmi-trace/src/main/java/ghidra/debug/spi/tracermi/TraceRmiLaunchOpinion.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/disassemble/DisassemblyInject.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/register/DebuggerRegisterColumnFactory.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/timeoverview/TimeOverviewColorService.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/mapping/DebuggerPlatformOpinion.java`
- `Ghidra/Debug/Framework-TraceModeling/src/main/java/ghidra/trace/model/target/info/TraceObjectInterfaceFactory.java`
- `Ghidra/Extensions/SampleTablePlugin/src/main/java/ghidra/examples/FunctionAlgorithm.java`
- `Ghidra/Extensions/SleighDevTools/src/main/java/ghidra/app/util/disassemble/ExternalDisassembler.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/analysis/validator/PostAnalysisValidator.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/checksums/ChecksumAlgorithm.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/overview/OverviewColorService.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/reloc/RelocationFixupHandler.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/script/GhidraScriptProvider.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/services/Analyzer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/services/DataTypeReferenceFinder.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/bin/format/coff/relocation/CoffRelocationHandler.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/bin/format/dwarf/funcfixup/DWARFFunctionFixup.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/bin/format/elf/extend/ElfExtension.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/bin/format/elf/info/ElfInfoProducer.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/bin/format/elf/relocation/ElfRelocationHandler.java`
- `Ghidra/Features/Base/src/main/java/ghidra/app/util/bin/format/macho/relocation/MachoRelocationHandler.java`
- … and 47 more (see JSON).

### GhidraScripts in Java (extends GhidraScript) — 356

- `Ghidra/Debug/Debugger-api/src/main/java/ghidra/debug/flatapi/FlatDebuggerAPI.java`
- `Ghidra/Debug/Debugger-rmi-trace/ghidra_scripts/ConnectTraceRmiScript.java`
- `Ghidra/Debug/Debugger-rmi-trace/ghidra_scripts/ListenTraceRmiScript.java`
- `Ghidra/Debug/Debugger-rmi-trace/ghidra_scripts/TerminalGhidraScript.java`
- `Ghidra/Debug/Debugger/ghidra_scripts/AddMapping.java`
- `Ghidra/Debug/Debugger/ghidra_scripts/ComputeUnwindInfoScript.java`
- `Ghidra/Debug/Debugger/ghidra_scripts/DemoDebuggerScript.java`
- `Ghidra/Debug/Debugger/ghidra_scripts/PopulateDemoTrace.java`
- `Ghidra/Debug/Debugger/ghidra_scripts/RefreshRegistersScript.java`
- `Ghidra/Extensions/Jython/src/main/java/ghidra/jython/JythonScript.java`
- `Ghidra/Extensions/Lisa/ghidra_scripts/LisaLaunchScript.java`
- `Ghidra/Extensions/Lisa/ghidra_scripts/Lisa_ResolveX86orX64LinuxSyscallsScript.java`
- `Ghidra/Extensions/MachineLearning/ghidra_scripts/FindFunctionsRFExampleScript.java`
- `Ghidra/Extensions/MachineLearning/ghidra_scripts/TurnOffFuncStartSearch.java`
- `Ghidra/Extensions/SleighDevTools/ghidra_scripts/CompareSleighExternal.java`
- `Ghidra/Extensions/SleighDevTools/ghidra_scripts/GNUDisassembleBlockScript.java`
- `Ghidra/Extensions/bundle_examples/scripts_lib/IntraBundleExampleScript.java`
- `Ghidra/Extensions/bundle_examples/scripts_lib_user/InterBundleExampleScript.java`
- `Ghidra/Extensions/bundle_examples/scripts_uses_jar/UsesJarExampleScript.java`
- `Ghidra/Extensions/bundle_examples/scripts_uses_jar_version/UsesJarByVersionExampleScript.java`
- `Ghidra/Extensions/bundle_examples/scripts_with_activator/ActivatorExampleScript.java`
- `Ghidra/Extensions/bundle_examples/scripts_with_manifest/InterBundleManifestExampleScript.java`
- `Ghidra/Features/BSim/ghidra_scripts/AddProgramToH2BSimDatabaseScript.java`
- `Ghidra/Features/BSim/ghidra_scripts/CompareBSimSignaturesScript.java`
- `Ghidra/Features/BSim/ghidra_scripts/CompareExecutablesScript.java`
- … and 331 more (see JSON).

### Docking Actions (DockingAction subclasses / new) — 359

- `Ghidra/Debug/Debugger-isf/src/main/java/ghidra/app/plugin/core/datamgr/actions/ExportToIsfAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/disassemble/AbstractTraceDisassembleAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/disassemble/CurrentPlatformTraceDisassembleAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/DebuggerResources.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/InvokeActionEntryAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/DebuggerBreakpointMarkerPlugin.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/DebuggerBreakpointsProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/timeline/BreakpointTimelineActions.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/timeline/BreakpointTimelineProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/console/DebuggerConsoleProvider.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/control/TargetDockingAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memview/actions/ZoomInAAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memview/actions/ZoomInTAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memview/actions/ZoomOutAAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/memview/actions/ZoomOutTAction.java`
- `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/tracecalltree/TraceCallTreeProvider.java`
- `Ghidra/Extensions/Jython/src/main/java/ghidra/jython/JythonPlugin.java`
- `Ghidra/Extensions/MachineLearning/src/main/java/ghidra/machinelearning/functionfinding/CreateFunctionsAction.java`
- `Ghidra/Extensions/MachineLearning/src/main/java/ghidra/machinelearning/functionfinding/DisassembleFunctionStartsAction.java`
- `Ghidra/Extensions/MachineLearning/src/main/java/ghidra/machinelearning/functionfinding/ShowSimilarStartsAction.java`
- `Ghidra/Extensions/SampleTablePlugin/src/main/java/ghidra/examples/SampleTableProvider.java`
- `Ghidra/Extensions/SampleTablePlugin/src/main/java/ghidra/examples2/SampleSearchTablePlugin.java`
- `Ghidra/Extensions/bundle_examples/scripts_with_activator/ActivatorExampleScript.java`
- `Ghidra/Extensions/sample/src/main/java/ghidra/examples/HelloProgramPlugin.java`
- `Ghidra/Extensions/sample/src/main/java/ghidra/examples/HelloWorldComponentProvider.java`
- … and 334 more (see JSON).

## Processor / Sleigh definitions

Processor support is added as whole modules under `Ghidra/Processors`.
These file counts indicate the size of that surface.

### .slaspec (Sleigh spec) — 150

- `Ghidra/Framework/Emulation/src/test/resources/mock.slaspec`
- `Ghidra/Processors/6502/data/languages/6502.slaspec`
- `Ghidra/Processors/6502/data/languages/65c02.slaspec`
- `Ghidra/Processors/68000/data/languages/68020.slaspec`
- `Ghidra/Processors/68000/data/languages/68030.slaspec`
- `Ghidra/Processors/68000/data/languages/68040.slaspec`
- `Ghidra/Processors/68000/data/languages/coldfire.slaspec`
- `Ghidra/Processors/8048/data/languages/8048.slaspec`
- `Ghidra/Processors/8051/data/languages/80251.slaspec`
- `Ghidra/Processors/8051/data/languages/80390.slaspec`
- `Ghidra/Processors/8051/data/languages/8051.slaspec`
- `Ghidra/Processors/8051/data/languages/cip-51.slaspec`
- `Ghidra/Processors/8051/data/languages/mx51.slaspec`
- `Ghidra/Processors/8085/data/languages/8085.slaspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64.slaspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64BE.slaspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64_AppleSilicon.slaspec`
- `Ghidra/Processors/ARM/data/languages/ARM4_be.slaspec`
- `Ghidra/Processors/ARM/data/languages/ARM4_le.slaspec`
- `Ghidra/Processors/ARM/data/languages/ARM4t_be.slaspec`
- `Ghidra/Processors/ARM/data/languages/ARM4t_le.slaspec`
- `Ghidra/Processors/ARM/data/languages/ARM5_be.slaspec`
- `Ghidra/Processors/ARM/data/languages/ARM5_le.slaspec`
- `Ghidra/Processors/ARM/data/languages/ARM5t_be.slaspec`
- `Ghidra/Processors/ARM/data/languages/ARM5t_le.slaspec`
- … and 125 more (see JSON).

### .sinc (Sleigh include) — 210

- `Ghidra/Processors/68000/data/languages/68000.sinc`
- `Ghidra/Processors/8051/data/languages/80251.sinc`
- `Ghidra/Processors/8051/data/languages/8051_main.sinc`
- `Ghidra/Processors/8051/data/languages/mx51.sinc`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64_AMXext.sinc`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64_base_PACoptions.sinc`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64base.sinc`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64instructions.sinc`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64ldst.sinc`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64neon.sinc`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64sve.sinc`
- `Ghidra/Processors/ARM/data/languages/ARM.sinc`
- `Ghidra/Processors/ARM/data/languages/ARMTHUMBinstructions.sinc`
- `Ghidra/Processors/ARM/data/languages/ARM_CDE.sinc`
- `Ghidra/Processors/ARM/data/languages/ARMinstructions.sinc`
- `Ghidra/Processors/ARM/data/languages/ARMneon.sinc`
- `Ghidra/Processors/ARM/data/languages/ARMv8.sinc`
- `Ghidra/Processors/Atmel/data/languages/avr32a_arithmetic_operations.sinc`
- `Ghidra/Processors/Atmel/data/languages/avr32a_autogen.sinc`
- `Ghidra/Processors/Atmel/data/languages/avr32a_bit_operations.sinc`
- `Ghidra/Processors/Atmel/data/languages/avr32a_coprocessor_interface.sinc`
- `Ghidra/Processors/Atmel/data/languages/avr32a_data_transfer.sinc`
- `Ghidra/Processors/Atmel/data/languages/avr32a_dsp_operations.sinc`
- `Ghidra/Processors/Atmel/data/languages/avr32a_dsp_operations2.sinc`
- `Ghidra/Processors/Atmel/data/languages/avr32a_instruction_flow.sinc`
- … and 185 more (see JSON).

### .pspec (processor spec) — 90

- `Ghidra/Framework/Emulation/src/test/resources/mock.pspec`
- `Ghidra/Processors/6502/data/languages/6502.pspec`
- `Ghidra/Processors/68000/data/languages/68000.pspec`
- `Ghidra/Processors/8048/data/languages/8048.pspec`
- `Ghidra/Processors/8051/data/languages/80251.pspec`
- `Ghidra/Processors/8051/data/languages/8051.pspec`
- `Ghidra/Processors/8051/data/languages/cip-51.pspec`
- `Ghidra/Processors/8051/data/languages/mx51.pspec`
- `Ghidra/Processors/8085/data/languages/8085.pspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64.pspec`
- `Ghidra/Processors/ARM/data/languages/ARMCortex.pspec`
- `Ghidra/Processors/ARM/data/languages/ARM_v45.pspec`
- `Ghidra/Processors/ARM/data/languages/ARMt.pspec`
- `Ghidra/Processors/ARM/data/languages/ARMtTHUMB.pspec`
- `Ghidra/Processors/ARM/data/languages/ARMt_v45.pspec`
- `Ghidra/Processors/ARM/data/languages/ARMt_v6.pspec`
- `Ghidra/Processors/Atmel/data/languages/atmega256.pspec`
- `Ghidra/Processors/Atmel/data/languages/avr32a.pspec`
- `Ghidra/Processors/Atmel/data/languages/avr8.pspec`
- `Ghidra/Processors/Atmel/data/languages/avr8xmega.pspec`
- `Ghidra/Processors/BPF/data/languages/BPF.pspec`
- `Ghidra/Processors/CP1600/data/languages/CP1600.pspec`
- `Ghidra/Processors/CR16/data/languages/CR16.pspec`
- `Ghidra/Processors/DATA/data/languages/data.pspec`
- `Ghidra/Processors/Dalvik/data/languages/Dalvik_Base.pspec`
- … and 65 more (see JSON).

### .cspec (compiler spec) — 112

- `Ghidra/Framework/Emulation/src/test/resources/mock.cspec`
- `Ghidra/Processors/6502/data/languages/6502.cspec`
- `Ghidra/Processors/68000/data/languages/68000.cspec`
- `Ghidra/Processors/68000/data/languages/68000_register.cspec`
- `Ghidra/Processors/8048/data/languages/8048.cspec`
- `Ghidra/Processors/8051/data/languages/80251.cspec`
- `Ghidra/Processors/8051/data/languages/80390.cspec`
- `Ghidra/Processors/8051/data/languages/8051.cspec`
- `Ghidra/Processors/8051/data/languages/8051_archimedes.cspec`
- `Ghidra/Processors/8051/data/languages/keil-cx51.cspec`
- `Ghidra/Processors/8051/data/languages/mx51.cspec`
- `Ghidra/Processors/8085/data/languages/8085.cspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64.cspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64_apple.cspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64_golang.cspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64_ilp32.cspec`
- `Ghidra/Processors/AARCH64/data/languages/AARCH64_win.cspec`
- `Ghidra/Processors/ARM/data/languages/ARM.cspec`
- `Ghidra/Processors/ARM/data/languages/ARM_apcs.cspec`
- `Ghidra/Processors/ARM/data/languages/ARM_v45.cspec`
- `Ghidra/Processors/ARM/data/languages/ARM_win.cspec`
- `Ghidra/Processors/Atmel/data/languages/avr32a.cspec`
- `Ghidra/Processors/Atmel/data/languages/avr8egcc.cspec`
- `Ghidra/Processors/Atmel/data/languages/avr8gcc.cspec`
- `Ghidra/Processors/Atmel/data/languages/avr8iarV1.cspec`
- … and 87 more (see JSON).

## Script trees

**Script directories (37):**

- `Ghidra/Debug/Debugger-rmi-trace/ghidra_scripts`
- `Ghidra/Debug/Debugger/ghidra_scripts`
- `Ghidra/Extensions/Jython/ghidra_scripts`
- `Ghidra/Extensions/Lisa/ghidra_scripts`
- `Ghidra/Extensions/MachineLearning/ghidra_scripts`
- `Ghidra/Extensions/SleighDevTools/ghidra_scripts`
- `Ghidra/Features/BSim/ghidra_scripts`
- `Ghidra/Features/Base/developer_scripts`
- `Ghidra/Features/Base/ghidra_scripts`
- `Ghidra/Features/BytePatterns/ghidra_scripts`
- `Ghidra/Features/Decompiler/ghidra_scripts`
- `Ghidra/Features/DecompilerDependent/ghidra_scripts`
- `Ghidra/Features/FileFormats/developer_scripts`
- `Ghidra/Features/FileFormats/ghidra_scripts`
- `Ghidra/Features/FunctionID/ghidra_scripts`
- `Ghidra/Features/GnuDemangler/ghidra_scripts`
- `Ghidra/Features/MicrosoftCodeAnalyzer/ghidra_scripts`
- `Ghidra/Features/MicrosoftDemangler/developer_scripts`
- `Ghidra/Features/MicrosoftDmang/developer_scripts`
- `Ghidra/Features/PDB/developer_scripts`
- `Ghidra/Features/PDB/ghidra_scripts`
- `Ghidra/Features/PyGhidra/ghidra_scripts`
- `Ghidra/Features/Swift/ghidra_scripts`
- `Ghidra/Features/SystemEmulation/ghidra_scripts`
- `Ghidra/Features/VersionTracking/developer_scripts`
- … and 12 more (see JSON).

**Python scripts under script trees (25):** shown in JSON report.

## Scan errors

None — all matched source files were read successfully.

## Uncertainty notes

- **Heuristic, not authoritative.** Patterns match declarations like
  `extends AbstractAnalyzer` or `@PluginInfo`. Unusual declarations
  (generics, multi-interface `implements`, indirection through base
  classes) may be over- or under-counted.
- **Base/abstract/test/example classes are included.** e.g. an
  `Abstract...Analyzer` or a `TestAnalyzer` counts as a candidate.
- **`implements ... Analyzer` / `... Loader`** uses a loose match and
  may catch unrelated interfaces ending in those words.
- **`new DockingAction`** counts anonymous-action usages, so the docking
  action count reflects *usage sites*, not distinct action classes.
- **Counts can shift** when upstream Ghidra is re-synced; regenerate the
  report after any sync.
- This report is advisory input for Phase 2+ planning, not a contract.

