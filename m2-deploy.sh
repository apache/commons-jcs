#!/bin/sh
mvn -Dfile=target/jcs-1.3.jar -DrepositoryId=apache.releases -DpomFile=pom.xml -Durl=scpexe://people.apache.org/www/people.apache.org/repo/m2-ibiblio-rsync-repository -Dpackaging=jar deploy:deploy-file
