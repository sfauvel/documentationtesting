#!/bin/bash

ROOT_DOC=$1

RED='\033[0;31m'
GREEN='\033[0;32m'
NO_COLOR='\033[0m' # No Color

ALL_FAILING_TESTS=$(git diff --name-only $ROOT_DOC)
ALL_GIT_FILES=$(git ls-files $ROOT_DOC)
NB_FAILURES=$(wc --lines <<<$ALL_FAILING_TESTS)

write_failure() {
  echo -e "${RED}$1${NO_COLOR}"
}

write_success() {
  echo -e "${GREEN}$1${NO_COLOR}"
}

ALL_FAILING_TESTS_RAW=$(sed -r "s|.*\/(.*).adoc|\1|g" <<<"${DIFF}")
for filename in $ALL_GIT_FILES
do
  SIMPLE_FILE_NAME=$(sed -r "s|.*\/(.*).adoc|\1|g" <<<"$filename")
  if [[ $ALL_FAILING_TESTS == *"$filename"* ]]; then
    write_failure "- $SIMPLE_FILE_NAME"
  else
    write_success "- $SIMPLE_FILE_NAME"
  fi
done

echo ---------------------
if [ -z "$ALL_FAILING_TESTS" ]
then
  write_success "SUCCESS"
else
  write_failure "FAILURES: ${NB_FAILURES}"
fi