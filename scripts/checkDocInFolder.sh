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

ALL_FAILING_TESTS=$(git status -s --no-renames $ROOT_DOC)

ALL_FILES_NOT_DELETED=$(find $ROOT_DOC -type f -printf "%f\n")
# Warning: option is 'one' digit and not a lowercase letter 'L'.
GIT_DELETED_FILE=$(git diff-index --diff-filter=D --name-only HEAD $ROOT_DOC)

ALL_GIT_FILES="${ALL_FILES_NOT_DELETED}${NEW_LINE}${GIT_DELETED_FILE}"

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
