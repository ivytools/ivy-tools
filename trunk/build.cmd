@echo off
setlocal enableextensions
subst d: c:\
REM If JTPL_ROOT is not already set, let's try a couple reasonable defaults before erroring out.
set APP_ROOT=%~dp0
REM Remove the trailing backslash (for cleaner derivatives):
set APP_ROOT=%APP_ROOT:~0,-1%
if "%JTPL_ROOT%" == "" set JTPL_ROOT=\snapshots\java3rdpartylib
if not exist "%JTPL_ROOT%" set JTPL_ROOT=%APP_ROOT%\..\java3rdpartylib
if not exist "%JTPL_ROOT%" goto jtpl_err
set ANT_HOME=%JTPL_ROOT%\ant\1.7.1
set JAVA_HOME=%JTPL_ROOT%\jdk\1.6.0_10
REM This is to avoid putting specific versions of junit in the shared Ant repository.
set CLASSPATH=%APP_ROOT%\lib\test\junit.jar;%CLASSPATH%
set RETVAL=255

%ANT_HOME%\bin\ant -buildfile "%APP_ROOT%\build\build.xml" %*
set RETVAL=%ERRORLEVEL%
goto end


:jtpl_err
echo Please set JTPL_ROOT to your "java3rdpartylib" directory, e.g. "\snapshots\java3rdpartylib" or "\snapshots\java3rdpartylib\trunk".
set RETVAL=255


:end
exit /B %RETVAL%
