#!/bin/bash

# Configuration
APP_NAME="SimpleLauncher"
APP_VERSION="1.0"
MAIN_CLASS="com.launcher.Main"

# Resolve directories
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
LAUNCHER_DIR="$PROJECT_ROOT/launcher"
DIST_DIR="$LAUNCHER_DIR/dist"
TARGET_DIR="$LAUNCHER_DIR/target"

# Ensure we are in the launcher directory for Maven operations
cd "$LAUNCHER_DIR" || exit 1

MAIN_JAR="$TARGET_DIR/simple-launcher-1.0-SNAPSHOT.jar"

# Ensure JAVA_HOME is set to a JDK 17+ (jpackage is required)
if [ -z "$JAVA_HOME" ]; then
    echo "Error: JAVA_HOME is not set."
    exit 1
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Clean and package with Maven
echo "Building project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Maven build failed."
    exit 1
fi

# Remove existing dist directory
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

# 1. Create App Image (unpacked .app)
echo "Creating application image..."
$JAVA_HOME/bin/jpackage \
  --name "$APP_NAME" \
  --input "$TARGET_DIR" \
  --main-jar "simple-launcher-1.0-SNAPSHOT.jar" \
  --main-class "$MAIN_CLASS" \
  --type app-image \
  --dest "$DIST_DIR" \
  --app-version "$APP_VERSION" \
  --java-options "-Xmx256m" \
  --mac-package-name "SimpleLauncher" \
  --verbose

if [ $? -ne 0 ]; then
    echo "App Image creation failed."
    exit 1
fi

# 2. Fix Missing Java Binary (Critical for ProcessBuilder)
# jpackage might strip bin/java, so we manually copy it back.
APP_PATH="$DIST_DIR/$APP_NAME.app"
RUNTIME_HOME="$APP_PATH/Contents/runtime/Contents/Home"
mkdir -p "$RUNTIME_HOME/bin"
cp "$JAVA_HOME/bin/java" "$RUNTIME_HOME/bin/java"
chmod +x "$RUNTIME_HOME/bin/java"
echo "Restored bin/java to bundled runtime."

# 3. Create DMG from the modified App Image
echo "Packaging into DMG..."
$JAVA_HOME/bin/jpackage \
  --name "$APP_NAME" \
  --app-image "$APP_PATH" \
  --type dmg \
  --dest "$DIST_DIR" \
  --app-version "$APP_VERSION" \
  --mac-package-name "SimpleLauncher" \
  --verbose

if [ $? -eq 0 ]; then
    echo "Packaging successful! Check the 'dist' directory: $DIST_DIR"
else
    echo "Packaging failed."
    exit 1
fi
