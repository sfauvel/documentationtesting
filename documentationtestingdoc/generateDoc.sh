#!/usr/bin/env bash
set -euo pipefail

mvn clean install package

# Generate the root index.html
DOC_PROJECT_PATH=..
PROJECT_NAME=${PWD##*/}
DESTINATION_PATH=${DOC_PROJECT_PATH}/docs
cp ${DESTINATION_PATH}/${PROJECT_NAME}/index.html ${DESTINATION_PATH}/index.html

HREF_TO_REPLACE="<a href=\".\/"
REPLACE_BY="<a href=\"${PROJECT_NAME}\/"

sed -i "s/${HREF_TO_REPLACE}/${REPLACE_BY}/g" ${DESTINATION_PATH}/index.html