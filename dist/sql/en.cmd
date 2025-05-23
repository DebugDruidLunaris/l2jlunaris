@echo off
@color 0C
cls

title L2Project Database Installer
color a

:start
echo Initialize Database Installer ...
echo.
echo Welcome to script server setup.
echo This script will help you install the database server.
echo To continue, press the space bar to exit Ctrl + C
pause > nul
echo ======================================================================
echo Checks environment ...
mysql --help >nul 2>nul
if errorlevel 1 goto nomysql
echo  - MySQL...       ok
echo ======================================================================
echo Server is ready for installation.
echo Please perform the initial configuration
echo ======================================================================
set DO_INSTALL=Y
set /P DO_INSTALL=Install the Auth Server [Y/n]
if "%DO_INSTALL%"=="N" goto installgame
if "%DO_INSTALL%"=="n" goto installgame
set INSTALL_MODE=login
:prepare
set DB_HOST=localhost
set DB_USER=root
set DB_PASSWORD=
set DB_NAME=jts
:step2

set /P DB_HOST=Host Database Server [%DB_HOST%]:

set /P DB_USER=Database user [%DB_USER%]:

set /P DB_PASSWORD=User password [%DB_USER%]:

set /P DB_NAME=DB name [%DB_NAME%]:
SET MYSQL_PARAM=-u %DB_USER% -h %DB_HOST%
if NOT "%DB_PASSWORD%"=="" SET MYSQL_PARAM=%MYSQL_PARAM% --password=%DB_PASSWORD%
echo exit | mysql %MYSQL_PARAM% >nul 2>nul
if errorlevel 1 goto dberror
echo exit | mysql %MYSQL_PARAM% %DB_NAME% >nul 2>nul
if errorlevel 1 goto dbnotexists
goto install
:dbnotexists
echo ! The database %DB_NAME% does not exist
set ANSWER=Y
set /P ANSWER=Create it [Y/n]?
if "%ANSWER%"=="y" goto createdb
if "%ANSWER%"=="Y" goto createdb
goto step2
:createdb
echo create database %DB_NAME% charset=utf8; | mysql %MYSQL_PARAM%
if errorlevel 1 goto dberror
goto install
:dberror
echo ! Can not connect to the database. Check the connection settings!
goto step2

:install
cls
echo ======================================================================
echo Check the input parameters
echo  - The server will be installed in %INSTALL_DIR%
echo  - The database server %DB_HOST%
echo  - Database Name %DB_NAME%
set ANSWER=Y
set /P ANSWER=All settings are correct [Y/n]?
if "%ANSWER%"=="n" goto step1
if "%ANSWER%"=="N" goto step1
echo  - Install the database, wait ...
for %%i in (sql\%INSTALL_MODE%\*.sql) do mysql %MYSQL_PARAM% %DB_NAME% < %%i
if "%INSTALL_MODE%"=="login" goto installgame
goto end
:installgame
cls
set DO_INSTALL=Y
set /P DO_INSTALL=Install game server[Y/n]
if "%DO_INSTALL%"=="N" goto end
if "%DO_INSTALL%"=="n" goto end
set INSTALL_MODE=game
goto prepare 
:nomysql
cls
echo  ! Utility mysql available
echo  Make sure that mysql.exe in the environment variable
echo  or the current directory with the script installation.
goto end
:end
cls
echo ======================================================================
echo ======================================================================
echo Installation is complete, thank you for choosing our product ...
echo ======================================================================
echo ======================================================================
pause > nul