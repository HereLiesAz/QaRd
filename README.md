# QR Lockscreen Widget

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/hereliesaz/qard/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A simple Android widget that displays a QR code on your home screen or lock screen for quick and easy access.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [License](#license)

## Project Overview

This app provides a highly customizable widget that generates and displays a QR code based on user-provided data. It's perfect for sharing contact information, Wi-Fi credentials, URLs, or any other text-based information directly from your device's screen without needing to open an app.

This is not a lock screen replacement, but a widget that can be placed on your existing lock screen or home screen (requires Android 5.0+ for lock screen widgets).

## Features

*   **Customizable QR Code Data:** Set any text you want to be encoded in the QR code.
*   **Customizable Appearance:**
    *   Choose the shape of the QR code (Square, Circle).
    *   Set the foreground and background colors to match your wallpaper or theme.
*   **Easy to Use:** Simple configuration screen to set up your widget.
*   **Multiple Widgets:** Add multiple QR code widgets to your screen, each with its own configuration.

## Screenshots

*To be added: Please provide screenshots of the widget in action.*

**1. Configuration Screen:**
*(A screenshot showing the configuration screen with the text input field, shape selector, and color pickers.)*

**2. Widget on Home Screen:**
*(A screenshot of the generated QR code widget displayed on an Android home screen.)*

## Installation

### From Source

To build and install the app from the source code, you will need:

*   [Android Studio](https://developer.android.com/studio) (latest version recommended)
*   Android SDK Platform 34
*   An Android device or emulator running Android 5.0 (Lollipop) or newer.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/hereliesaz/qard.git
    ```
2.  **Open the project in Android Studio.**
3.  **Build the project:**
    *   From the menu bar, select **Build > Make Project**.
    *   Alternatively, run the following command in the project's root directory:
        ```bash
        ./gradlew build
        ```
4.  **Run the app:**
    *   Select your device from the toolbar and click the "Run" button.
    *   Alternatively, run:
        ```bash
        ./gradlew installDebug
        ```

## Usage

1.  **Add the Widget:**
    *   Go to your device's home screen or lock screen.
    *   Long-press on an empty space to bring up the widget menu.
    *   Find "QR Lockscreen Widget" in the list and drag it to your desired location.
2.  **Configure the Widget:**
    *   After adding the widget, the configuration screen will automatically open.
    *   **QR Code Data:** Enter the text you want to encode in the QR code (e.g., a URL, your email address, etc.).
    *   **Shape:** Choose between a square or circular QR code.
    *   **Foreground Color:** Enter a hex color code for the dark parts of the QR code (e.g., `#000000` for black).
    *   **Background Color:** Enter a hex color code for the light parts of the QR code (e.g., `#FFFFFF` for white).
    *   Click "Create Widget".
3.  **View the Widget:**
    *   The QR code will now be displayed on your screen.
    *   To reconfigure the widget, simply remove it and add it again.

## Project Structure

Here is a brief overview of the key files and directories in the project:

* `app/src/main/java/com/hereliesaz/qard/`: The main package for the application.
    *   `data/`: Contains the data models and data store for the widget's configuration.
        *   `QrConfig.kt`: Defines the data structure for the QR code configuration.
        *   `QrDataStore.kt`: Handles saving and loading the widget's configuration.
    *   `ui/`: Contains the UI components, including the configuration activity.
        *   `ConfigActivity.kt`: The activity that allows the user to configure the widget.
    *   `widget/`: Contains the widget-related classes.
        *   `QrWidget.kt`: The main widget class.
        *   `QrGenerator.kt`: Handles the generation of the QR code bitmap.
        *   `QrWidgetReceiver.kt`: A broadcast receiver for widget updates.
*   `app/build.gradle.kts`: The build script for the app module.
*   `build.gradle.kts`: The top-level build script for the project.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
