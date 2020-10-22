#!/usr/bin/env bash
set -euo pipefail

PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=../scripts
DOCS_PATH=src/test/docs
DESTINATION_PATH=../docs/${PROJECT_NAME}

mvn clean install package -q

for DOC_FILE in $(find ${DOCS_PATH} -name "*.adoc")
do
  ASCIIDOC_NAME=${DOC_FILE##*/}
  HTMLDOC_NAME=${ASCIIDOC_NAME/.adoc/.html}
  ASCIIDOC_PATH=${DESTINATION_PATH}$(dirname ${DOC_FILE})
  ASCIIDOC_PATH=${ASCIIDOC_PATH/$DOCS_PATH/}
  ROOT_PATH=$(dirname ${DOC_FILE})
  ${SCRIPTS_PATH}/convertAdocToHtml.sh  ${ROOT_PATH} ${ASCIIDOC_NAME} ${ASCIIDOC_PATH} ${HTMLDOC_NAME}
done