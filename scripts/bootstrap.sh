#!/usr/bin/env bash
#
# bootstrap.sh — Phase 0 developer bootstrap for the Ghidra fork.
#
# SAFE BY DESIGN:
#   - Does NOT install anything (no apt/sudo/brew/pip/etc.).
#   - Does NOT run a full build.
#   - Only inspects the environment and makes ./gradlew executable.
#   - Idempotent: safe to run repeatedly.
#
# It prints repo/version info and the recommended (but NOT executed) build commands.

set -euo pipefail

# --- locate repo root (one level up from this script) -----------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

# --- helpers ----------------------------------------------------------------
hr()  { printf '%s\n' "------------------------------------------------------------"; }
say() { printf '%s\n' "$*"; }

hr
say "Ghidra fork — Phase 0 bootstrap"
say "Repo root: ${REPO_ROOT}"
hr

# --- repo / version info ----------------------------------------------------
APP_PROPS="Ghidra/application.properties"
if [[ -f "${APP_PROPS}" ]]; then
  say "Found ${APP_PROPS}:"
  # Print the key fields if present; tolerate missing keys.
  while IFS= read -r line; do
    case "${line}" in
      application.name=*|application.version=*|application.release.name=*|\
      application.gradle.min=*|application.java.min=*|application.java.compiler=*|\
      application.python.supported=*)
        say "  ${line}"
        ;;
    esac
  done < "${APP_PROPS}"
else
  say "WARNING: ${APP_PROPS} not found — are you in the repo root?"
fi
hr

# --- Java -------------------------------------------------------------------
if command -v java >/dev/null 2>&1; then
  say "Java (required: JDK 21):"
  java -version 2>&1 | sed 's/^/  /' || true
else
  say "Java: not found on PATH (JDK 21 required to build)."
fi
hr

# --- Python -----------------------------------------------------------------
if command -v python3 >/dev/null 2>&1; then
  say "Python (supported: 3.9 - 3.14):"
  python3 --version 2>&1 | sed 's/^/  /' || true
elif command -v python >/dev/null 2>&1; then
  say "Python (supported: 3.9 - 3.14):"
  python --version 2>&1 | sed 's/^/  /' || true
else
  say "Python: not found on PATH (3.9 - 3.14 needed for PyGhidra/scripting)."
fi
hr

# --- Gradle wrapper ---------------------------------------------------------
if [[ -f "./gradlew" ]]; then
  say "Found ./gradlew."
  if [[ ! -x "./gradlew" ]]; then
    chmod +x ./gradlew
    say "  -> made ./gradlew executable (chmod +x)."
  else
    say "  -> ./gradlew is already executable."
  fi
else
  say "WARNING: ./gradlew not found in repo root."
fi
hr

# --- recommended commands (printed, NOT executed) ---------------------------
say "Recommended next steps (run manually; this script does NOT run them):"
say ""
say "  1) Fetch dependencies:"
say "     ./gradlew -I gradle/support/fetchDependencies.gradle -DhideDownloadProgress -DnoEclipse"
say ""
say "  2) Full build (heavy — only on adequate hardware / with approval):"
say "     ./gradlew buildGhidra --parallel"
say ""
say "  3) Inspect available tasks:"
say "     ./gradlew tasks --all"
hr
say "Bootstrap complete. No packages installed, no build run."
