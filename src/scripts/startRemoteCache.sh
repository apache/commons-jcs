# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ! /bin/sh

export CLASSPATH=.
export CLASSPATH=${CLASSPATH}:`dirname $0`/../conf:/usr/java/jcs/conf:/usr/java/jcs/conf/

THISDIR=`dirname $0`

for i in `find ${THISDIR}/../lib -name "*.jar" `
do
        export CLASSPATH=${CLASSPATH}:$i
done
echo "Classpath = ${CLASSPATH}"

# START THE REGISTRY
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



