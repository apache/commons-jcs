#!/bin/zsh -f
THIS_DIR=$(dirname $0)

export CLASSPATH=${THIS_DIR}/../../src/conf
export CLASSPATH=${CLASSPATH}:${THIS_DIR}/../../target/test-classes
export CLASSPATH=${CLASSPATH}:${THIS_DIR}/../../target/classes
export CLASSPATH=${CLASSPATH}:.

for i in `find ${THIS_DIR}/../../jars -name "*.jar" `
do
        export CLASSPATH=${CLASSPATH}:$i
done



echo ${CLASSPATH}

${JAVA_HOME}/bin/java -ms90m -mx400m -verbosegc -classpath "${CLASSPATH}" org.apache.jcs.access.TestCacheAccess /cache$argv.ccf 
