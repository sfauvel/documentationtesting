#!/usr/bin/env bash
# Convert all documentation of modules writing in asciidoctor to a global HTML documentation.
set -euo pipefail

DOCKER_IMAGE=asciidoctor/docker-asciidoctor
DOCKER_WORKDIR=/documents
DOC_PATH=docs
SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

function generateAsciidoc() {

    PROJECT_PATH=$1
    ADOC_FILE=$2
    DESTINATION=$3
    STYLESHEETS=${SCRIPT_PATH}/../stylesheets

    # echo ------------------------------
    # echo PROJECT_PATH: $PROJECT_PATH
    # echo ADOC_FILE: $ADOC_FILE
    # echo DESTINATION: $DESTINATION
    # echo STYLESHEETS: $STYLESHEETS
    # echo ------------------------------

    if [ ! -d ${DESTINATION} ]
    then
        mkdir -p ${DESTINATION}
    fi

    docker run -it \
    	-v $(pwd)/${PROJECT_PATH}:${DOCKER_WORKDIR}/ \
    	-v $(pwd)/${DESTINATION}:/destination/ \
    	-v ${STYLESHEETS}:/stylesheets:ro \
	    -w ${DOCKER_WORKDIR} \
    	${DOCKER_IMAGE} \
    	asciidoctor \
    	-D /destination \
    	-o index.html \
    	-r asciidoctor-diagram \
    	-a sourcedir=${DOCKER_WORKDIR}/src/main/java \
        -a webfonts! \
        -a stylesheet=/stylesheets/adoc-rocket-panda.css \
    	--attribute htmlOutput="html" \
    	--attribute rootpath="${DOCKER_WORKDIR}" \
    	${ADOC_FILE}

}

function generateDemo() {
    generateAsciidoc . $1/$2 $3

    ABSOLUTE_DESTINATION_PATH=$(pwd)/$3
    pushd $1 > /dev/null
    find . -name *.png -exec cp --parents {} $ABSOLUTE_DESTINATION_PATH \;
    popd > /dev/null
}

function write_result() {
  HTML_RESULT="$*"
  if [ -z "$HTML_RESULT" ]
  then
    write_success "OK"
  else
    write_failure "FAILURE"
    write_failure "$HTML_RESULT"
  fi
}


source ${SCRIPT_PATH}/loadWritingFunction.sh
HTML_RESULT=$(generateDemo $1 $2 $3)
write_result "$HTML_RESULT"
