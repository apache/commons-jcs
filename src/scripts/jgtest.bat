@echo off

call prep.bat

rem-Dlog4j.configuration=I:/dev/jakarta-turbine-jcs/src/scripts/log4j.properties
   
:run
java org.jgroups.tests.McastReceiverTest -mcast_addr 224.10.10.10 -port 5555 


