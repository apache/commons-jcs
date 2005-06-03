#! /bin/sh

export CLASSPATH=.
export CLASSPATH=${CLASSPATH}:`dirname $0`/../conf:/usr/java/jcs/conf:/usr/java/jcs/conf/

THISDIR=`dirname $0`

for i in `find ${THISDIR}/../lib -name "*.jar" `
do
        export CLASSPATH=${CLASSPATH}:$i
done
echo "Classpath = ${CLASSPATH}"

#START THE REGISTRY
if [ "$2" != "" ]; then
  echo "Starting the registry on port $2"
  rmiregistry $2 &
else
  echo "Not starting registry, since no port was supplied."
fi

POLICY="-Djava.security.policy=`dirname $0`/../conf/cache.policy"

HEAP="-Xms128m -Xmx512m"

DEBUG="-verbose:gc -XX:+PrintTenuringDistribution"

ARGS="$HEAP $DEBUG $POLICY"

echo $ARGS

java  $ARGS org.apache.jcs.auxiliary.remote.server.RemoteCacheServerFactory "$1"



