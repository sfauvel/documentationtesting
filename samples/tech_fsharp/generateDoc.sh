#!/usr/bin/env bash
set -euo pipefail

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=docs
DESTINATION_PATH=${DOC_PROJECT_PATH}/tmp/docs/${PROJECT_NAME}
DOCKER_IMAGE=mcr.microsoft.com/dotnet/sdk
GITHUB_REPO=https://github.com/sfauvel/documentationtesting/tree/master

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

  if [[ -z $DOCKER_IMAGE || $IS_WINDOWS = true ]]
  then
    pushd ${WORKING_FOLDER}
    ${COMMAND}
    popd
  else
    docker run \
      -v $(pwd):/project \
      -w /project/${WORKING_FOLDER} \
      -it $DOCKER_IMAGE \
      ${COMMAND}
  fi
}

function generate_main_documentation_file() {
  ADOC_FILES=$(ls -1 ${DOCS_PATH})

  CURRENT_PATH=$(pwd)
  ROOT_FILE=index.adoc
  DOC=${DOCS_PATH}/${ROOT_FILE}
  echo ":toc: left
:nofooter:
:description: Example in FSharp.

== FSharp examples

View source project on link:${GITHUB_REPO}${CURRENT_PATH/$(realpath $DOC_PROJECT_PATH)/}[Github]
" > ${DOC}

  for FILENAME in $ADOC_FILES
  do
    if [[ ! $FILENAME == $ROOT_FILE ]]
    then
      echo "include::${FILENAME}[leveloffset=+2]" >> ${DOC}

      SOURCE_FILENAME=${FILENAME%%.*}; SOURCE_FILENAME=${SOURCE_FILENAME:1}.fs
      echo "[%collapsible]

.Code used to generate this doc
====
----
include::../src/${SOURCE_FILENAME}[]
----
====" >> ${DOC}

    fi
  done
}

# Generate all documentation for all module of project.
function generate_docs() {
  execute_command src "dotnet test"
  generate_main_documentation_file

}

# Check file differences
function check_file_differences() {
    NB_RECEIVED=$(find docs -name "*.received" | wc -l)
    if [[ $NB_RECEIVED -gt 0 ]]
    then
      exit 1
    fi
}

source ${SCRIPTS_PATH}/loadWritingFunction.sh
echo "============================="

IMAGE_ID=$(docker images -q $DOCKER_IMAGE)
if [[ $IMAGE_ID ]]
then
  generate_docs
  check_file_differences
else
  echo "No '$DOCKER_IMAGE' docker image. Uses already generated '.adoc' files."
fi
#${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} Documentation.adoc ${DESTINATION_PATH}
if [[ "$GENERATE_HTML" == "yes" ]];
then
  mvn org.asciidoctor:asciidoctor-maven-plugin:process-asciidoc
fi