rem cd ..
rem dir | find "Directory" > }{.bat
rem echo set CURDIR=%%2> directory.bat
rem for %%a in (call del) do %%a }{.bat
rem cd bin 

cd ..\..
set CURDIR=%CD%
echo %CURDIR%
cd src
cd scripts