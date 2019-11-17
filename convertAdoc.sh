#!/usr/bin/env bash

# https://github.com/asciidoctor/docker-asciidoctor

DOCKER_IMAGE=asciidoctor/docker-asciidoctor
DOCKER_WORKDIR=/documents

FILENAME=Documentation
TARGET_PATH=src/test/docs
#FILENAME=GameOfLifeTest.return_fizz_when_divisible_by_three.received
#FILENAME=GameOfLifeTest.die_when_less_than_2_neighbours.received
#TARGET_PATH=src/test/docs/org/sfvl/application

# Need to set 'htmlOutput' attribute to have a specific style.

docker run -it \
	-v $(pwd):${DOCKER_WORKDIR}/ \
	-w ${DOCKER_WORKDIR}/${TARGET_PATH} \
    	${DOCKER_IMAGE} \
    	asciidoctor -r asciidoctor-diagram -a sourcedir=${DOCKER_WORKDIR}/src/main/java --attribute htmlOutput="html" ${FILENAME}.adoc

#docker run -it \
#	-v $(pwd):${DOCKER_WORKDIR}/ \
#	-w ${DOCKER_WORKDIR}/${TARGET_PATH} \
#    	${DOCKER_IMAGE} \
#    	asciidoctor -r asciidoctor-diagram -a sourcedir=${DOCKER_WORKDIR}/src/main/java GameOfLifeTest.die_when_less_than_2_neighbours.received.adoc
#
#
#
#docker run -it \
#	-v $(pwd):${DOCKER_WORKDIR}/ \
#	-w ${DOCKER_WORKDIR}/${TARGET_PATH} \
#    	${DOCKER_IMAGE} \
#    	asciidoctor -r asciidoctor-diagram -a sourcedir=${DOCKER_WORKDIR}/src/main/java GameOfLifeTest.return_fizz_when_divisible_by_three.received.adoc
#



echo "HTML documentation was generated. You can found it in ${TARGET_PATH}"

#docker run -it \
#	-v $(pwd):${DOCKER_WORKDIR}/ \
#	-w ${DOCKER_WORKDIR}/${TARGET_PATH} \
#    	${DOCKER_IMAGE} \
#    	asciidoctor-pdf -r asciidoctor-diagram -a sourcedir=${DOCKER_WORKDIR}/src/main/java ${FILENAME}.adoc
#
#echo "PDF documentation was generated. You can found it in ${TARGET_PATH}"