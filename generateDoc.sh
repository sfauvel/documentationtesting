#!/usr/bin/env bash
set -euo pipefail

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

function generate_demos() {
  ALL_RESULTS=""
  echo -n "Generate Html"
  for DEMO_NAME in  $(ls | grep "demo_*")
  do
      pushd $DEMO_NAME
      DEMO_RESULT=$(source ./generateDoc.sh)
      echo "$DEMO_RESULT"

      DEMO_STATUS="${DEMO_RESULT##*$'\n'}" # Last line
      ALL_RESULTS="$ALL_RESULTS- ${DEMO_NAME}: ${DEMO_STATUS}\n"
      popd
  done

  echo ---------------------
  echo "$DEMO_RESULT"
  echo ---------------------

  echo Results:
  echo -e "$ALL_RESULTS"
}


function generate_general_doc() {
  pushd documentationtestingdoc
  mvn clean install package -Dnoassert -Dapproved_with=$VALIDATION_MODE
  popd

  scripts/convertAdocToHtml.sh documentationtestingdoc/target/classes/docs demo.adoc ./docs
}

generate_demos
generate_general_doc
