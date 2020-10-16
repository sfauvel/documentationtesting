#!/bin/bash

docker run \
	-v $(pwd)/src:/project/src \
	-v $(pwd)/docs:/project/docs \
	-w /project/src \
	-it python:3.8.1 \
	python -m unittest
