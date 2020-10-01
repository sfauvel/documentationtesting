#!/bin/bash

#
# Define functions to write using colors.
#

RED='\033[0;31m'
GREEN='\033[0;32m'
NO_COLOR='\033[0m' # No Color

BOLD='\033[1m'

write_failure() {
  echo -e "${RED}$1${NO_COLOR}"
}

write_success() {
  echo -e "${GREEN}$1${NO_COLOR}"
}
