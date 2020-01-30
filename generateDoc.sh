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
  #  return
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
    	-v $(pwd):${DOCKER_WORKDIR}/ \
    	-v ${STYLESHEETS}:/stylesheets:ro \
	    -w ${DOCKER_WORKDIR}/${MODULE} \
    	${DOCKER_IMAGE} \
    	asciidoctor \
    	-D ${DOCKER_WORKDIR}/${DESTINATION} \
    	-o index.html \
    	-r asciidoctor-diagram \
    	-a sourcedir=${DOCKER_WORKDIR}/${MODULE}/src/main/java \
        -a webfonts! \
        -a stylesheet=/stylesheets/adoc-rocket-panda.css \
    	--attribute htmlOutput="html" \
    	--attribute rootpath="..\..\.." \
    	${ADOC_FILE}

}

function generateDoc() {
    MODULE=documentationtestingdoc
    generateAsciidoc ${MODULE} docs ${DOCKER_WORKDIR}/${MODULE}/target/adoc/demo.adoc
}

function generateDemo() {
    MODULE=$1
    generateAsciidoc ${MODULE} docs/${MODULE} ${DOCKER_WORKDIR}/${MODULE}/src/test/docs/Documentation.adoc
}

if [ ! -d ${DOC_PATH} ]
then
    mkdir ${DOC_PATH}
fi


mvn install package

# Generate main doc
pushd documentationtestingdoc
mvn install exec:java -Dexec.mainClass="fr.sfvl.documentationtesting.DocGenerator"
popd

generateDoc

# Generate examples
for demo_foder in  $(ls | grep "demo_*")
do
    generateDemo $demo_foder
done


