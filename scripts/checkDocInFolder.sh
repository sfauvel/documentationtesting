#!/bin/bash

# Usage info
function show_help() {
  echo -e "${BOLD}NAME${NO_COLOR}"
  echo -e "\t${BOLD}${0##*/}${NO_COLOR} - Check differences between files on disk and git staged files."
  echo -e "${BOLD}SYNOPSIS${NO_COLOR}"
  echo -e "\t${BOLD}${0##*/}${NO_COLOR} [OPTION]... PATH"
  echo -e "${BOLD}DESCRIPTION${NO_COLOR}"
  echo -e "\tCheck differences between files in PATH and git staged files."
  echo -e "\tEach difference is consider as a failure."
  echo -e ""
  echo -e "\t${BOLD}-h${NO_COLOR}"
  echo -e "\t\t display this help and exit."
  echo -e "\t${BOLD}-f${NO_COLOR}"
  echo -e "\t\t show only failures."
}
SHOW_ONLY_FAILURE=false
SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
source ${SCRIPT_PATH}/loadWritingFunction.sh

ARGS=( "$@" )
while getopts "hf" OPTION; do
   case ${OPTION} in
     h ) show_help
       exit 0
       ;;
     \? ) show_help
       exit 0
       ;;
    f ) SHOW_ONLY_FAILURE=true
      ;;
   esac
done

LAST_OPTION_INDEX=$((OPTIND-1))

ROOT_DOC=${ARGS[${LAST_OPTION_INDEX}+0]}
if [ -z "$ROOT_DOC" ]; then show_help; exit 0; fi

set -euo pipefail


NEW_LINE=$'\n'

# We use staged files as reference
STATUS=$(git status -s --no-renames $ROOT_DOC)
if [ -z "$STATUS" ]
then
  NOT_STAGED_STATUS=""
else
  NOT_STAGED_STATUS=$(echo "$STATUS" | grep "^.[^ ].*$") || true
fi
ALL_FAILING_TESTS="$NOT_STAGED_STATUS"

GIT_FILES=$(git ls-tree -r --name-only HEAD $ROOT_DOC)
PATH_FILES=$(find . -type f -path "./$ROOT_DOC/*" -printf '%P\n')
ALL_GIT_FILES=$(echo "${GIT_FILES}${NEW_LINE}${PATH_FILES}"|sort|uniq)

NB_FAILURES=0
for FILENAME in $ALL_GIT_FILES
do
  SIMPLE_FILE_NAME=$(sed -r "s|.*\/(.*).adoc|\1|g" <<<"$FILENAME")
  if [[ $ALL_FAILING_TESTS == *"$FILENAME"* ]]
  then
    FAILURE_LINE=$(grep "$FILENAME" <<< "${ALL_FAILING_TESTS}")
    write_failure "- ${SIMPLE_FILE_NAME}(${FAILURE_LINE:1:1})"
    NB_FAILURES=`expr ${NB_FAILURES} + 1`
  else
    if [ $SHOW_ONLY_FAILURE = false ]
    then
      write_success "- $SIMPLE_FILE_NAME"
    fi
  fi
done

echo ---------------------
if [ -n "$ALL_FAILING_TESTS" ]
then
  write_failure "FAILURES: ${NB_FAILURES}"
else
  write_success "SUCCESS"
fi