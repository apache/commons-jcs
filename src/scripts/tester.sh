#!/bin/zsh -f
THIS_DIR=$(dirname $0)

pushd ${THIS_DIR}/../..; 

export CLASSPATH=.
export CLASSPATH=${CLASSPATH}:${THIS_DIR}/target/test-classes
export CLASSPATH=${CLASSPATH}:${THIS_DIR}/target/classes
#export CLASSPATH=${CLASSPATH}:${THIS_DIR}/src/conf

for i in `find ${THIS_DIR}/jars -name "*.jar" `
do
        export CLASSPATH=${CLASSPATH}:$i
done



echo ${CLASSPATH}

${JAVA_HOME}/bin/java -ms90m -mx400m -verbosegc -classpath "${CLASSPATH}" org.apache.jcs.access.TestCacheAccess /cache$argv.ccf 
