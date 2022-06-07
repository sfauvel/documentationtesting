#!/usr/bin/env bash
set -euo pipefail

GENERATE_HTML="yes"
while getopts ":t" opt; do
   case ${opt} in
     t ) GENERATE_HTML="no"
       ;;
   esac
done

mvn clean test install
if [[ "$GENERATE_HTML" == "yes" ]];
then
  mvn org.asciidoctor:asciidoctor-maven-plugin:process-asciidoc
fi
