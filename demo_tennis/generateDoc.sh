#!/usr/bin/env bash
set -euo pipefail

PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=../scripts
DOCS_PATH=src/test/docs
DESTINATION_PATH=../docs/${PROJECT_NAME}

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
  rm -rf ${DOCS_PATH}
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
  echo ""
  echo ---------------------
  DEMO_RESULT=$(source ${SCRIPTS_PATH}/checkDocInFolder.sh ${DOCS_PATH})
  echo "$DEMO_RESULT"
  echo ---------------------
}

source ${SCRIPTS_PATH}/loadWritingFunction.sh
generate_docs
check_file_differences
source ${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} Documentation.adoc ${DESTINATION_PATH}
