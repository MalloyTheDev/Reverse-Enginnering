#!/usr/bin/env bash
#
# export-triage.sh — import + analyze one binary, then run the fork's read-only
# export scripts to produce a basic triage bundle.
#
# Outputs (written by the GhidraScripts into <report_dir>):
#   functions.csv  strings.csv  imports.csv  triage.md
#
# Usage:
#   export-triage.sh -i <binary> -p <project_dir> -n <project_name> -r <report_dir> [-- <extra analyzeHeadless args>]
#
#   -i  input binary (required)
#   -p  project directory (required)
#   -n  project name (required)
#   -r  report output directory (required)
#   -h  show this help
#
# Anything after the parsed options is passed straight through to analyzeHeadless.

set -euo pipefail
# shellcheck source=scripts/headless/common.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

usage() { sed -n '2,20p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; }

INPUT="" PROJECT_DIR="" PROJECT_NAME="" REPORT_DIR=""
while getopts ":i:p:n:r:h" opt; do
	case "${opt}" in
		i) INPUT="${OPTARG}" ;;
		p) PROJECT_DIR="${OPTARG}" ;;
		n) PROJECT_NAME="${OPTARG}" ;;
		r) REPORT_DIR="${OPTARG}" ;;
		h) usage; exit 0 ;;
		:) die "Option -${OPTARG} requires an argument (try -h)." ;;
		\?) die "Unknown option -${OPTARG} (try -h)." ;;
	esac
done
shift $((OPTIND - 1))
EXTRA=( "$@" )

[[ -n "${INPUT}" ]]        || { usage; die "Missing -i <binary>."; }
[[ -n "${PROJECT_DIR}" ]]  || { usage; die "Missing -p <project_dir>."; }
[[ -n "${PROJECT_NAME}" ]] || { usage; die "Missing -n <project_name>."; }
[[ -n "${REPORT_DIR}" ]]   || { usage; die "Missing -r <report_dir> (required for triage)."; }

find_analyze_headless
require_fork_scripts
require_input_file "${INPUT}"
ensure_out_dir "${PROJECT_DIR}"
ensure_out_dir "${REPORT_DIR}"

# Resolve an absolute report dir so the post-scripts write to a stable location
# regardless of analyzeHeadless's working directory.
REPORT_ABS="$(cd "${REPORT_DIR}" && pwd)"

cmd=( "${ANALYZE_HEADLESS}" "${PROJECT_DIR}" "${PROJECT_NAME}" -import "${INPUT}"
	-scriptPath "${FORK_SCRIPT_PATH}"
	-postScript ForkExportFunctions.java "${REPORT_ABS}"
	-postScript ForkExportStrings.java "${REPORT_ABS}"
	-postScript ForkExportImports.java "${REPORT_ABS}"
	-postScript ForkTriageReport.java "${REPORT_ABS}" )
if (( ${#EXTRA[@]} )); then
	cmd+=( "${EXTRA[@]}" )
fi

print_cmd "${cmd[@]}"
"${cmd[@]}"
log "export-triage complete. Reports in: ${REPORT_ABS}"
log "  functions.csv  strings.csv  imports.csv  triage.md"
