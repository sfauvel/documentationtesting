#!/usr/bin/env bash
oldstate="$(set +o)"
set -euo pipefail

date

source ./scripts/loadWritingFunction.sh

# Validation mode: git or approvals
# With approvals: file .approved is compared to .received (no need to have git). It not verifies removed tests
# with git: file .approved is compared with git commited version. It detects tests removed.
ROOT_PATH=$(pwd)
OUTPUT_PATH=$ROOT_PATH/tmp
OUTPUT_LOG=$OUTPUT_PATH/generateDoc.log
GLOBAL_EXIT_VALUE=0
OPTIONS=""

# Usage info
function show_help() {
  echo "Usage: ${0##*/} [-h] [-t] [FILE]..."
  echo "Build all the project: compile, generate documentation, verify there is no regression"
  echo "and convert asciidoctor generated to Html."
  echo ""
  echo "    -h                  display this help and exit."
  echo "    -t                  only tests."
}

while getopts ":ht" opt; do
   case ${opt} in
     h ) show_help
       exit 0
       ;;
     t ) OPTIONS="$OPTIONS -t"
       ;;
     \? ) show_help
       exit 0
       ;;
   esac
done

shift $((OPTIND-1))

function restore_shell_options() {
  set +u
  HISTIGNORE_BACKUP="$HISTIGNORE"
  set -u
  HISTIGNORE='set [\+\-]o *'
  eval "$oldstate"
  HISTIGNORE="$HISTIGNORE_BACKUP"
}

function generate_docs() {
  ALL_DEMOS=$1
  ALL_RESULTS=""
  ALL_STATUS_RESULT="${GREEN}OK${NO_COLOR}"
  local TEST_COLOR
  echo "Generate projects documentation..."
  rm -f "$OUTPUT_LOG"
  mkdir -p "$OUTPUT_PATH"
  for DEMO_NAME in $ALL_DEMOS
  do
      echo -n "Project ${DEMO_NAME}: "
      echo "---------------------" >> "$OUTPUT_LOG"
      echo "Project ${DEMO_NAME} ${OPTIONS}" >> "$OUTPUT_LOG"
      pushd $DEMO_NAME > /dev/null
      returncode=0
      ./generateDoc.sh $OPTIONS >> "$OUTPUT_LOG" 2>&1 || returncode=$?

      if [[ returncode -eq 0 ]]
      then
        DEMO_STATUS="OK"
        TEST_COLOR=${GREEN}
      else
        ALL_STATUS_RESULT="${RED}FAILED${NO_COLOR}"
        DEMO_STATUS="FAILED"
        GLOBAL_EXIT_VALUE=1
        TEST_COLOR=${RED}
        cat $OUTPUT_LOG
      fi

      echo -e  "${TEST_COLOR}${DEMO_STATUS}${NO_COLOR}"

      ALL_RESULTS="$ALL_RESULTS${TEST_COLOR}- ${DEMO_NAME}: ${DEMO_STATUS}${NO_COLOR}\n"
      popd > /dev/null
  done

  echo ---------------------
  echo Results:
  echo ---------------------
  echo -e "$ALL_RESULTS"

  echo ---------------------
  echo -e "$ALL_STATUS_RESULT"
  echo ---------------------

}

set +u
if [ -z "$1" ]
then
  MODULES="documentationtesting $(find samples -maxdepth 1 -name "demo_*") $(find samples -maxdepth 1 -name "tech_*") documentationtestingdoc"
else
  MODULES="$1"
fi
set -u

generate_docs "$MODULES"

restore_shell_options

date

exit $GLOBAL_EXIT_VALUE