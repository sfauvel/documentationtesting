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

while getopts ":hm" opt; do
   case ${opt} in
     h ) show_help
       exit 0
       ;;
     m ) VALIDATION_MODE=$opt
       ;;
     \? ) show_help
       exit 0
       ;;
   esac
done

function remove_docs_directories() {
  for DEMO_NAME in  $(ls | grep "demo_*")
  do
    rm -rf $DEMO_NAME/src/test/docs
  done
}

# Generate all documentation for all module of project.
function generate_docs() {
  # delete docs directories to check files not regenerated because of a removed test.
  # Do not remove if check with approvals
  if [ $VALIDATION_MODE = "git" ]
  then
    remove_docs_directories
  fi

  # 'noassert' avoid to check diff on each test. That's not seem to build significantly faster with this option.
  # The main advantage is that the build do not break, and we can have a result for all modules.
  mvn clean install package -Dnoassert -Dapproved_with=$VALIDATION_MODE
  #mvn clean install package -Dapproved_with=$VALIDATION_MODE
}

# Check file differences
function check_file_differences() {
  ALL_RESULTS=""
  echo -n "Generate Html"
  for DEMO_NAME in  $(ls | grep "demo_*")
  do
      echo ""
      echo ---------------------
      echo "$DEMO_NAME"
      echo ---------------------
      DEMO_RESULT=$(source checkDocInFolder.sh $DEMO_NAME/src/test/docs)
      DEMO_STATUS="${DEMO_RESULT##*$'\n'}" # Last line
      echo "$DEMO_RESULT"
      ALL_RESULTS="$ALL_RESULTS- ${DEMO_NAME}: ${DEMO_STATUS}\n"
  done

  echo ""
  echo ---------------------
  echo Results:
  echo -e "$ALL_RESULTS"
}

source loadWritingFunction.sh
generate_docs
check_file_differences
source convertAdocToHtml.sh
