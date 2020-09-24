#!/usr/bin/env bash
set -euo pipefail

mvn clean install
echo OK

# Generate an HTML file to include in geeneral documentation
DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs/${PROJECT_NAME}
${SCRIPTS_PATH}/convertAdocToHtml.sh docs minimal.adoc ${DESTINATION_PATH}
