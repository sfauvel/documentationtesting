#!/usr/bin/env bash
set -euo pipefail

PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=./scripts
DOCS_PATH=documentationtestingdoc/target/classes/docs
DESTINATION_PATH=./docs

mvn clean install package
pushd ..
${SCRIPTS_PATH}/convertAdocToHtml.sh ${DOCS_PATH} demo.adoc ${DESTINATION_PATH}
popd