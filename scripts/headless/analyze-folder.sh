#!/usr/bin/env bash
#
# analyze-folder.sh — import and analyze a folder of binaries with Ghidra headless.
#
# Uses analyzeHeadless recursive import. Ghidra's loaders decide what is loadable;
# unrecognized files are handled by Ghidra (this wrapper does not pre-filter, but
# see -x to exclude obvious noise via an extra analyzeHeadless arg).
#
# Usage:
#   analyze-folder.sh -i <folder> -p <project_dir> -n <project_name> [-r <report_dir>] [-d] [-- <extra analyzeHeadless args>]
#
#   -i  input folder (required)
#   -p  project directory (required)
#   -n  project name (required)
#   -r  report/output directory (optional; created if missing)
#   -d  dry-run: print the command but do not execute
#   -h  show this help
#
# Anything after the parsed options is passed straight through to analyzeHeadless.

set -euo pipefail
# shellcheck source=scripts/headless/common.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

usage() { sed -n '2,20p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; }

INPUT="" PROJECT_DIR="" PROJECT_NAME="" REPORT_DIR="" DRYRUN=0
while getopts ":i:p:n:r:dh" opt; do
	case "${opt}" in
		i) INPUT="${OPTARG}" ;;
		p) PROJECT_DIR="${OPTARG}" ;;
		n) PROJECT_NAME="${OPTARG}" ;;
		r) REPORT_DIR="${OPTARG}" ;;
		d) DRYRUN=1 ;;
		h) usage; exit 0 ;;
		:) die "Option -${OPTARG} requires an argument (try -h)." ;;
		\?) die "Unknown option -${OPTARG} (try -h)." ;;
	esac
done
shift $((OPTIND - 1))
EXTRA=( "$@" )

[[ -n "${INPUT}" ]]        || { usage; die "Missing -i <folder>."; }
[[ -n "${PROJECT_DIR}" ]]  || { usage; die "Missing -p <project_dir>."; }
[[ -n "${PROJECT_NAME}" ]] || { usage; die "Missing -n <project_name>."; }

find_analyze_headless
require_input_dir "${INPUT}"

# Count candidate files (informational only; recursion is delegated to analyzeHeadless).
file_count="$(find "${INPUT}" -type f 2>/dev/null | wc -l | tr -d ' ')"
log "Input folder '${INPUT}' contains ${file_count} file(s) (recursive)."

cmd=( "${ANALYZE_HEADLESS}" "${PROJECT_DIR}" "${PROJECT_NAME}" -import "${INPUT}" -recursive )
if (( ${#EXTRA[@]} )); then
	cmd+=( "${EXTRA[@]}" )
fi

if (( DRYRUN )); then
	log "Dry-run: not executing. Command that would run:"
	print_cmd "${cmd[@]}"
	exit 0
fi

ensure_out_dir "${PROJECT_DIR}"
[[ -n "${REPORT_DIR}" ]] && ensure_out_dir "${REPORT_DIR}"

print_cmd "${cmd[@]}"
"${cmd[@]}"
log "analyze-folder complete: project '${PROJECT_NAME}' in ${PROJECT_DIR}"
