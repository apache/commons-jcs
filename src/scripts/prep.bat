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
set CLASSPATH=%CLASSPATH%;%CURDIR%\conf\
set CLASSPATH=%CLASSPATH%;%CURDIR%\classes\
set CLASSPATH=%CLASSPATH%;%CURDIR%\stratum.jar
goto jars

:jars
set _LIBJARS=
for %%i in (@lib.repo@*.jar) do call %CURDIR%\scripts\cpappend.bat %%i
if not "%_LIBJARS%" == "" goto addLibJars

:addLibJars
set CLASSPATH=%CLASSPATH%;%_LIBJARS%
  
