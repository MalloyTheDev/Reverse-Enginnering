# FORK.md — What this repository is

> Phase 0 baseline document. Created on a fork of Ghidra. See `fork/docs/ROADMAP.md`
> for where this is going and `fork/docs/ADR/0001-fork-boundaries.md` for the rules
> that constrain how it gets there.

## Identity

This repository is a **private downstream fork of NSA Ghidra**. It is **not** an
upstream contribution, not a public distribution, and not a competitor to Ghidra.
It tracks Ghidra as its base and layers additional research and workflow tooling
on top.

- Upstream / origin of code: **NSA Ghidra** (National Security Agency).
- This downstream repo: **MalloyTheDev/Reverse-Enginnering**.
- Status: private development fork.

## Goal

The goal of this fork is to grow into a **serious reverse-engineering research and
workflow platform** built on top of Ghidra — without giving up the ability to track
and re-sync with upstream Ghidra.

Concretely, the fork aims to add, over time:

- Diagnostics (analyzer/decompiler timing, memory snapshots, structured logs).
- An **extension inventory** — a way to enumerate the plugins, analyzers, loaders,
  exporters, and scripts that ship in (or get added to) the tree.
- **Headless automation** helpers (analyze one binary, analyze a folder, export
  strings/functions/imports, produce triage reports).
- **Analyzer packs** (e.g. an entropy analyzer) that surface findings as bookmarks,
  comments, and exportable reports.
- **UX / workflow helpers** (command palette, analysis presets, script-manager
  quality-of-life, call-graph export, a P-Code explorer).
- **Optional, local-first AI suggestions** (function summaries, rename ideas, type
  hints) — strictly opt-in, reviewed by the user, and never silently calling the
  cloud.

These are *future* directions. Phase 0 (this document set) only establishes the
baseline, the upstream-sync plan, build notes, an architecture map, and bootstrap
scripts. No product features are implemented yet.

## Hard constraint: preserve core Ghidra compatibility

The single most important rule of this fork is that **stock Ghidra behavior must be
preserved**. A user who installs this fork should be able to do everything they could
do with vanilla Ghidra. That means the following must keep working unchanged:

- Project create / open / load.
- Program import and loaders.
- Disassembly.
- Decompilation.
- Auto-analysis and individual analyzers.
- Plugins and the plugin/tool model.
- GhidraScript (Java and Python scripts).
- Sleigh processor specifications.
- PyGhidra.
- Headless analyzer (`analyzeHeadless`).
- Extensions.

If a change in this fork would alter any of those behaviors for the worse, it does
not belong in the fork as written — it belongs behind a feature flag, in a new
module, or not at all.

## How new functionality should arrive

New capability should be **additive** and should live in the *least invasive* place
that can host it. In rough order of preference:

1. **New GhidraScripts** (Java/Python) — zero risk to the core, easy to iterate.
2. **New plugins** — self-contained, loaded into the tool, removable.
3. **New analyzers** — registered through the existing analyzer framework.
4. **New extensions / modules** — packaged like the existing `Ghidra/Extensions/*`.
5. **New Gradle helper tasks** — additive build/automation glue.
6. **New documentation** under `fork/docs/`.
7. **Feature-flagged experiments** — gated so the default behavior is stock.

Only when none of the above can host a change should a core edit even be considered,
and then only under the ADR + compatibility-test + rollback-plan rule (see the ADR).

## Areas to avoid touching early (danger zone)

These areas are high-risk: they are central to correctness, they are hard to test in
isolation, and a regression in them is both serious and hard to spot. Avoid editing
them in early phases:

- **Decompiler C++** (`Ghidra/Features/Decompiler/src/decompile/...`).
- **ProgramDB internals** and the program/database model.
- **Sleigh compiler internals** (the compiler itself — distinct from individual
  processor `.slaspec`/`.sinc` files, which are also sensitive).
- **Project / database on-disk format** — changing this breaks portability with
  stock Ghidra.
- **Package-wide renames** — these create enormous, unreviewable diffs and make
  upstream re-sync painful.
- **Mass refactors / mass reformatting** — same problem; they destroy the ability
  to cleanly merge upstream.

If one of these genuinely must change, it goes through an ADR with a compatibility
test and a documented rollback plan. There are no casual edits in the danger zone.

## Relationship to upstream

The fork intends to stay mergeable with upstream Ghidra for as long as practical.
The mechanics (adding `upstream`, fetching tags, the pristine baseline tag, and the
branch strategy) are documented in `fork/docs/UPSTREAM.md`. The guiding principle:
**keep fork changes additive and out of the way of upstream files** so that pulling
a new Ghidra release is a manageable operation rather than a merge nightmare.
