@echo off

call prep.bat

rem-Dlog4j.configuration=I:/dev/jakarta-turbine-jcs/src/scripts/log4j.properties
   
:run
java -ms90m -mx400m -verbosegc org.apache.jcs.access.TestCacheAccess /cache%1.ccf  %2 %3 %4 %5
 


