#!/bin/bash
# A script to correctly set up a basic Android development environment.
set -euo pipefail

# --- 1. Install Java Development Kit (JDK) ---
echo "➡️ Installing OpenJDK 17..."
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

# --- 2. Install Android Command Line Tools ---
echo "➡️ Setting up Android SDK..."

# Define paths and a known working URL
ANDROID_SDK_ROOT="$HOME/Android/sdk"
TOOLS_VERSION="11076708"
TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-${TOOLS_VERSION}_latest.zip"
TOOLS_ZIP="/tmp/android-tools.zip"

# Download and place the tools in their final destination
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
wget -q "$TOOLS_URL" -O "$TOOLS_ZIP"

# Unzip and restructure the directory
# The zip file contains a single 'cmdline-tools' folder. We unzip it and rename it to 'latest'.
rm -rf "$ANDROID_SDK_ROOT/cmdline-tools/latest"
unzip -oq "$TOOLS_ZIP" -d "$ANDROID_SDK_ROOT/cmdline-tools"
mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
rm "$TOOLS_ZIP"

# --- 3. Set Environment Variables Permanently ---
echo "➡️ Configuring environment variables..."

# Auto-detect the user's shell configuration file
if [[ "$SHELL" == */bash ]]; then
    RC_FILE="$HOME/.bashrc"
elif [[ "$SHELL" == */zsh ]]; then
    RC_FILE="$HOME/.zshrc"
else
    # Fallback for other shells like dash, fish, etc.
    echo "⚠️ Could not automatically determine shell config file. Defaulting to ~/.profile."
    echo "You may need to check this manually."
    RC_FILE="$HOME/.profile"
fi

echo "Updating shell configuration at: $RC_FILE"

# Use a function to avoid adding duplicate lines
append_if_missing() {
    CONTENT="$1"
    FILE="$2"
    if ! grep -qF -- "$CONTENT" "$FILE"; then
        echo "$CONTENT" >> "$FILE"
    fi
}

# Add environment variables to the detected config file
append_if_missing '' "$RC_FILE" # Add a newline for spacing
append_if_missing '# Android & Java Environment' "$RC_FILE"
append_if_missing 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' "$RC_FILE"
append_if_missing 'export ANDROID_SDK_ROOT=$HOME/Android/sdk' "$RC_FILE"
append_if_missing 'export ANDROID_HOME=$ANDROID_SDK_ROOT' "$RC_FILE" # For backward compatibility
append_if_missing 'export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools' "$RC_FILE"
append_if_missing 'export PATH=$JAVA_HOME/bin:$PATH' "$RC_FILE"


# --- 4. Accept SDK Licenses ---
echo "➡️ Accepting Android SDK licenses..."
export ANDROID_SDK_ROOT="$HOME/Android/sdk"
yes | "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --licenses


echo "✅ Setup complete!"
echo "Run 'source $RC_FILE' or restart your terminal to apply the changes."
