#!/usr/bin/env bash
set -euo pipefail

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=src/test/docs
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs/${PROJECT_NAME}

source ${SCRIPTS_PATH}/loadWritingFunction.sh

mvn clean install package -q
echo -n "Build ${PROJECT_NAME}: "
write_success "OK"
${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} Documentation.adoc ${DESTINATION_PATH}
${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} svg/Documentation.adoc ${DESTINATION_PATH}/svg
