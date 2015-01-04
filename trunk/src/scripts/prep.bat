rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements.  See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership.  The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License.  You may obtain a copy of the License at
rem
rem   http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.
@rem echo off

:setcurdir
call setCURDIR
echo %CURDIR%

goto javahome

:javahome
if "%JAVA_HOME%" == "" goto noJavaHome
goto setcpbase

:noJavaHome
echo Warning: JAVA_HOME environment variable is not set.
set JAVA_HOME=C:\jdk1.2.2

:setcpbase
set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;%CURDIR%\src\conf\
set CLASSPATH=%CLASSPATH%;%CURDIR%\target\classes\
set CLASSPATH=%CLASSPATH%;%CURDIR%\target\test-classes\
set CLASSPATH=%CLASSPATH%;%CURDIR%\auxiliary-builds\jdk14\target\classes\
goto jars

:jars
set _LIBJARS=
for %%i in (%CURDIR%\jars\*.jar) do call %CURDIR%\src\scripts\cpappend.bat %%i
if not "%_LIBJARS%" == "" goto addLibJars

:addLibJars
set CLASSPATH=%CLASSPATH%;%_LIBJARS%

