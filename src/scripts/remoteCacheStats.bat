@echo off

:setcurdir
call setCURDIR
goto javahome

:javahome
if "%JAVA_HOME%" == "" goto noJavaHome
goto setcpbase

:noJavaHome
echo Warning: JAVA_HOME environment variable is not set.
set JAVA_HOME=C:\jdk1.2.2

:setcpbase
set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;%CURDIR%\props\
set CLASSPATH=%CLASSPATH%;%CURDIR%\lib\build\jcs.jar
goto jars

:jars
set _LIBJARS=
for %%i in (%CURDIR%\lib\*.jar) do call %CURDIR%\bin\cpappend.bat %%i
if not "%_LIBJARS%" == "" goto addLibJars

:addLibJars
set CLASSPATH=%CLASSPATH%;%_LIBJARS%
  
:run
rem set DBUGPARM=-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=5000,suspend=n
%JAVA_HOME%\bin\java %DBUGPARM% -ms1m -mx20m -classpath %CLASSPATH% "-Djava.security.policy=C:/dev/cache/props/cache.policy" org.apache.jcs.auxiliary.remote.server.group.RemoteGroupCacheServerFactory -stats 1102 /remote.cache.properties 
 




