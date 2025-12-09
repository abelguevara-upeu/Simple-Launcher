#!/bin/bash

# Ensure we are in the project root
cd "$(dirname "$0")/../launcher"

# Build the project first
echo "Building JAR..."
mvn clean package

# Define variables
APP_NAME="SimpleLauncher"
APP_VERSION="1.0"
INPUT_DIR="target"
MAIN_JAR="simple-launcher-1.0-SNAPSHOT.jar"
MAIN_CLASS="com.launcher.Main"
OUTPUT_DIR="dist"

# Clean output dir
rm -rf $OUTPUT_DIR

# Create DMG using jpackage
# --icon needs an .icns file, skipping for now or use a placeholder if available
echo "Packaging $APP_NAME..."

jpackage \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --input "$INPUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --type dmg \
  --dest "$OUTPUT_DIR" \
  --java-options "-Xmx2048m" \
  --mac-package-name "$APP_NAME" \
  --verbose

echo "Packaging complete. Check $OUTPUT_DIR"
