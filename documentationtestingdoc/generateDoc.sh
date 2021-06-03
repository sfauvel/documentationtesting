#!/usr/bin/env bash
set -euo pipefail


mvn clean install package -q

export DOC_AS_TEST_ADDITIONAL_VOLUME='$(pwd)/../samples:${DOCKER_WORKDIR}/samples'

DOC_PROJECT_PATH=..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
DOCS_PATH=src/test/docs

DESTINATION_PATH=${DOC_PROJECT_PATH}/docs/${PROJECT_NAME}
for DOC_FILE in $(find ${DOCS_PATH} -type f -name "*.adoc")
do
  # Not include approved files
  if [[ ! "$DOC_FILE" == *.approved.adoc && ! "$DOC_FILE" == *.received.adoc ]]
  then
    ASCIIDOC_NAME=${DOC_FILE##*/}
    HTMLDOC_NAME=${ASCIIDOC_NAME/.adoc/.html}
    ASCIIDOC_PATH=${DESTINATION_PATH}$(dirname ${DOC_FILE})
    ASCIIDOC_PATH=${ASCIIDOC_PATH/$DOCS_PATH/}
    ROOT_PATH=$(dirname ${DOC_FILE})
    echo "${SCRIPTS_PATH}/convertAdocToHtml.sh  ${ROOT_PATH} ${ASCIIDOC_NAME} ${ASCIIDOC_PATH} ${HTMLDOC_NAME}"
    ${SCRIPTS_PATH}/convertAdocToHtml.sh  ${ROOT_PATH} ${ASCIIDOC_NAME} ${ASCIIDOC_PATH} ${HTMLDOC_NAME}
  fi
done

# Special index file.
# Html file is not put in project folder like others but it will be at the root folder of documentation.
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs

INDEX_FILE_NAME=index.adoc
DOC_FILE=tmp_index.adoc

cp ${DOCS_PATH}/${INDEX_FILE_NAME} ${DOCS_PATH}/${DOC_FILE}
sed -i "s/:ROOT_PATH: \./:ROOT_PATH: ${PROJECT_NAME}/g" ${DOCS_PATH}/${DOC_FILE}

ASCIIDOC_NAME=${DOC_FILE##*/}
HTMLDOC_NAME=${INDEX_FILE_NAME/.adoc/.html}
ASCIIDOC_PATH=${DESTINATION_PATH}
ROOT_PATH=${DOCS_PATH}

${SCRIPTS_PATH}/convertAdocToHtml.sh  ${ROOT_PATH} ${ASCIIDOC_NAME} ${ASCIIDOC_PATH} ${HTMLDOC_NAME}

rm ${DOCS_PATH}/${DOC_FILE}