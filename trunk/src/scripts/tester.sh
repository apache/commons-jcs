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
# !/bin/zsh -f
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
# -Xrunhprof:cpu=samples,depth=6,thread=y

${JAVA_HOME}/bin/java  -ms90m -mx400m -verbosegc -classpath "${CLASSPATH}" org.apache.commons.jcs.access.TestCacheAccess /cache$argv.ccf
