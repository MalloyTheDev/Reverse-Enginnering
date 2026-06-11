# ForkDiagnostics (fork Phase 2 MVP)

Observation-only diagnostics extension for this private downstream Ghidra fork. It is
the first fork-specific plugin and exists to prove that fork functionality can be added
safely as a standard, additive Ghidra extension module.

> Full design notes and rationale: `fork/docs/DIAGNOSTICS.md`.

## What it does

When **explicitly enabled**, the plugin appends JSON-lines records to a local log file
for these events:

- `plugin_constructed`
- `program_activated`
- `program_deactivated`
- `plugin_disposed`

Each record includes a UTC timestamp, the Java runtime version, a JVM memory snapshot
(max/total/free/used), and — when a program is active — its name, project path,
language id, and compiler-spec id.

## Enabling it (disabled by default)

It is **off** unless a flag is set:

- Environment variable: `GHIDRA_FORK_DIAGNOSTICS=true`
- or system property: `-Dghidra.fork.diagnostics=true`

It also requires the usual extension opt-in: install the extension and add the
**Fork diagnostics (observation-only)** plugin to your tool (File → Configure).

## Log location

- Default: `fork/logs/diagnostics.jsonl` (relative to the working directory; good for
  repo/dev runs).
- Override: `-Dghidra.fork.diagnostics.logfile=/absolute/path/diagnostics.jsonl`
  (recommended for installed distributions, where the working directory may not be
  writable — use a user-writable location such as one under your home/config dir).

## Safety properties

- **Disabled by default**; double opt-in (extension + plugin + flag).
- **No UI** (MVP), **no network**, **no analysis mutation** — reads metadata only.
- **Never disruptive** — all logging failures are swallowed internally.

## Structure

- `src/main/java/ghidra/fork/diagnostics/ForkDiagnosticsLog.java` — dependency-free
  logger core (flag detection, memory snapshot, JSONL build + append).
- `src/main/java/ghidra/fork/diagnostics/ForkDiagnosticsPlugin.java` — thin
  `ProgramPlugin` that forwards lifecycle events to the logger.
- `src/test/java/ghidra/fork/diagnostics/ForkDiagnosticsLogTest.java` — unit tests for
  the logger core.
