# ForkWorkflowUX (fork Phase 5)

Workflow-UX plugins for the MalloyRE fork, translated from the MalloyRE design system into
**Ghidra-native** Java/Swing (Plugins, ComponentProviders, DockingActions, the Theme system).
No web/React code; no design-system assets are vendored into the repo.

> Plan & design mapping: `fork/docs/PHASE5.md`.

## Phase 5A — Entropy Findings view (read-only)

`EntropyFindingsPlugin` contributes a dockable **Entropy Findings** window: a sortable,
filterable table of the program's Binary Entropy findings (the bookmarks written by the Phase-4
`BinaryEntropyAnalyzer`), with **entropy heat-scale** cell rendering and double-click navigation.

- **Read-only.** It only reads existing "Binary Entropy" bookmarks; it never modifies the program.
- **Heat scale.** The Entropy column is colored by six fork-owned theme colors
  (`color.fork.ux.entropy.0..5`), drawn from the design system's entropy scale. These are
  additive ids used only by this view — stock Ghidra appearance is unchanged.
- **Navigation.** Double-click / Enter goes to the finding's address via the tool's navigation
  service.
- **Empty state** explains the next action: enable the “Binary Entropy (fork)” analyzer and run
  analysis.

### Using it

1. Build/install the extension (and the ForkEntropy extension, to produce findings).
2. Add the plugin: **File → Configure → (Fork Workflow UX)**, enable *Entropy Findings view (fork)*.
3. Run the Binary Entropy analyzer on a program, then open **Window → Entropy Findings**.
4. Sort by entropy, filter, and double-click a row to navigate. Use the toolbar **Refresh** to
   re-pull after re-analyzing.

## Structure

```
src/main/java/ghidra/fork/ux/
  ForkUxPluginPackage.java          # groups fork UX plugins in Configure
  theme/ForkUxColors.java           # GColor accessors for the scoped fork palette
  entropy/
    EntropyHeatScale.java           # dependency-free bucket + comment-parse logic (unit-tested)
    EntropyFinding.java             # row object
    EntropyFindingsTableModel.java  # AddressBasedTableModel over Binary Entropy bookmarks
    EntropyFindingsProvider.java    # dockable ComponentProvider (table + navigation)
    EntropyFindingsPlugin.java      # ProgramPlugin wiring
data/forkux.theme.properties        # scoped fork color ids
```

Later sub-phases (diagnostics viewer, report viewer, status surfaces, command palette) add their
own packages under `ghidra.fork.ux.*` in this same module.
