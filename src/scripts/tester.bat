@echo off

call prep.bat
   
:run
java -ms10m -mx400m org.apache.jcs.access.TestCacheAccess /cache%1.ccf  %2 %3 %4 %5
 


