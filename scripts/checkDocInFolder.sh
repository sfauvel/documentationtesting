#!/bin/bash
# Check differences between file on disk and git files in one subdirectory.
# Each difference is consider as a regression.

if [ -z "$1" ]
then
  echo First parameter with path to check must be define
  exit 0
else
  ROOT_DOC=$1
fi

set -euo pipefail

SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
source ${SCRIPT_PATH}/loadWritingFunction.sh

NEW_LINE=$'\n'

# We use staged files as reference
STATUS=$(git status -s --no-renames $ROOT_DOC)
if [ -z "$STATUS" ]
then
  NOT_STAGED_STATUS=""
else
  NOT_STAGED_STATUS=$(echo "$STATUS" | grep "^.[^ ].*$")
fi
ALL_FAILING_TESTS="$NOT_STAGED_STATUS"

GIT_FILES=$(git ls-tree -r --name-only HEAD $ROOT_DOC)
PATH_FILES=$(find . -type f -path "./$ROOT_DOC/*" -printf '%P\n')
ALL_GIT_FILES=$(echo "${GIT_FILES}${NEW_LINE}${PATH_FILES}"|sort|uniq)

NB_FAILURES=0
for FILENAME in $ALL_GIT_FILES
do
  SIMPLE_FILE_NAME=$(sed -r "s|.*\/(.*).adoc|\1|g" <<<"$FILENAME")
  if [[ $ALL_FAILING_TESTS == *"$FILENAME"* ]]; then
    write_failure "- $SIMPLE_FILE_NAME"
    NB_FAILURES=`expr ${NB_FAILURES} + 1`
  else
    write_success "- $SIMPLE_FILE_NAME"
  fi
done

echo ---------------------
if [ ! -z "$ALL_FAILING_TESTS" ]
then
  write_failure "FAILURES: ${NB_FAILURES}"
else
  write_success "SUCCESS"
fi
