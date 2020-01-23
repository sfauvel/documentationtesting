#!/usr/bin/env bash

mvn test

mvn exec:java -Dexec.mainClass="org.sfvl.doctesting.MainDocumentation" -Dexec.classpathScope=test

. convertAdoc.sh
