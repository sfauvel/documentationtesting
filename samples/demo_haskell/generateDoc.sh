#!/usr/bin/env bash
set -euo pipefail

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=docs
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs/${PROJECT_NAME}
DOCKER_IMAGE=haskell_dev

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

function execute_on_docker() {

  local WORKING_FOLDER=$1
  local COMMAND=$2

  docker run \
    -v $(pwd):/project \
    -w /project/${WORKING_FOLDER} \
    -it ${DOCKER_IMAGE} \
    ${COMMAND}
}

function remove_docs_directories() {
  execute_on_docker . "rm -rf ${DOCS_PATH}"
}

function generate_main_documentation_file() {
  ADOC_FILES=$(ls -1 ${DOCS_PATH})

  DOC=${DOCS_PATH}/Documentation.adoc
  touch ${DOC}
  echo ":toc: left" >> ${DOC}
  echo ":nofooter:" >> ${DOC}
  echo "" >> ${DOC}
  echo "= Haskell examples" >> ${DOC}
  echo "" >> ${DOC}
  for FILENAME in $ADOC_FILES
  do
    echo "== ${FILENAME%%.*}" >> ${DOC}
    echo "" >> ${DOC}
    echo "include::${FILENAME}[leveloffset=+2]" >> ${DOC}
  done
}

# Generate all documentation for all module of project.
function generate_docs() {
  # delete docs directories to check files not regenerated because of a removed test.
  # Do not remove if check with approvals
  if [ $VALIDATION_MODE = "git" ]
  then
    remove_docs_directories
    mkdir ${DOCS_PATH}
  fi

  # 'no-assert' avoid to check diff on each test. That's not seem to build significantly faster with this option.
  # The main advantage is that the build do not break, and we can have a result for all modules.

  for TEST_FILE_NAME in $(find src -maxdepth 1 -name "test_*")
  do
    DOC_NAME=${TEST_FILE_NAME/src\//}
    echo "Execute: ${DOC_NAME}"
    execute_on_docker . "ghc -o target/${DOC_NAME} -outputdir target -no-keep-hi-files -no-keep-o-files ${TEST_FILE_NAME}"
    execute_on_docker . "./target/${DOC_NAME}"
  done

  generate_main_documentation_file

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
