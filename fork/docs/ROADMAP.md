# ROADMAP.md — Fork phases

This roadmap sequences the fork's work into phases. Each phase is a branch
(`fork/phaseN-*`; see `fork/docs/UPSTREAM.md`). Phases are deliberately additive and
ordered so that risk increases gradually — docs and inventory first, runtime features
later, optional AI last. **Do not start a phase until the previous one is approved.**

Guiding constraints (from `fork/docs/ADR/0001-fork-boundaries.md`): plugins / scripts
/ analyzers / extensions first; docs and tooling before core patches; feature flags
for experiments; no AI auto-apply; no cloud AI by default.

---

## Phase 0 — Baseline / docs / dev workflow  ✅ (this phase)

Establish the foundation. No product features.

- Fork identity (`FORK.md`).
- Recorded baseline (`FORK_BASELINE.md`).
- Upstream sync plan (`UPSTREAM.md`).
- Build notes (`BUILD-NOTES.md`).
- Architecture notes / first-pass repo map (`ARCHITECTURE-NOTES.md`).
- This roadmap.
- ADR 0001 — fork boundaries.
- Bootstrap scripts (`scripts/bootstrap.sh`, `scripts/bootstrap.ps1`).

**Exit criteria:** docs reviewed, branch pushed, no core source touched.

---

## Phase 1 — Extension inventory

Know exactly what ships in the tree and where new things plug in.

- **Static source scanner** — walks the tree and identifies plugins, analyzers,
  loaders, exporters, scripts, and processor modules (no runtime needed).
- **Generated extension surface report** — a machine- and human-readable inventory
  (e.g. JSON + Markdown) of every extension point found.
- **Plugin / analyzer skeleton notes** — document the minimal template for adding a
  new plugin and a new analyzer, grounded in the real `sample`/`bundle_examples`
  modules.

**Why first:** resolves the "exact file paths" open questions in
`ARCHITECTURE-NOTES.md` before any feature is built.

---

## Phase 2 — Diagnostics plugin

A read-only-ish plugin that observes and records, without changing analysis results.

- **Analyzer timing** — per-analyzer wall-clock.
- **Decompiler timing** — per-function decompile time, *if* the API exposes it.
- **Memory snapshots** — periodic memory/heap sampling.
- **Local JSONL logs** — structured, append-only logs written locally (no network).

**Constraint:** observation only; must not alter analysis behavior. Feature-flagged.

---

## Phase 3 — Headless automation

Batch / scripted analysis built on `analyzeHeadless`.

- **`analyze-one`** — analyze a single binary end to end.
- **`analyze-folder`** — recurse a directory of binaries.
- **Export** — strings, functions, imports (to JSON/CSV).
- **Triage reports** — a per-binary summary suitable for quick review.

**Constraint:** scripts and headless wrappers; no core edits.

---

## Phase 4 — First analyzer

A real analyzer, shipped as an extension, proving the analyzer path.

- **`BinaryEntropyAnalyzer`** — the flagship.
- **Memory-block entropy** — compute entropy per memory block / region.
- **Bookmarks / comments** — surface high-entropy regions as bookmarks and comments.
- **Report export** — emit an entropy report alongside the program.

**Constraint:** registered through the stock analyzer framework; additive module.

---

## Phase 5 — Workflow UX plugins

Quality-of-life features for day-to-day RE work.

- **Command palette** — fast keyboard-driven command access.
- **Analysis presets** — saved analyzer configurations.
- **Script Manager improvements** — usability upgrades.
- **Call graph export** — export call graphs in a portable format.
- **P-Code explorer** — inspect P-Code for selected instructions/functions.

**Constraint:** UI lives in plugins/docking actions; default behavior stays stock.
(UI features are explicitly deferred until earlier phases are approved.)

---

## Phase 6 — Optional local AI tooling

Strictly opt-in, local-first AI assistance. Last on purpose.

- **Function summarizer** — natural-language summary of a function.
- **Rename suggestions** — proposed symbol names.
- **Type hints** — proposed types.
- **Local-first provider** — runs against a local model by default.
- **Explicit user review** — suggestions are *proposals*; the user applies them.
- **No silent cloud calls** — nothing leaves the machine without explicit, informed
  opt-in.

**Hard rules (from ADR 0001):** no AI auto-apply; no cloud AI by default; every
suggestion is reviewable before it changes the program.

---

## Sequencing rules

- One phase per `fork/phaseN-*` branch; risky spikes on `experiment/*`.
- Each phase must keep all Phase 0 compatibility guarantees intact.
- Core edits (if ever) follow the ADR + compatibility-test + rollback-plan rule.
- **No phase starts without explicit approval of the prior phase.**
