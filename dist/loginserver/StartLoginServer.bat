@echo off
color 0C
title L2Project - High Five: Login Server Console
:start
echo Starting L2Project - High Five: Login Server.
echo.

SET java_opts=%java_opts% -Xms32m
SET java_opts=%java_opts% -Xmx64m

REM Sets survivor space ratio to 1:8, resulting in larger survivor spaces (the smaller the ratio, the larger the space). Larger survivor spaces allow short lived objects a longer time period to die in the young generation
SET java_opts=%java_opts% -XX:SurvivorRatio=8
SET java_opts=%java_opts% -Xincgc
SET java_opts=%java_opts% -XX:+AggressiveOpts

SET java_settings=%java_settings% -Dfile.encoding=UTF-8
SET java_settings=%java_settings% -Djava.net.preferIPv4Stack=true

java -server %java_settings% %java_opts% -cp config;lib/*; jts.loginserver.LoginServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly
echo.
:end
echo.
echo server terminated
echo.
del login_is_running.tmp
pause
