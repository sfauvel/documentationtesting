#!/usr/bin/env bash
set -euo pipefail

PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=../scripts
DOCS_PATH=src/test/docs
DESTINATION_PATH=../docs/${PROJECT_NAME}

function remove_docs_directories() {
  rm -rf ${DOCS_PATH}
}

# Generate all documentation for all module of project.
function generate_docs() {
  remove_docs_directories

  # 'no-assert' avoid to check diff on each test. That's not seem to build significantly faster with this option.
  # The main advantage is that the build do not break, and we can have a result for all modules.
  mvn clean install package -Dno-assert
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
${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} Documentation.adoc ${DESTINATION_PATH}
