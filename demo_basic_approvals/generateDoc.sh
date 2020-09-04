#!/usr/bin/env bash
set -euo pipefail

PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=../scripts
DOCS_PATH=src/test/docs
DESTINATION_PATH=../docs/${PROJECT_NAME}

source ${SCRIPTS_PATH}/loadWritingFunction.sh
mvn clean install package
${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} Documentation.adoc ${DESTINATION_PATH}
