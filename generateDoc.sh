#!/usr/bin/env bash
oldstate="$(set +o)"
set -euo pipefail

source ./scripts/loadWritingFunction.sh

# Validation mode: git or approvals
# With approvals: file .approved is compared to .received (no need to have git). It not verifies removed tests
# with git: file .approved is compared with git commited version. It detects tests removed.
VALIDATION_MODE="git"
OUTPUT_LOG=tmp/generateDoc.log

# Usage info
function show_help() {
  echo "Usage: ${0##*/} [-h] [-m VALIDATION_MODE] [FILE]..."
  echo "Build all the project: compile, generate documentation, verify there is no regression"
  echo "and convert asciidoctor generated to Html."
  echo ""
  echo "    -h                  display this help and exit."
  echo "    -m VALIDATION_MODE: could be 'git' or 'approvals'."
}

while getopts ":hm:g" opt; do
   case ${opt} in
     h ) show_help
       exit 0
       ;;
     m ) VALIDATION_MODE=${OPTARG}
       ;;
     \? ) show_help
       exit 0
       ;;
   esac
done

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
  for DEMO_NAME in $ALL_DEMOS
  do
      #echo "---------------------"
      echo -n "Project ${DEMO_NAME}: "
      pushd $DEMO_NAME > /dev/null
      returncode=0
      ./generateDoc.sh > "$OUTPUT_LOG" 2>&1 || returncode=$?


      if [[ returncode -eq 0 ]]
      then
        DEMO_STATUS="OK"
        TEST_COLOR=${GREEN}
      else
        ALL_STATUS_RESULT="${RED}FAILED${NO_COLOR}"
        DEMO_STATUS="FAILED"
        TEST_COLOR=${RED}
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
