#!/bin/zsh -f
THIS_DIR=$(dirname $0)

export CLASSPATH=.
export CLASSPATH=${CLASSPATH}:${THIS_DIR}../jcs/conf:

for i in `find ${THIS_DIR}/jars -name "*.jar" `
do
        export CLASSPATH=${CLASSPATH}:$i
done


echo ${CLASSPATH}
${JAVA_HOME}/bin/java  -Xms128m -Xmx512m -verbosegc -classpath ${CLASSPATH}  "-Djava.security.policy=${THIS_DIR}/conf/cache.policy" org.apache.jcs.auxiliary.remote.server.RemoteCacheServerFactory /remote.cache$1.ccf
