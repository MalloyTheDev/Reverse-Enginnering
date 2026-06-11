#!/usr/bin/env bash
#
# common.sh — shared helpers for the fork headless wrappers.
#
# SOURCE this file from the other wrappers; it is not meant to be run directly.
# It locates analyzeHeadless, validates inputs, and prepares output directories.
#
# Design: GHIDRA_INSTALL_DIR is the clean path. A source checkout is NOT an
# installed Ghidra distribution, so analyzeHeadless may only exist after building
# or extracting a release. These helpers therefore fail cleanly rather than
# pretending source-tree execution is guaranteed.

set -euo pipefail

# Directory of this script and the repo root (scripts/headless/common.sh -> repo root).
FORK_HEADLESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${FORK_HEADLESS_DIR}/../.." && pwd)"

# Where the fork-owned GhidraScript files live (passed to analyzeHeadless -scriptPath).
FORK_SCRIPT_PATH="${REPO_ROOT}/fork/headless/ghidra_scripts"

# Populated by find_analyze_headless.
ANALYZE_HEADLESS=""

log() { printf '[fork-headless] %s\n' "$*" >&2; }
die() { printf '[fork-headless] ERROR: %s\n' "$*" >&2; exit 1; }

# Print the exact command (safely quoted) that is about to run, to stderr.
print_cmd() {
	{ printf '[fork-headless] command:\n  '; printf '%q ' "$@"; printf '\n'; } >&2
}

# Locate analyzeHeadless, setting the ANALYZE_HEADLESS global. Order:
#   1) $GHIDRA_INSTALL_DIR/support/analyzeHeadless  (the clean, supported path)
#   2) build/dist/*/support/analyzeHeadless          (auto-detected built dist)
# Fails cleanly (exit) if neither is found.
find_analyze_headless() {
	if [[ -n "${GHIDRA_INSTALL_DIR:-}" ]]; then
		local cand="${GHIDRA_INSTALL_DIR%/}/support/analyzeHeadless"
		[[ -f "${cand}" ]] || die "GHIDRA_INSTALL_DIR='${GHIDRA_INSTALL_DIR}' is set, but '${cand}' was not found. Point GHIDRA_INSTALL_DIR at an installed Ghidra distribution."
		ANALYZE_HEADLESS="${cand}"
		log "Using analyzeHeadless from GHIDRA_INSTALL_DIR: ${cand}"
		return 0
	fi

	# No GHIDRA_INSTALL_DIR: try to auto-detect a locally-built distribution.
	shopt -s nullglob
	local matches=( "${REPO_ROOT}"/build/dist/*/support/analyzeHeadless )
	shopt -u nullglob
	if (( ${#matches[@]} > 0 )); then
		ANALYZE_HEADLESS="${matches[0]}"
		log "Auto-detected built distribution analyzeHeadless: ${ANALYZE_HEADLESS}"
		return 0
	fi

	die "Could not locate analyzeHeadless. Set GHIDRA_INSTALL_DIR to an installed Ghidra (so \$GHIDRA_INSTALL_DIR/support/analyzeHeadless exists), or build a distribution under build/dist/. See fork/docs/HEADLESS.md."
}

require_input_file() {
	[[ -n "${1:-}" ]] || die "No input binary given."
	[[ -e "$1" ]] || die "Input binary not found: $1"
	[[ -f "$1" ]] || die "Input binary is not a regular file: $1"
}

require_input_dir() {
	[[ -n "${1:-}" ]] || die "No input folder given."
	[[ -d "$1" ]] || die "Input folder not found (or not a directory): $1"
}

# Create a directory if needed. Never deletes anything.
ensure_out_dir() {
	[[ -n "${1:-}" ]] || die "No output directory given."
	mkdir -p "$1" || die "Could not create output directory: $1"
}

# Verify the fork GhidraScript path exists (used by export-triage).
require_fork_scripts() {
	[[ -d "${FORK_SCRIPT_PATH}" ]] || die "Fork script path not found: ${FORK_SCRIPT_PATH}"
}
