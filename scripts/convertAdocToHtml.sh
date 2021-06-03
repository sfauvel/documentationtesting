#!/usr/bin/env bash
# Convert all documentation of modules writing in asciidoctor to a global HTML documentation.
# You can define `DOC_AS_TEST_ADDITIONAL_VOLUME` variable to add one volume to docker (not possible to add more than one)
#   DOC_AS_TEST_ADDITIONAL_VOLUME=$(pwd)/../samples:${DOCKER_WORKDIR}/samples
set -euo pipefail

ASCIIDOC_DOCKER_IMAGE=asciidoctor/docker-asciidoctor:1.1.0
if [[ "$(uname)" =~ "NT" ]]; then IS_WINDOWS=true; else IS_WINDOWS=false; fi

if [[ -z ASCIIDOC_DOCKER_IMAGE ]]
then
  echo "No docker image defined"
  echo "Html will not be generated"
  #read -t 5 -n1 -r -p "Press a key to continue..." key
  exit 0
fi

if $IS_WINDOWS
then
  echo "Docker will not work in a shell bash on Windows"
  echo "Html will not be generated"
  #read -t 5 -n1 -r -p "Press a key to continue..." key
  exit 0
fi

REPO_GITHUB=https://github.com/sfauvel/documentationtesting
GITHUB_PAGES=https://sfauvel.github.io/documentationtesting
DOCKER_WORKDIR=/documents
DOC_PATH=docs
SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# Styles come from : https://github.com/darshandsoni/asciidoctor-skins
#STYLE=adoc-readthedocs.css
#STYLE=adoc-rocket-panda.css
STYLE=asciidoctor.css

function generateAsciidoc() {

    local PROJECT_PATH=$1
    local ADOC_FILE=$2
    local DESTINATION=$3
    local OUTPUT_FILE=$4
    local STYLESHEETS=${SCRIPT_PATH}/../stylesheets

    # By default, we set to ':' so we can add this value after '-v' option in docker command.
    local ADDITIONAL_VOLUME=$(eval echo "${DOC_AS_TEST_ADDITIONAL_VOLUME:-":"}")

    # echo ------------------------------
    # echo PROJECT_PATH: $PROJECT_PATH
    # echo ADOC_FILE: $ADOC_FILE
    # echo DESTINATION: $DESTINATION
    # echo STYLESHEETS: $STYLESHEETS
    # echo STYLE: $STYLE
    # echo ------------------------------

    if [ ! -d ${DESTINATION} ]
    then
        mkdir -p ${DESTINATION}
    fi

# -v $(pwd)/../samples:${DOCKER_WORKDIR}/samples
    docker run -it \
    	-v $(pwd)/${PROJECT_PATH}:${DOCKER_WORKDIR}/ \
    	-v $(pwd)/${DESTINATION}:/destination/ \
    	-v ${STYLESHEETS}:/stylesheets:ro \
	    -v ${ADDITIONAL_VOLUME} \
	    -w ${DOCKER_WORKDIR} \
    	${ASCIIDOC_DOCKER_IMAGE} \
    	asciidoctor \
    	-D /destination \
    	-o "$OUTPUT_FILE" \
    	-r asciidoctor-diagram \
    	-a sourcedir=${DOCKER_WORKDIR}/src/main/java \
        -a webfonts! \
        -a stylesheet=/stylesheets/${STYLE} \
    	--attribute htmlOutput="html" \
    	--attribute github-repo="${REPO_GITHUB}/tree/master" \
      --attribute github-pages="${GITHUB_PAGES}" \
    	--attribute rootpath="${DOCKER_WORKDIR}" \
    	${ADOC_FILE}

}

function generateDemo() {
    generateAsciidoc . $1/$2 $3 $4

    ABSOLUTE_DESTINATION_PATH=$(pwd)/$3
    pushd $1 > /dev/null
    find . -name *.png -exec cp --parents {} $ABSOLUTE_DESTINATION_PATH \;
    find . -name *.jpg -exec cp --parents {} $ABSOLUTE_DESTINATION_PATH \;
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
    exit 1
  fi
}


OUTPUT_FILE=${4:-"index.html"}

source ${SCRIPT_PATH}/loadWritingFunction.sh
echo -n "Convert '$1/$2' to html: "
HTML_RESULT=$(generateDemo $1 $2 $3 $OUTPUT_FILE)
write_result "$HTML_RESULT"

