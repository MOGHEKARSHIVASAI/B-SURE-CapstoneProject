@echo off
cd /d "%~dp0"
call mvnw.cmd clean test -DskipIntegrationTests
echo.
echo TEST RUN COMPLETE
pause

