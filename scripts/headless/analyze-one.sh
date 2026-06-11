#!/usr/bin/env bash
#
# analyze-one.sh — import and analyze a single binary with Ghidra headless.
#
# Read-only with respect to your binary; it creates/updates a Ghidra project and
# runs auto-analysis. It does NOT delete projects by default.
#
# Usage:
#   analyze-one.sh -i <binary> -p <project_dir> -n <project_name> [-r <report_dir>] [-- <extra analyzeHeadless args>]
#
#   -i  input binary (required)
#   -p  project directory (required) — where the Ghidra project is created
#   -n  project name (required)
#   -r  report/output directory (optional; created if missing)
#   -h  show this help
#
# Anything after the parsed options is passed straight through to analyzeHeadless,
# e.g.:  analyze-one.sh -i a.bin -p /tmp/proj -n demo -- -noanalysis -overwrite

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
EXTRA=( "$@" )  # any remaining args (typically after --) pass through to analyzeHeadless

[[ -n "${INPUT}" ]]        || { usage; die "Missing -i <binary>."; }
[[ -n "${PROJECT_DIR}" ]]  || { usage; die "Missing -p <project_dir>."; }
[[ -n "${PROJECT_NAME}" ]] || { usage; die "Missing -n <project_name>."; }

find_analyze_headless
require_input_file "${INPUT}"
ensure_out_dir "${PROJECT_DIR}"
[[ -n "${REPORT_DIR}" ]] && ensure_out_dir "${REPORT_DIR}"

cmd=( "${ANALYZE_HEADLESS}" "${PROJECT_DIR}" "${PROJECT_NAME}" -import "${INPUT}" )
if (( ${#EXTRA[@]} )); then
	cmd+=( "${EXTRA[@]}" )
fi

print_cmd "${cmd[@]}"
"${cmd[@]}"
log "analyze-one complete: project '${PROJECT_NAME}' in ${PROJECT_DIR}"
