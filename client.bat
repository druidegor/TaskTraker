@echo off
setlocal

if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Users\egorm\.jdks\openjdk-23.0.2"
set "PATH=%JAVA_HOME%\bin;%PATH%"

call mvnw.cmd javafx:run
