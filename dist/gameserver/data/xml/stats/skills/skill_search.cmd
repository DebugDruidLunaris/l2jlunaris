@echo off
title Skill search
cls
echo.
:find
set /p text="enter search text here: "
echo.
echo.search text "%text%" result:
findstr /I /N %text% *.xml
echo.
goto find