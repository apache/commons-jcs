#! /bin/bash

cd commons-jcs-core && mvn clean install -Dmaven.test.skip=true && cd -
cd commons-jcs-jcache && mvn clean install && cd -
cd commons-jcs-jcache-openjpa && mvn clean install && cd -
cd commons-jcs-jcache-extras && mvn clean install && cd -
cd commons-jcs-tck-tests && mvn clean install -Djcache.tck && cd -

