# PLUGIN-SKELETON-NOTES.md — How Ghidra plugins are shaped

Phase 1 note. This captures the **conceptual** structure of a Ghidra plugin and points
at real examples found by `tools/extension-inventory.py`. **No plugin is generated
here.** Phase 2 will use these notes to plan the diagnostics plugin.

> All paths below were located by the static scanner. They are real files in this
> tree; confirm details against them before writing code.

## What a Ghidra plugin is

A **plugin** is a unit of behavior loaded into a Ghidra *tool* (e.g. the CodeBrowser).
Plugins add commands, services, listeners, and (optionally) UI. They are discovered by
Ghidra's `ClassSearcher` and described by a `@PluginInfo` annotation. By convention a
plugin class name ends in `Plugin`.

Two common base classes:

- **`Plugin`** — the general base for any tool plugin.
- **`ProgramPlugin`** — a `Plugin` specialization with convenient hooks for the
  active program / location / selection. Most feature plugins extend this.

## Where existing examples were found

Plugins (`extends Plugin` / `ProgramPlugin`, or `@PluginInfo`) — 237 candidates.
Representative, readable examples:

- `Ghidra/Features/GhidraGo/src/main/java/ghidra/app/plugin/core/go/GhidraGoPlugin.java`
  — carries a full `@PluginInfo` annotation.
- `Ghidra/Features/PDB/src/main/java/pdb/PdbPlugin.java` — `@PluginInfo` example.
- `Ghidra/Features/ProgramGraph/src/main/java/ghidra/graph/program/ProgramGraphPlugin.java`
- `Ghidra/Features/FunctionID/src/main/java/ghidra/feature/fid/plugin/FidPlugin.java`
- `Ghidra/Features/PyGhidra/src/main/java/ghidra/pyghidra/PyGhidraPlugin.java`

For UI surfaces and actions, the scanner also found:

- **Component providers** (UI docking windows) — 262 candidates (`extends
  ComponentProvider` / `ComponentProviderAdapter` / `DialogComponentProvider`).
- **Docking actions** — 359 candidates (`extends DockingAction` and `new
  DockingAction` usage sites).

The richest, most copyable example density is under
`Ghidra/Features/Base/.../ghidra/app/plugin/core/...`, which is the natural place to
study the plugin/action/provider triad together.

## Minimal conceptual structure

A typical feature plugin is composed of these pieces (only the first is mandatory):

1. **Plugin class** — `class FooPlugin extends ProgramPlugin`, annotated with
   `@PluginInfo(...)` (status, package, category, shortDescription, description, and
   any provided/consumed services). Class name ends in `Plugin`.
2. **PluginTool integration** — the plugin receives the `PluginTool` in its
   constructor; it registers actions/providers and wires listeners through it. Program
   lifecycle is handled via `ProgramPlugin` hooks (e.g. `programActivated`).
3. **DockingAction (if it adds commands)** — actions are created (often via an action
   builder or by subclassing `DockingAction`) and added to the tool to appear in
   menus/toolbars/context menus.
4. **ComponentProvider (if it has UI)** — a dockable window is implemented as a
   `ComponentProvider` (or `…Adapter`) and registered with the tool; transient dialogs
   use `DialogComponentProvider`.
5. **Module location** — the plugin lives inside a module, conventionally under
   `Ghidra/Features/<Module>/src/main/java/...` (or a new module under
   `Ghidra/Extensions/<Module>`), packaged like the existing modules. New, additive
   modules are preferred over edits to existing ones (see ADR 0001).

## Notes for Phase 2 (diagnostics plugin)

- A diagnostics plugin can likely be **observation-only**: a `ProgramPlugin` plus a
  `ComponentProvider` to display readings, with no mutation of analysis results.
- Study a small `@PluginInfo` plugin (e.g. `GhidraGoPlugin`) and one component
  provider before designing the diagnostics UI.
- Keep it in a **new module** (additive) and behind a feature flag, per ADR 0001.
- **Do not build it yet** — this is planning input only.
