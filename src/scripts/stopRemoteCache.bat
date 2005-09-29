@echo off

call prep.bat
  
:run
rem org.apache.jcs.auxiliary.remote.server.RemoteCacheServerFactory
rem set DBUGPARM=-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=5000,suspend=n
%JAVA_HOME%\bin\java %DBUGPARM% -ms1m -mx20m -classpath %CLASSPATH% "-Djava.security.policy=C:/dev/cache/props/cache.policy" org.apache.jcs.auxiliary.remote.server.RemoteCacheServerFactory -shutdown /remote.cache.ccf 
 
