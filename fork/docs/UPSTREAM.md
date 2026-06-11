# UPSTREAM.md — Tracking and syncing with NSA Ghidra

This fork tracks **NSA Ghidra** as its upstream. The goal is to stay re-syncable: we
want pulling a new Ghidra release to be a routine operation, not a merge crisis. That
is only possible if fork changes stay **additive** and out of upstream files.

## 1. Add official Ghidra as `upstream`

`origin` is this private downstream repo (`MalloyTheDev/Reverse-Enginnering`). Add
NSA Ghidra as a second remote named `upstream`:

```bash
git remote add upstream https://github.com/NationalSecurityAgency/ghidra.git
git fetch upstream --tags
```

Verify:

```bash
git remote -v
# origin    ...MalloyTheDev/Reverse-Enginnering (fetch/push)
# upstream  https://github.com/NationalSecurityAgency/ghidra.git (fetch/push)
```

> Note: only run `git remote add upstream ...` once. If it already exists, use
> `git remote set-url upstream https://github.com/NationalSecurityAgency/ghidra.git`.

## 2. Create a pristine baseline tag

Before layering fork work on top, tag the pristine imported baseline so there is
always a clean reference point to diff against and to roll back to:

```bash
git tag fork-baseline-vanilla
# optionally push the tag to origin:
git push origin fork-baseline-vanilla
```

Anything reachable from `fork-baseline-vanilla` is "stock as imported". A simple
`git diff fork-baseline-vanilla --stat` then shows exactly what the fork has added or
touched — which is the fastest way to audit that the fork is still additive.

## 3. Branch strategy

| Branch / pattern | Purpose |
| --- | --- |
| `main` | Imported baseline / current **stable** fork. |
| `fork/phase0-*` | Baseline + developer-workflow foundation (this phase). |
| `fork/phase1-*` | Extension inventory work. |
| `fork/phase2-*` | Diagnostics work. |
| `fork/phaseN-*` | Subsequent roadmap phases. |
| `experiment/*` | Risky, isolated ideas that may never land. |

The current working branch for this phase is **`fork/phase0-baseline-devx`**.

Each phase gets its own `fork/phaseN-*` branch; risky spikes go on `experiment/*` so
they can be abandoned without polluting the phase branches.

## 4. Syncing in a new upstream release

Rough flow (adapt as needed):

```bash
git fetch upstream --tags
git checkout main
git merge upstream/master        # or merge a specific release tag
# resolve conflicts — ideally none, because fork changes are additive
```

If conflicts touch upstream files, that is a signal the fork drifted into core edits
and should be reconsidered (see rules below).

## 5. Rules

These rules exist to keep upstream re-sync cheap and the fork auditable:

1. **Keep fork changes additive wherever possible.** Prefer adding files over
   editing existing upstream files.
2. **Prefer new files / modules / plugins over core edits.** A new plugin, analyzer,
   script, extension, or `fork/`-namespaced file never conflicts with upstream.
3. **Core edits require an ADR, a compatibility test, and a rollback plan.** No
   exceptions. See `fork/docs/ADR/0001-fork-boundaries.md`. A "core edit" is any
   modification to an upstream-owned source file, especially in the danger zones
   listed in `FORK.md` (decompiler C++, ProgramDB internals, Sleigh compiler
   internals, on-disk project/database format).

Following these rules means most `git diff fork-baseline-vanilla` output is *new
files under `fork/`, `scripts/`, and new modules* — never edits to Ghidra core.
