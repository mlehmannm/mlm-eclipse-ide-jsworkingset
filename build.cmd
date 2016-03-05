@setlocal
@echo off
@if not "%ECHO%" == "" echo %ECHO%

@rem ???
if "%1" == "" set CALL_MAVEN_OPTS=verify
if not "%1" == "" set CALL_MAVEN_OPTS=%*

@call mvn -V -e %CALL_MAVEN_OPTS%
