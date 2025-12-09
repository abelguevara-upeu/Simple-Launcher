@echo off
set "APP_NAME=SimpleLauncher"
set "APP_VERSION=1.0"
set "INPUT_DIR=target"
set "MAIN_JAR=simple-launcher-1.0-SNAPSHOT.jar"
set "MAIN_CLASS=com.launcher.Main"
set "OUTPUT_DIR=dist-win"

echo Building JAR...
call mvn clean package

if %errorlevel% neq 0 (
    echo Maven build failed!
    pause
    exit /b %errorlevel%
)

echo Packaging %APP_NAME% for Windows...
rmdir /s /q "%OUTPUT_DIR%"

jpackage ^
  --name "%APP_NAME%" ^
  --app-version "%APP_VERSION%" ^
  --input "%INPUT_DIR%" ^
  --main-jar "%MAIN_JAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --type exe ^
  --dest "%OUTPUT_DIR%" ^
  --java-options "-Xmx2048m" ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-menu ^
  --verbose

if %errorlevel% neq 0 (
    echo jpackage failed!
    pause
    exit /b %errorlevel%
)

echo Packaging complete. Check %OUTPUT_DIR%
pause
