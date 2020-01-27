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

function generate() {
    COMMAND="$@"

    docker run -it \
        -v $(pwd):${DOCKER_WORKDIR}/ \
        -w ${DOCKER_WORKDIR} \
            ${DOCKER_IMAGE} \
                $COMMAND \
                -r asciidoctor-diagram \
                -a sourcedir=${DOCKER_WORKDIR}/src/main/java \
                -a webfonts! \
                ${ASCIIDOC_PATH}/${FILENAME}.adoc
}

function generateDoc() {
    MODULE=documentationtestingdoc

    docker run -it \
    	-v $(pwd):${DOCKER_WORKDIR}/ \
	    -w ${DOCKER_WORKDIR}/${MODULE} \
    	${DOCKER_IMAGE} \
    	asciidoctor \
    	-D ${DOCKER_WORKDIR}/docs \
    	-o index.html \
    	-r asciidoctor-diagram \
    	-a sourcedir=${DOCKER_WORKDIR}/${MODULE}/src/main/java \
        -a webfonts! \
    	--attribute htmlOutput="html" \
    	${DOCKER_WORKDIR}/documentationtestingdoc/target/adoc/demo.adoc
}

function generateDemo() {
    FILENAME=Documentation
    MODULE=$1

    if [ ! -d docs/${MODULE} ]
    then
        mkdir docs/${MODULE}
    fi

    docker run -it \
    	-v $(pwd):${DOCKER_WORKDIR}/ \
	    -w ${DOCKER_WORKDIR}/${MODULE} \
    	${DOCKER_IMAGE} \
    	asciidoctor \
    	-D ${DOCKER_WORKDIR}/docs/${MODULE} \
    	-o index.html \
    	-r asciidoctor-diagram \
    	-a sourcedir=${DOCKER_WORKDIR}/${MODULE}/src/main/java \
        -a webfonts! \
    	--attribute htmlOutput="html" \
    	${DOCKER_WORKDIR}/${MODULE}/src/test/docs/${FILENAME}.adoc

}

if [ ! -d ${DOC_PATH} ]
then
    mkdir ${DOC_PATH}
fi

spushd documentationtestingdoc
mvn install exec:java -Dexec.mainClass="fr.sfvl.documentationtesting.DocGenerator"
popd

generateDoc

for demo_foder in  $(ls | grep "demo_*")
do
    generateDemo $demo_foder
done


