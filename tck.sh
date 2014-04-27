#! /bin/bash

# build [jcs] first
mvn clean install -Dmaven.test.skip=true

# then run tck against freshly built binaries
mvn clean -f tck.xml test surefire-report:report-only -Dmaven.test.failure.ignore=true $@

