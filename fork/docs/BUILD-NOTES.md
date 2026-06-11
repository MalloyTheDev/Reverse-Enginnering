# BUILD-NOTES.md — Building this fork

These notes capture how to build the fork. They are derived from
`Ghidra/application.properties`, the top-level `DevGuide.md`, and `build.gradle`.
Nothing here installs software — installing toolchains is the operator's
responsibility.

> The standard build is a **full Ghidra build**. Do not run it casually in a
> constrained or shared environment. See "Cloud / remote environment limitations"
> below.

## Required tools (from `Ghidra/application.properties`)

| Tool | Requirement | Notes |
| --- | --- | --- |
| **Java (JDK)** | **21** | `application.java.min=21`, `application.java.compiler=21`, no max. A 64-bit JDK is required. |
| **Gradle** | **8.5+** | `application.gradle.min=8.5`, no max. Prefer the bundled wrapper (`./gradlew`) which pins a known-good Gradle version. |
| **Python** | **3.9 – 3.14** | `application.python.supported=3.14, 3.13, 3.12, 3.11, 3.10, 3.9`. Needed for PyGhidra and Python scripting. |

> A working internet connection (or a pre-populated dependency cache) is required for
> the dependency-fetch step.

## Step 0 — make the Gradle wrapper executable (if needed)

On a fresh checkout the wrapper may not have the execute bit set
(observed at baseline: `./gradlew: Permission denied`). Fix once:

```bash
chmod +x ./gradlew
```

`scripts/bootstrap.sh` does this for you.

## Step 1 — fetch dependencies

Ghidra fetches non-Maven dependencies via an init script before the main build:

```bash
./gradlew -I gradle/support/fetchDependencies.gradle -DhideDownloadProgress -DnoEclipse
```

- `-I gradle/support/fetchDependencies.gradle` — runs the dependency-fetch init script.
- `-DhideDownloadProgress` — quieter logs (no per-byte progress).
- `-DnoEclipse` — skip Eclipse-related setup (we are not using the Eclipse PDE flow here).

On an **offline** machine you can pre-download on a connected box and point the build
at a local gradle home (see `DevGuide.md`, e.g. `gradle -g dependencies/gradle ...`).

## Step 2 — main build

```bash
./gradlew buildGhidra --parallel
```

- `buildGhidra` — assembles the full Ghidra distribution.
- `--parallel` — uses parallel project execution to speed up the build.

Output lands under `build/dist/` as a zipped distribution.

> ⚠️ This is the heavy build. **Do not run it unless explicitly approved** for this
> environment. (Phase 0 deliberately does not run it.)

## Inspecting available tasks

To discover what tasks exist (per module and aggregate):

```bash
./gradlew tasks --all
```

During Phase 0 validation this output is captured to `fork/docs/gradle-tasks.txt`
for reference.

## Test tasks (to verify later)

From `DevGuide.md`, the relevant test/report tasks are:

| Task | Purpose |
| --- | --- |
| `./gradlew unitTestReport` | Run unit tests and produce a report. |
| `./gradlew integrationTest` | Run integration tests. |
| `./gradlew combinedTestReport` | Combine unit + integration test results into one report. |

Other useful tasks seen in `DevGuide.md`:

| Task | Purpose |
| --- | --- |
| `./gradlew buildNatives` | Build native components (decompiler, etc.). |
| `./gradlew sleighCompile` | Compile Sleigh processor specifications. |
| `./gradlew buildPyPackage` | Build the PyGhidra Python package. |
| `./gradlew prepdev cleanEclipse eclipse` | Prepare a dev/Eclipse environment. |

These are recorded here as the canonical verification commands for later phases; they
are **not** run in Phase 0.

## Local timing notes

> Fill in as builds are actually run on real hardware. Keep this honest — it informs
> whether CI / cloud runs are even feasible.

| Date | Machine / cores / RAM | Task | Wall-clock time | Notes |
| --- | --- | --- | --- | --- |
| _TBD_ | _TBD_ | `fetchDependencies` | _TBD_ | |
| _TBD_ | _TBD_ | `buildGhidra --parallel` | _TBD_ | |
| _TBD_ | _TBD_ | `buildNatives` | _TBD_ | |
| _TBD_ | _TBD_ | `unitTestReport` | _TBD_ | |

## Cloud / remote environment limitations

The Phase 0 work was done in an **ephemeral, isolated cloud container**. Constraints
that affect building here:

- **No package installation.** apt / sudo / brew / choco / winget / pip-installs of
  system tooling are **off limits** in this fork's workflow. The toolchain must
  already be present.
- **Ephemeral filesystem.** The container is reclaimed after inactivity; only
  committed-and-pushed work survives. Build outputs under `build/` do **not** persist.
- **Network policy may restrict outbound access.** `fetchDependencies` needs network
  (or a warm cache); it may fail or be slow under a restrictive policy.
- **Resource limits.** A full `buildGhidra` is CPU/RAM/IO heavy and may exceed the
  container's limits or time budget. Treat the cloud env as suitable for *docs,
  scripts, inspection, and light Gradle queries* — not for full builds — unless
  explicitly approved.
- **Wrapper execute bit.** As noted, `./gradlew` may arrive non-executable; bootstrap
  fixes it.

Bottom line: in this environment, prefer `./gradlew tasks --all` and `--version`-style
queries. Run `buildGhidra` only on adequately-resourced hardware (or with explicit
approval).
