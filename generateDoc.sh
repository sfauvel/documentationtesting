#!/usr/bin/env bash


DOCKER_IMAGE=asciidoctor/docker-asciidoctor
DOCKER_WORKDIR=/documents

DOC_PATH=docs
# File to generate
ASCIIDOC_PATH=documentationtestingdoc/target/adoc
FILENAME=demo

if [ ! -d ${ASCIIDOC_PATH} ]
then

    echo "Directory ${ASCIIDOC_PATH} does not exists."
fi


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
    	--attribute rootpath="../../.." \
    	${ADOC_FILE}

}

function generateDoc() {
    MODULE=documentationtestingdoc
    generateAsciidoc "" docs ${DOCKER_WORKDIR}/${MODULE}/target/adoc/demo.adoc
}

# Redefine pushd and popd to avoid trace
function pushd() {
  command pushd "$@" > /dev/null
}

function popd() {
  command popd "$@" > /dev/null
}

function generateDemo() {
    MODULE=$1
    generateAsciidoc ${MODULE} docs/${MODULE} ${DOCKER_WORKDIR}/src/test/docs/Documentation.adoc

    pushd ${MODULE}/src/test/docs
    find . -name *.png -exec cp --parents {} ../../../../docs/${MODULE} \;
    popd
}

if [ ! -d ${DOC_PATH} ]
then
    mkdir ${DOC_PATH}
fi

# Generate all documentation
mvn clean install package

# Generate Html files
echo -n "Generate Html"
for demo_folder in  $(ls | grep "demo_*")
do
    generateDemo $demo_folder
    echo -n "."
done

generateDoc
echo "."

