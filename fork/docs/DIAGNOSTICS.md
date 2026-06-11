# DIAGNOSTICS.md — Fork diagnostics (Phase 2)

Phase 2 deliverable. This documents the **observation-only diagnostics MVP** and the
reasoning behind it. The implementation lives in the additive extension module
`Ghidra/Extensions/ForkDiagnostics/`.

## Goal of Phase 2

Prove that this fork can add a fork-specific plugin **safely** — as a standard,
additive Ghidra extension — without touching Ghidra core, changing default behavior, or
taking on risk. The diagnostics feature itself is intentionally minimal: local logging
first, UI later.

## What was built

A no-UI plugin that, **only when enabled**, appends JSONL records on plugin/program
lifecycle events.

| Piece | File | Notes |
| --- | --- | --- |
| Logger core | `Ghidra/Extensions/ForkDiagnostics/src/main/java/ghidra/fork/diagnostics/ForkDiagnosticsLog.java` | **No Ghidra dependency** — compilable/testable standalone. Flag check, memory snapshot, JSONL build + append. |
| Plugin bridge | `.../ForkDiagnosticsPlugin.java` | Thin `ProgramPlugin`; forwards lifecycle events to the logger. |
| Unit tests | `src/test/java/.../ForkDiagnosticsLogTest.java` | JSON/escape/flag logic. |
| Module scaffold | `build.gradle`, `Module.manifest`, `extension.properties`, `certification.manifest`, `README.md` | Modeled on `Ghidra/Extensions/sample`. |

## Why `Ghidra/Extensions/ForkDiagnostics/`

`settings.gradle` calls `includeProjects('Ghidra/Extensions')`, and `includeProjects`
(in `gradle/support/settingsUtil.gradle`) auto-discovers every `*/build.gradle` one
level down. A new module under `Ghidra/Extensions/` is therefore **auto-included with
zero edits to `settings.gradle` or any existing file** — the maximally-additive choice,
and the precedent for future fork plugins/analyzers. (See ADR-0001.)

## Feature flag (disabled by default)

The plugin does nothing observable unless explicitly enabled:

- Environment variable: `GHIDRA_FORK_DIAGNOSTICS=true`
- or system property: `-Dghidra.fork.diagnostics=true`

Truthy values: `true`, `1`, `yes`, `on` (case-insensitive). Anything else = disabled.

This is **double opt-in**: an extension is not loaded into a tool by default, so the
user must (1) install the extension, (2) add the *Fork diagnostics (observation-only)*
plugin to a tool, and (3) set the flag. With the flag unset, default Ghidra behavior is
completely unchanged.

## Log output

- **Format:** one JSON object per line (JSONL), append-only.
- **Default path:** `fork/logs/diagnostics.jsonl`, relative to `user.dir`. Good for
  repo / development runs. The repo ignores generated logs via `fork/logs/.gitignore`.
- **Override:** `-Dghidra.fork.diagnostics.logfile=/absolute/path.jsonl`.
- **Installed-distribution note:** in an installed Ghidra, `user.dir` may not be
  writable. For that scenario, set `ghidra.fork.diagnostics.logfile` to a user-writable
  location (e.g. under the user's home or config directory). A future iteration may
  default to such a path automatically; the MVP keeps the dev-friendly default and
  documents the override.

### Record fields

| Field | Meaning |
| --- | --- |
| `event` | `plugin_constructed`, `program_activated`, `program_deactivated`, `plugin_disposed` |
| `timestamp_utc` | ISO-8601 instant |
| `java_runtime_version` | `java.runtime.version` |
| `max_memory` / `total_memory` / `free_memory` / `used_memory` | JVM `Runtime` snapshot (bytes) |
| `active_program_name` | active program name, or `null` |
| `active_program_path` | active program project path, or `null` |
| `active_program_language` | language id (e.g. `x86:LE:64:default`), or `null` |
| `active_program_compiler` | compiler-spec id (e.g. `gcc`), or `null` |
| `note` | present when context is unavailable |

Example line:

```json
{"event":"program_activated","timestamp_utc":"2026-06-11T09:00:00Z","java_runtime_version":"21.0.10+7","max_memory":2147483648,"total_memory":536870912,"free_memory":268435456,"used_memory":268435456,"active_program_name":"hello","active_program_path":"/Test/hello","active_program_language":"x86:LE:64:default","active_program_compiler":"gcc"}
```

## Safety properties (by construction)

- **Disabled by default**; no effect on stock Ghidra unless explicitly enabled.
- **Observation-only** — reads metadata via public APIs; never mutates a program or
  analysis state.
- **Local-only** — a single local file; no network, ever.
- **Never disruptive** — every logging failure is swallowed inside the logger; it cannot
  throw into Ghidra.
- **Additive** — a new module; no edits to Ghidra core or to existing build files.

## Explicitly deferred (do not implement without approval)

- **Analyzer timing.** Deferred. It should be done only through clean public hooks
  (e.g. an analysis-options/`AutoAnalysisManager` listener if one is exposed), not by
  patching the analysis pipeline. Path to investigate, not yet built.
- **Decompiler timing.** Deferred — must avoid invasive decompiler hooks / C++.
- **UI dashboard / ComponentProvider.** Deferred (UI later).
- **Headless integration, rotation/retention, structured schema versioning.** Deferred.
- **Anything AI or network-related.** Out of scope by fork policy.

## Build / validation status

- The **logger core** (`ForkDiagnosticsLog`) has no Ghidra dependency and was compiled
  with `javac` (Java 21) and exercised directly in this environment; emitted lines parse
  as JSON and the disabled-flag path correctly writes nothing.
- The **plugin** (`ForkDiagnosticsPlugin`) and the **JUnit test** depend on the Ghidra
  classpath / JUnit and are **not** compiled in this environment, because the Gradle
  dependency repo has not been fetched here. To build/test the full module later:

  ```bash
  # one-time: fetch dependencies (downloads a large set; run on adequate hardware)
  ./gradlew -I gradle/support/fetchDependencies.gradle -DhideDownloadProgress -DnoEclipse
  # then compile/test just this module:
  ./gradlew :ForkDiagnostics:compileJava :ForkDiagnostics:test
  ```

  (No full `buildGhidra` is required to validate this module.)
