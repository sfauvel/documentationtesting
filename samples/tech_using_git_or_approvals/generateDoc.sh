#!/usr/bin/env bash
set -euo pipefail

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=src/test/docs
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs/${PROJECT_NAME}

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

function remove_docs_directories() {
  rm -rf ${DOCS_PATH}
}

# Check file differences
function check_file_differences() {
  echo ""
  echo ---------------------
  DEMO_RESULT=$(source ${SCRIPTS_PATH}/checkDocInFolder.sh ${DOCS_PATH})
  echo "$DEMO_RESULT"
  echo ---------------------
  if [[ "$DEMO_RESULT" =~ "FAILURES:" ]]
  then
    exit 1
  fi
}

source ${SCRIPTS_PATH}/loadWritingFunction.sh
if [ $VALIDATION_MODE = "git" ]
then
  # delete docs directories to check files not regenerated because of a removed test.
  remove_docs_directories
  # 'no-assert' avoid to check diff on each test. That's not seem to build significantly faster with this option.
  # The main advantage is that the build do not break, and we can have a result for all modules.
  mvn clean install package -q -Dno-assert -Dapproved_with=$VALIDATION_MODE
  echo -n "Build ${PROJECT_NAME}: "
  write_success "OK"
  check_file_differences
else
  # Do not remove approved file with approvals because it's the reference
  mvn clean install package -q -Dapproved_with=$VALIDATION_MODE
fi
${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} Documentation.adoc ${DESTINATION_PATH}
