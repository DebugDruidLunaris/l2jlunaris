@echo off
@color 0C
title L2Project: Game Server Registration...
:start
echo Starting Game Server Registration.
echo.
java -server -Dfile.encoding=UTF-8 -Xms32m -Xmx64m -cp config;lib/*; jts.loginserver.GameServerRegister

pause
