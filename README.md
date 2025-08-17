# QaRd

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/hereliesaz/qard/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Design your QR code widget, shared like a digital business card or contact.


## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Installation](#installation)
- [Usage](#usage)
- [Lock Screen Availability](#lock-screen-availability)
- [License](#license)

## Project Overview

Share your contact info, files, or social media with a QR code widget. A simple yet customizable QaRd design to share information quickly without digging through apps.

## Features

*   **Instant Access:** Keep a QR code of your choice always visible on your screen. Perfect for sharing your Wi-Fi password, contact details, a link to your portfolio, or any other information you need to share frequently.
*   **Fully Customizable:** Make the widget your own!
    *   **QR Code Data:** Set any text you want to be encoded.
    *   **Shape:** Choose between a classic square or a modern circular QR code design.
    *   **Colors:** Pick custom foreground and background colors to perfectly match your wallpaper and theme.
*   **Simple and Lightweight:** The app is easy to use. No unnecessary permissions. Just a focus on doing one thing well.
*   **Multiple Widgets:** Need more than one QaRd? You just effin' do it, and BAM! Plural effin' QR codes. Put bunches of QaRds on your screen, you clever you.

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

**How To Work It:**

1.  Add the QaRd widget to your home screen.
2.  On the config screen, include your QaRd info.
3.  Customize the QaRd design.
4.  Huzzah. Your home screen now displays your QaRd. Resize if you think size matters. 

## Lock Scree Availability

**Important Note on Lock Screen Availability:**

The ability to place widgets directly on the **lock screen depends** on your Android version and device manufacturer.

For all other supported Android devices, the widget works perfectly on the **home screen**.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
