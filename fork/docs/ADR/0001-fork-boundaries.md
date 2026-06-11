# ADR 0001 — Preserve Ghidra compatibility through additive fork extensions

- **Status:** Accepted
- **Date:** 2026-06-11
- **Phase:** 0 (baseline)
- **Supersedes:** —
- **Superseded by:** —

## Context

This repository is a private downstream fork of NSA Ghidra
(`MalloyTheDev/Reverse-Enginnering`). The intent is to grow a serious
reverse-engineering research and workflow platform on top of Ghidra while still being
able to pull in future upstream Ghidra releases.

Two forces are in tension:

1. **We want to add a lot** — diagnostics, an extension inventory, headless
   automation, analyzer packs, UX helpers, and eventually optional local AI tooling.
2. **We must not break Ghidra, and we must stay re-syncable with upstream.** Ghidra's
   core (decompiler C++, ProgramDB, the Sleigh compiler, the on-disk project/database
   format) is correctness-critical, hard to test in isolation, and central to every
   workflow. Editing upstream-owned files also creates merge conflicts that make
   re-syncing painful and make the fork hard to audit.

Left unmanaged, "just a small core tweak" accumulates until the fork can no longer
track upstream and its behavior diverges from stock Ghidra in ways nobody fully
understands. We need an explicit boundary, decided once, up front.

## Decision

**All fork functionality is delivered as additive extensions to Ghidra — new
plugins, analyzers, loaders, exporters, scripts, extension modules, Gradle helpers,
docs, and feature-flagged experiments — rather than as edits to Ghidra's core.**

Core Ghidra behavior is preserved by default. The compatibility surface that must
keep working unchanged is: project loading, program import, disassembly,
decompilation, analyzers, plugins, scripts, Sleigh processors, PyGhidra, headless
mode, and extensions (see `fork/docs/FORK_BASELINE.md`).

A change to an upstream-owned source file (a "core patch") is treated as an exceptional
event, not a normal one, and is gated behind the rules below.

## Rules

1. **Plugins / scripts / analyzers / extensions first.** New capability must be
   attempted as an additive extension before any other approach. New files in new
   locations are strongly preferred over edits to existing files.
2. **Docs / tooling before core patches.** Document and script around a limitation
   before changing core to remove it. Many goals are reachable with zero core edits.
3. **Feature flags for experiments.** Anything experimental is gated so the **default**
   behavior is stock Ghidra. Experiments live on `experiment/*` branches and/or behind
   flags; they never change default behavior.
4. **No AI auto-apply.** AI-generated output (summaries, renames, type hints) is always
   a *proposal* presented for explicit user review. The fork never silently mutates a
   program based on AI output.
5. **No cloud AI by default.** AI tooling is local-first. No request leaves the machine
   without explicit, informed, opt-in consent. There are no silent network calls.
6. **No core patch without ADR + validation + rollback.** Any edit to an upstream-owned
   source file — especially in the danger zones (decompiler C++, ProgramDB internals,
   Sleigh compiler internals, project/database on-disk format, package-wide renames,
   mass refactors/reformatting) — requires, before it lands:
   - a dedicated **ADR** describing the change and why an additive approach was
     insufficient;
   - a **compatibility test** demonstrating the preserved behaviors still hold;
   - a documented **rollback plan**.

## Consequences

**Positive**

- Upstream re-syncs stay cheap: `git diff fork-baseline-vanilla` is mostly *new files*,
  not conflicting edits.
- The fork is auditable — what's "ours" vs. "stock" is obvious by location.
- Stock Ghidra compatibility is protected by construction, not by vigilance.
- Risky areas are fenced off, so early development is low-stakes.

**Negative / costs**

- Some features are harder or slower to build additively than they would be with a
  quick core edit; we accept that cost in exchange for re-syncability and safety.
- A few desirable changes may be impractical without a core patch; those are not
  forbidden, but they carry real overhead (ADR + test + rollback) by design.
- Feature flags and extension scaffolding add some surface area of their own.

**Neutral**

- Establishes the `fork/`-namespaced docs area, the `scripts/` bootstrap area, the
  `fork/phaseN-*` / `experiment/*` branch model, and the `fork-baseline-vanilla` tag as
  standing conventions.

## References

- `fork/docs/FORK.md` — fork identity and danger zones.
- `fork/docs/FORK_BASELINE.md` — recorded baseline and compatibility goals.
- `fork/docs/UPSTREAM.md` — upstream remote, baseline tag, branch strategy.
- `fork/docs/ROADMAP.md` — phased plan that operationalizes these boundaries.
