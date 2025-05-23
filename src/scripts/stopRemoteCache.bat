rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements.  See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership.  The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License.  You may obtain a copy of the License at
rem
rem   https://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.
@echo off

call prep.bat

:run
rem org.apache.commons.jcs.auxiliary.remote.server.RemoteCacheServerFactory
rem set DBUGPARM=-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=5000,suspend=n
%JAVA_HOME%\bin\java %DBUGPARM% -ms1m -mx20m -classpath %CLASSPATH% "-Djava.security.policy=C:/dev/cache/props/cache.policy" org.apache.commons.jcs.auxiliary.remote.server.RemoteCacheServerFactory -shutdown /remote.cache.ccf

