#!/usr/bin/env bash
set -euo pipefail

GENERATE_HTML="yes"
while getopts ":t" opt; do
   case ${opt} in
     t ) GENERATE_HTML="no"
       ;;
   esac
done

mvn clean test
if [[ "$GENERATE_HTML" == "yes" ]];
then
  mvn org.asciidoctor:asciidoctor-maven-plugin:process-asciidoc

  # Generate the root index.html
  DOC_PROJECT_PATH=../tmp
  PROJECT_NAME=${PWD##*/}
  DESTINATION_PATH=${DOC_PROJECT_PATH}/docs
  cp ${DESTINATION_PATH}/${PROJECT_NAME}/index.html ${DESTINATION_PATH}/index.html

  HREF_TO_REPLACE="<a href=\".\/"
  REPLACE_BY="<a href=\"${PROJECT_NAME}\/"

  sed -i "s/${HREF_TO_REPLACE}/${REPLACE_BY}/g" ${DESTINATION_PATH}/index.html
fi
