#!/usr/bin/env bash
set -euo pipefail

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
source ${SCRIPTS_PATH}/loadWritingFunction.sh

mvn clean install -q
echo -n "Build ${PROJECT_NAME}: "
write_success "OK"

# Generate an HTML file to include in geeneral documentation
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs/${PROJECT_NAME}
${SCRIPTS_PATH}/convertAdocToHtml.sh docs minimal.adoc ${DESTINATION_PATH}
