@echo off
setlocal

REM Configuration
set APP_NAME=SimpleLauncher
set APP_VERSION=1.0
set MAIN_CLASS=com.launcher.Main

REM Resolve directories
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
set LAUNCHER_DIR=%PROJECT_ROOT%\launcher
set DIST_DIR=%LAUNCHER_DIR%\dist
set TARGET_DIR=%LAUNCHER_DIR%\target
set MAIN_JAR=simple-launcher-1.0-SNAPSHOT.jar

REM Ensure JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME is not set.
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%

REM Prepare directories
cd /d "%LAUNCHER_DIR%"
if errorlevel 1 exit /b 1

REM Clean and package with Maven
echo Building project...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo Maven build failed.
    exit /b 1
)

REM Remove existing dist directory
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%DIST_DIR%"

REM 1. Create App Image
echo Creating application image...
"%JAVA_HOME%\bin\jpackage" ^
  --name "%APP_NAME%" ^
  --input "%TARGET_DIR%" ^
  --main-jar "%MAIN_JAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --type app-image ^
  --dest "%DIST_DIR%" ^
  --app-version "%APP_VERSION%" ^
  --java-options "-Xmx256m" ^
  --win-console ^
  --verbose

if errorlevel 1 (
    echo App Image creation failed.
    exit /b 1
)

REM 2. Create EXE Installer
echo Packaging into EXE...
"%JAVA_HOME%\bin\jpackage" ^
  --name "%APP_NAME%" ^
  --app-image "%DIST_DIR%\%APP_NAME%" ^
  --type exe ^
  --dest "%DIST_DIR%" ^
  --app-version "%APP_VERSION%" ^
  --win-menu ^
  --win-shortcut ^
  --verbose

if errorlevel 1 (
    echo Packaging failed.
    exit /b 1
)

echo Packaging successful! Check the 'dist' directory.
pause
