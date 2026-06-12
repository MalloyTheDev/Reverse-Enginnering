# PHASE5.md — Workflow UX plugins

Phase 5 layers a fork "workstation" UX on top of stock Ghidra, taking the **MalloyRE design
system** as product/visual direction. The design system is a web/React + CSS-tokens system; it is
**not** vendored into the repo. Its *intent* is translated into Ghidra-native Java/Swing.

## Design-system → Ghidra translation

| Design-system concept | Ghidra-native realization | Mutates DB? |
| --- | --- | --- |
| Color/space/type tokens | `data/forkux.theme.properties` → new `GColor` ids (fork-owned; never override stock) | No |
| 6-stop entropy heat scale | theme ids `color.fork.ux.entropy.0..5` + a table cell renderer | No |
| Panel / dockable surface | `ComponentProvider` registered by a `Plugin` | No |
| Toolbar / actions | `DockingAction` (toolbar/menu/keybinding) | No |
| Command palette (⌘K) | `Plugin` + popup dialog over `tool.getAllActions()` | No |
| EntropyBar / findings | `ComponentProvider` table over Binary Entropy bookmarks + navigation | No |
| Diagnostics panel | read-only `ComponentProvider` over the ForkDiagnostics log | No |
| Export / report viewer | read-only preview + export-to-file | No (export only) |
| StatusBar | `Tool.setStatusInfo` + optional status component | No |
| Workstation layout | per-provider docking defaults; optional opt-in preset (never the default) | No |
| AI assist | excluded (no AI yet) | — |

Guardrails: additive extension module only, **no core edits**, `settings.gradle` untouched, stock
windows/actions/status untouched, **read-only by default**, opt-in plugins.

## Sub-phase roadmap (safest → riskiest)

- **5A — Theme foundation + Entropy Findings view** *(read-only)* — **in progress / this branch.**
- **5B — Diagnostics / log viewer** *(read-only)*.
- **5C — Export / report viewer** *(read-only preview + export)*.
- **5D — Workflow status surfaces** (status bar).
- **5E — Command palette** (⌘K-equivalent over `tool.getAllActions()`).
- **5F — Optional "MalloyRE workspace" preset** (opt-in docking layout; never default).

Each sub-phase = its own branch + PR + green CI.

## Module structure

One cohesive additive module `Ghidra/Extensions/ForkWorkflowUX/`, package-by-package:
`ghidra.fork.ux.theme`, `…entropy` (5A), then `…diagnostics`, `…report`, `…status`, `…palette`.

## Implementation risk ranking (low → high)

1. Theme tokens (additive ids) · 2. Entropy Findings view (read-only table) ·
3. Diagnostics viewer (read-only) · 4. Report/export viewer (adds file export) ·
5. Status surfaces (shared status bar) · 6. Command palette (invokes actions; keybindings) ·
7. Workspace layout preset (most intrusive; opt-in only).

## Phase 5A — Entropy Findings view

The first, safest slice: a read-only dockable table over the Phase-4 "Binary Entropy" bookmarks.

- `EntropyHeatScale` — dependency-free bucket mapping (`bits → 0..5`) and recovery of the entropy
  value embedded in a bookmark comment; unit-tested in isolation. Degrades to NaN/neutral on an
  unrecognized comment.
- `EntropyFindingsTableModel` — `AddressBasedTableModel` whose `doLoad` iterates the program's
  Analysis bookmarks with category `Binary Entropy`; columns: Entropy (heat-rendered), Block,
  Size, Location, Description. `getAddress(row)` drives navigation.
- `EntropyFindingsProvider` — `ComponentProviderAdapter` hosting a threaded `GhidraTable` +
  filter panel; `installNavigation(tool)` for double-click/Enter navigation; an empty-state hint;
  a toolbar **Refresh** action.
- `EntropyFindingsPlugin` — `ProgramPlugin` wiring; sets the program on activation/deactivation.
- `ForkUxColors` + `data/forkux.theme.properties` — scoped fork color ids only.
- `ForkUxPluginPackage` — groups fork UX plugins in Configure.

### Validation

- `EntropyHeatScale` unit tests (bucket/parse/format) — dependency-free.
- `:ForkWorkflowUX:compileJava`, `:ForkWorkflowUX:test`, `:ForkWorkflowUX:ip` (IP/certification).
- Regenerate the extension-point inventory (plugins +1).
- GUI behavior (table populates, heat renders, navigation) is verified by a manual smoke test in a
  built Ghidra, since Swing providers are not fully headless-testable.

### Branch

`fork/phase5a-entropy-view` (module `ForkWorkflowUX`).
