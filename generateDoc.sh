#!/usr/bin/env bash
set -euo pipefail

source ./scripts/loadWritingFunction.sh

# Validation mode: git or approvals
# With approvals: file .approved is compared to .received (no need to have git). It not verifies removed tests
# with git: file .approved is compared with git commited version. It detects tests removed.
VALIDATION_MODE="git"

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

function generate_docs() {
  ALL_DEMOS=$1
  ALL_RESULTS=""
  ALL_STATUS_RESULT="${GREEN}OK${NO_COLOR}"
  local TEST_COLOR
  echo -n "Generate Html"
  for DEMO_NAME in $ALL_DEMOS
  do
      pushd $DEMO_NAME
      returncode=0
      ./generateDoc.sh || returncode=$?

      if [[ returncode -eq 0 ]]
      then
        DEMO_STATUS="${GREEN}OK${NO_COLOR}"
        TEST_COLOR=${GREEN}
      else
        ALL_STATUS_RESULT="${RED}FAILED${NO_COLOR}"
        DEMO_STATUS="FAILED"
        TEST_COLOR=${RED}
      fi

      ALL_RESULTS="$ALL_RESULTS${TEST_COLOR}- ${DEMO_NAME}: ${DEMO_STATUS}${NO_COLOR}\n"
      popd
  done

  echo ---------------------
  echo -e "$ALL_STATUS_RESULT"
  echo ---------------------

  echo Results:
  echo -e "$ALL_RESULTS"
}

mvn -pl documentationtesting -am clean install
generate_docs "$(ls | grep "demo_*") $(ls | grep "tech_*") documentationtestingdoc"