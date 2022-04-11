#!/usr/bin/env bash
set -eo pipefail

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=docs
DESTINATION_PATH=${DOC_PROJECT_PATH}/tmp/docs/${PROJECT_NAME}
PYTHON_DOCKER_IMAGE=python:3.8.1
if [[ "$(uname)" =~ "NT" ]]; then IS_WINDOWS=true; else IS_WINDOWS=false; fi

# Usage info
function show_help() {
  echo "Usage: ${0##*/} [-h]"
  echo "Build all the project: compile, generate documentation, verify there is no regression"
  echo "and convert asciidoctor generated to Html."
  echo ""
  echo "    -h                  display this help and exit."
  echo "    -t                  tests only."
}

GENERATE_HTML="yes"
while getopts ":ht" opt; do
   case ${opt} in
     h ) show_help
       exit 0
       ;;
     t ) GENERATE_HTML="no"
       ;;
     \? ) show_help
       exit 0
       ;;
   esac
done

function execute_command() {
  local WORKING_FOLDER=$1
  local COMMAND=$2

  if [[ -z $PYTHON_DOCKER_IMAGE || $IS_WINDOWS = true ]]
  then
    pushd ${WORKING_FOLDER}
    ${COMMAND}
    popd
  else
    docker run \
      -v $(pwd):/project \
      -w /project/${WORKING_FOLDER} \
      -it $PYTHON_DOCKER_IMAGE \
      ${COMMAND}
  fi
}

function reset_docs_directories() {
  execute_command . "rm -rf ${DOCS_PATH}"
  mkdir ${DOCS_PATH}
}

function generate_main_documentation_file() {
  ADOC_FILES=$(ls -1 ${DOCS_PATH})

  DOC=${DOCS_PATH}/index.adoc
  touch ${DOC}
  echo ":toc: left" >> ${DOC}
  echo ":nofooter:" >> ${DOC}
  echo ":description: Example in Python." >> ${DOC}
  echo "" >> ${DOC}
  echo "== Python examples" >> ${DOC}
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

  execute_command src "python -m unittest"

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

IMAGE_ID=$(docker images -q $PYTHON_DOCKER_IMAGE)
if [[ $IMAGE_ID ]]
then
  generate_docs
  check_file_differences
else
  echo "No '$PYTHON_DOCKER_IMAGE' docker image. Uses already generated '.adoc' files."
fi

#${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} Documentation.adoc ${DESTINATION_PATH}
if [[ "$GENERATE_HTML" == "yes" ]];
then
  mvn org.asciidoctor:asciidoctor-maven-plugin:process-asciidoc
fi