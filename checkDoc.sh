#!/bin/bash

ROOT_DOC=$1

RED='\033[0;31m'
GREEN='\033[0;32m'
NO_COLOR='\033[0m' # No Color

ALL_FAILING_TESTS=$(git diff --name-only $ROOT_DOC)
ALL_GIT_FILES=$(git ls-files -dmoc $ROOT_DOC)

GIT_DIFF_BETWEEN_HEAD_AND_INDEX=$(git diff-index --no-commit-id --name-only HEAD $ROOT_DOC)
GIT_NOT_INDEXES_FILES=$(git ls-files -o $ROOT_DOC)

if [ ! -z "$GIT_DIFF_BETWEEN_HEAD_AND_INDEX" ]
then
  NB_GIT_DIFF_BETWEEN_HEAD_AND_INDEX=$(wc --lines <<<"$GIT_DIFF_BETWEEN_HEAD_AND_INDEX")
else
  NB_GIT_DIFF_BETWEEN_HEAD_AND_INDEX=0
fi
if [ ! -z "$GIT_NOT_INDEXES_FILES" ]
then
  NB_GIT_NOT_INDEXES_FILES=$(wc --lines <<<"$GIT_NOT_INDEXES_FILES")
else
  NB_GIT_NOT_INDEXES_FILES=0
fi
NB_FAILURES=$((NB_GIT_DIFF_BETWEEN_HEAD_AND_INDEX + NB_GIT_NOT_INDEXES_FILES))

write_failure() {
  echo -e "${RED}$1${NO_COLOR}"
}

write_success() {
  echo -e "${GREEN}$1${NO_COLOR}"
}

for filename in $ALL_GIT_FILES
do
  SIMPLE_FILE_NAME=$(sed -r "s|.*\/(.*).adoc|\1|g" <<<"$filename")
  if [[ $GIT_DIFF_BETWEEN_HEAD_AND_INDEX == *"$filename"* ]]; then
    write_failure "- $SIMPLE_FILE_NAME"
  elif [[ $GIT_NOT_INDEXES_FILES == *"$filename"* ]]; then
    write_failure "- $SIMPLE_FILE_NAME"
  else
    write_success "- $SIMPLE_FILE_NAME"
  fi
done

echo ---------------------

if [ ! -z "$GIT_DIFF_BETWEEN_HEAD_AND_INDEX" ]
then
  write_failure "FAILURES: ${NB_FAILURES}"
elif [ ! -z "$GIT_NOT_INDEXES_FILES" ]
then
  write_failure "FAILURES: ${NB_FAILURES}"
else
  write_success "SUCCESS"
fi
