#!/usr/bin/env bash


DOCKER_IMAGE=asciidoctor/docker-asciidoctor
DOCKER_WORKDIR=/documents

DOC_PATH=docs
# File to generate
ASCIIDOC_PATH=documentationtestingdoc/target/adoc
FILENAME=demo

# Validation mode: git or approvals
# With approvals: file .approved is compare to .received (no need to have git). It not verify removed tests
# with git: file .approved is compare with git commited version. It detect tests removed.
VALIDATION_MODE="git"

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
    generateAsciidoc "" docs ${DOCKER_WORKDIR}/${MODULE}/target/classes/docs/demo.adoc
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

function remove_docs_directories() {
  for DEMO_NAME in  $(ls | grep "demo_*")
  do
    rm -rf $DEMO_NAME/src/test/docs
  done
}

# Generate all documentation

# delete docs directories to check files not regenerated because of a removed test.
# Do not remove if check with approvals
if [ $VALIDATION_MODE = "git" ]
then
  remove_docs_directories
fi

# 'noassert' avoir to check diff on each test. That's not seem to significantly faster.
mvn clean install package -Dnoassert -Dapproved_with=$VALIDATION_MODE

# Check file differences
ALL_RESULTS=""
echo -n "Generate Html"
for DEMO_NAME in  $(ls | grep "demo_*")
do
    echo ""
    echo ---------------------
    echo "$DEMO_NAME"
    echo ---------------------
    DEMO_RESULT=$(source checkDoc.sh $DEMO_NAME/src/test/docs)
    DEMO_STATUS="${DEMO_RESULT##*$'\n'}" # Last line
    echo "$DEMO_RESULT"
    ALL_RESULTS="$ALL_RESULTS- ${DEMO_NAME}: ${DEMO_STATUS}\n"
done

echo ""
echo ---------------------
echo Results:
echo -e "$ALL_RESULTS"

# Generate Html files
RED='\033[0;31m'
GREEN='\033[0;32m'
NO_COLOR='\033[0m' # No Color

write_failure() {
  echo -e "${RED}$1${NO_COLOR}"
}

write_success() {
  echo -e "${GREEN}$1${NO_COLOR}"
}


write_result() {
  HTML_RESULT="$*"
  if [ -z "$HTML_RESULT" ]
  then
    write_success "OK"
  else
    write_failure "FAILURE"
    write_failure "$HTML_RESULT"
  fi
}

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
