#!/usr/bin/env bash
# Convert all documentation of modules writing in asciidoctor to a global HTML documentation.
set -euo pipefail

DOCKER_IMAGE=asciidoctor/docker-asciidoctor
DOCKER_WORKDIR=/documents
DOC_PATH=docs

# Redefine pushd and popd to avoid trace
function pushdNoTrace() {
  command pushd "$@" > /dev/null
}

function popdNoTrace() {
  command popd "$@" > /dev/null
}

function generateAsciidoc() {

    MODULE=$1
    DESTINATION=$2
    ADOC_FILE=$3
    STYLESHEETS=$(pwd)/stylesheets

    if [ ! -d ${DESTINATION} ]
    then
        mkdir ${DESTINATION}
    fi

    docker run -it \
    	-v $(pwd)/${MODULE}:${DOCKER_WORKDIR}/ \
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

function generateDoc() {
    MODULE=documentationtestingdoc
    generateAsciidoc "" docs ${DOCKER_WORKDIR}/${MODULE}/target/classes/docs/demo.adoc
}

function generateDemo() {
    MODULE=$1
    generateAsciidoc ${MODULE} docs/${MODULE} ${DOCKER_WORKDIR}/src/test/docs/Documentation.adoc

    pushdNoTrace ${MODULE}/src/test/docs
    find . -name *.png -exec cp --parents {} ../../../../docs/${MODULE} \;
    popdNoTrace
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

function convert_all_to_html() {
  if [ ! -d ${DOC_PATH} ]
  then
      mkdir ${DOC_PATH}
  fi

  echo "Generate Html: "
  for DEMO_NAME in  $(ls | grep "demo_*")
  do
      echo -n "- ${DEMO_NAME}: "
      HTML_RESULT=$(generateDemo $DEMO_NAME)
      write_result "$HTML_RESULT"
  done

  HTML_RESULT=$(generateDoc)
  echo -n "- project documentation: "
  write_result "$HTML_RESULT"

}

source loadWritingFunction.sh
convert_all_to_html