#!/usr/bin/env bash
set -euo pipefail

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=src/test/docs
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs/${PROJECT_NAME}

GENERATE_HTML="yes"
while getopts ":t" opt; do
   case ${opt} in
     t ) GENERATE_HTML="no"
       ;;
   esac
done

function remove_docs_directories() {
  rm -rf ${DOCS_PATH}
}

# Generate all documentation for all module of project.
function generate_docs() {
  remove_docs_directories

  # 'no-assert' avoid to check diff on each test. That's not seem to build significantly faster with this option.
  # The main advantage is that the build do not break, and we can have a result for all modules.
  mvn clean test -Dno-assert
  mvn org.codehaus.mojo:exec-maven-plugin:java
  if [[ "$GENERATE_HTML" == "yes" ]];
  then
    mvn org.asciidoctor:asciidoctor-maven-plugin:process-asciidoc
  fi
}

# Check file differences
function check_file_differences() {
  echo ""
  echo ---------------------
  OPTIND=1
  DEMO_RESULT=$(source ${SCRIPTS_PATH}/checkDocInFolder.sh ${DOCS_PATH})
  echo "$DEMO_RESULT"
  echo ---------------------
  if [[ "$DEMO_RESULT" =~ "FAILURES:" ]]
  then
    exit 1
  fi
}

source ${SCRIPTS_PATH}/loadWritingFunction.sh
generate_docs
#echo -n "Build ${PROJECT_NAME}: "
#write_success "OK"
check_file_differences
#${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} Documentation.adoc ${DESTINATION_PATH}
