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
  
