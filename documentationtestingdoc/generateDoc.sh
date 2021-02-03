#!/usr/bin/env bash
set -euo pipefail

DOC_PROJECT_PATH=.
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=${PROJECT_NAME}/target/classes/docs
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs

mvn clean install package -q

echo  ${DOCS_PATH}
pushd ..
for DOC_FILE in $(find ${DOCS_PATH} -name "*.adoc")
do
  ASCIIDOC_NAME=${DOC_FILE##*/}
  HTMLDOC_NAME=${ASCIIDOC_NAME/.adoc/.html}
  ASCIIDOC_PATH=${DESTINATION_PATH}$(dirname ${DOC_FILE})
  ASCIIDOC_PATH=${ASCIIDOC_PATH/$DOCS_PATH/}
  ROOT_PATH=$(dirname ${DOC_FILE})
  ${SCRIPTS_PATH}/convertAdocToHtml.sh  ${ROOT_PATH} ${ASCIIDOC_NAME} ${ASCIIDOC_PATH} ${HTMLDOC_NAME}
done
popd
