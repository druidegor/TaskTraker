@echo off
setlocal

if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Users\egorm\.jdks\openjdk-23.0.2"
set "PATH=%JAVA_HOME%\bin;%PATH%"

call mvnw.cmd -q compile
java -cp "target\classes;C:\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar" org.example.tasktraker.server.Server
