#!/usr/bin/env bash
set -euo pipefail

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=docs
DESTINATION_PATH=${DOC_PROJECT_PATH}/tmp/docs/${PROJECT_NAME}
HASKELL_DOCKER_IMAGE=haskell_dev
if [[ "$(uname)" =~ "NT" ]]; then IS_WINDOWS=true; else IS_WINDOWS=false; fi

# Usage info
function show_help() {
  echo "Usage: ${0##*/} [-h]"
  echo "Build all the project: compile, generate documentation, verify there is no regression"
  echo "and convert asciidoctor generated to Html."
  echo ""
  echo "    -h                  display this help and exit."
}

while getopts ":hm:g" opt; do
   case ${opt} in
     h ) show_help
       exit 0
       ;;
     \? ) show_help
       exit 0
       ;;
   esac
done

function execute_command() {
  local WORKING_FOLDER=$1
  local COMMAND=$2

  if [[ -z $HASKELL_DOCKER_IMAGE || $IS_WINDOWS = true ]]
  then
    pushd ${WORKING_FOLDER}
    ${COMMAND}
    popd
  else
    docker run \
      -v $(pwd):/project \
      -w /project/${WORKING_FOLDER} \
      -it $HASKELL_DOCKER_IMAGE \
      ${COMMAND}
  fi
}

function reset_docs_directories() {
  execute_command . "rm -rf ${DOCS_PATH}"
  mkdir ${DOCS_PATH}
}

function generate_main_documentation_file() {
  ADOC_FILES=$(ls -1 ${DOCS_PATH})

  DOC=${DOCS_PATH}/Documentation.adoc
  touch ${DOC}
  echo ":toc: left" >> ${DOC}
  echo ":nofooter:" >> ${DOC}
  echo ":description: Example in Haskell." >> ${DOC}
  echo "" >> ${DOC}
  echo "== Haskell examples" >> ${DOC}
  echo "" >> ${DOC}
  for FILENAME in $ADOC_FILES
  do
    echo "include::${FILENAME}[leveloffset=+2]" >> ${DOC}
  done
}

# Generate all documentation for all module of project.
function generate_docs() {
  # delete docs directories to check files not regenerated because of a removed test.
  # Do not remove if check with approvals
  reset_docs_directories

  # 'no-assert' avoid to check diff on each test. That's not seem to build significantly faster with this option.
  # The main advantage is that the build do not break, and we can have a result for all modules.

  for TEST_FILE_NAME in $(find src -maxdepth 1 -name "test_*")
  do
    DOC_NAME=${TEST_FILE_NAME/src\//}
    echo "Execute: ${DOC_NAME}"
    execute_command . "ghc -o target/${DOC_NAME} -outputdir target -no-keep-hi-files -no-keep-o-files ${TEST_FILE_NAME}"
    execute_command . "./target/${DOC_NAME}"
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
