#!/usr/bin/env bash

DOC_PROJECT_PATH=../..
PROJECT_NAME=${PWD##*/}
SCRIPTS_PATH=${DOC_PROJECT_PATH}/scripts
source ${SCRIPTS_PATH}/loadWritingFunction.sh

mvn clean install package -q
cp -r target/docs ${DOC_PROJECT_PATH}/
echo -n "Build ${PROJECT_NAME}: "
write_success "OK"
