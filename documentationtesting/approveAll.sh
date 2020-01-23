#!/usr/bin/env bash


pushd src/test/docs

find ./ -type f -name '*.adoc' -execdir rename -f 's/\.received\.adoc$/.approved.adoc/g' '{}' \;

popd