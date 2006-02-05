#! /bin/sh

export CLASSPATH=.
export CLASSPATH=${CLASSPATH}:`dirname $0`/../conf:/usr/java/jcs/conf:/usr/java/jcs/conf/

THISDIR=`dirname $0`

for i in `find ${THISDIR}/../lib -name "*.jar" `
do
        export CLASSPATH=${CLASSPATH}:$i
done
echo "Classpath = ${CLASSPATH}"


POLICY="-Djava.security.policy=`dirname $0`/../conf/cache.policy"

HEAP="-Xms10m -Xmx20m"

DEBUG="-verbose:gc -XX:+PrintTenuringDistribution"

ARGS="$HEAP $DEBUG $POLICY"

echo $ARGS

java  $ARGS org.apache.jcs.auxiliary.remote.server.RemoteCacheServerFactory -stats "$1"



