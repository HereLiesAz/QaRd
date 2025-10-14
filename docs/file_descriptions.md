# File Descriptions

This file contains a list of all non-ignored files in the project and a brief description of what each file is supposed to do.

## `.github/workflows/android-ci.yml`

This file is a GitHub Actions workflow that runs continuous integration checks on the Android project. It is triggered on every push and pull request to the `main` branch.

## `.github/workflows/android-cl.yml`

This file is a GitHub Actions workflow that runs continuous integration checks on the Android project. It is triggered on every push and pull request to the `main` branch.

## `.github/workflows/generate-apk-aab-debug-release.yml`

This file is a GitHub Actions workflow that generates debug and release APKs and AABs.

## `.github/workflows/generate-wrapper.yaml`

This file is a GitHub Actions workflow that generates a Gradle wrapper.

## `.github/workflows/generate-wrapper.yml`

This file is a GitHub Actions workflow that generates a Gradle wrapper.

## `.github/workflows/gradle.yaml`

This file is a GitHub Actions workflow that runs Gradle checks.

## `.github/workflows/release.yml`

This file is a GitHub Actions workflow that creates a release on GitHub.

## `.github/workflows/setup-java.yml`

This file is a GitHub Actions workflow that sets up Java for the other workflows.

## `.gitignore`

This file specifies which files and directories should be ignored by Git.

## `APP_DESCRIPTION.md`

This file contains the app description for the QaRd Widget, including a short and full description, key features, and how it works.

## `Agents.md`

This file contains instructions for the agent on how to approach the project.

## `CHANGELOG.md`

This file documents all notable changes to the project.

## `LICENSE`

This file contains the MIT License for the project.

## `PRIVACY_POLICY.md`

This file explains that the app does not collect any personal data.

## `README.md`

This file provides a project overview, features, installation instructions, and usage guide.

## `RELEASE_NOTES.md`

This file contains the release notes for version 1.0.0 of the QaRd Widget.

## `app/build.gradle.kts`

This file is the Gradle build script for the application module. It defines the app's dependencies, build types, and other build configurations.

## `app/src/main/AndroidManifest.xml`

This file is the Android Manifest file for the application. It declares the app's components, permissions, and other essential information.

## `app/src/main/java/com/hereliesaz/qard/data/QrConfig.kt`

This file defines the data classes and enums related to the QR code configuration, such as `QrConfig`, `QrData`, `QrShape`, etc.

## `app/src/main/java/com/hereliesaz/qard/data/QrDataStore.kt`

This file provides a class for storing and retrieving QR code configurations using Android's DataStore.

## `app/src/main/java/com/hereliesaz/qard/ui/ConfigActivity.kt`

This file contains the activity for configuring the QR code widget.

## `app/src/main/java/com/hereliesaz/qard/ui/theme/Color.kt`

This file defines the color palette for the app's theme.

## `app/src/main/java/com/hereliesaz/qard/ui/theme/QaRdTheme.kt`

This file sets up the theme for the app, including the color scheme and typography.

## `app/src/main/java/com/hereliesaz/qard/ui/theme/Type.kt`

This file defines the typography for the app's theme.

## `app/src/main/java/com/hereliesaz/qard/widget/QrGenerator.kt`

This file contains the logic for generating a QR code bitmap from a `QrConfig` object.

## `app/src/main/java/com/hereliesaz/qard/widget/QrWidget.kt`

This file implements the GlanceAppWidget for the QR code widget.

## `app/src/main/java/com/hereliesaz/qard/widget/QrWidgetReceiver.kt`

This file is the receiver for the QR code widget.

## `app/src/main/java/com/hereliesaz/qard/widget/shape/DiamondShapeFunction.kt`

This file defines a custom shape function for the QR code generator to draw diamonds.

## `app/src/main/res/drawable/ic_launcher.webp`

This is the launcher icon for the app.

## `app/src/main/res/layout/activity_main.xml`

This is the layout file for the main activity.

## `app/src/main/res/layout/widget_qr.xml`

This is the layout file for the QR code widget.

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

This is the adaptive launcher icon for the app.

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

This is the round adaptive launcher icon for the app.

## `app/src/main/res/mipmap-hdpi/ic_launcher.webp`

This is the launcher icon for high-density screens.

## `app/src/main/res/mipmap-hdpi/ic_launcher_foreground.webp`

This is the foreground of the launcher icon for high-density screens.

## `app/src/main/res/mipmap-hdpi/ic_launcher_round.webp`

This is the round launcher icon for high-density screens.

## `app/src/main/res/mipmap-mdpi/ic_launcher.webp`

This is the launcher icon for medium-density screens.

## `app/src/main/res/mipmap-mdpi/ic_launcher_foreground.webp`

This is the foreground of the launcher icon for medium-density screens.

## `app/src/main/res/mipmap-mdpi/ic_launcher_round.webp`

This is the round launcher icon for medium-density screens.

## `app/src/main/res/mipmap-xhdpi/ic_launcher.webp`

This is the launcher icon for extra-high-density screens.

## `app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.webp`

This is the foreground of the launcher icon for extra-high-density screens.

## `app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp`

This is the round launcher icon for extra-high-density screens.

## `app/src/main/res/mipmap-xxhdpi/ic_launcher.webp`

This is the launcher icon for extra-extra-high-density screens.

## `app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp`

This is the foreground of the launcher icon for extra-extra-high-density screens.

## `app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp`

This is the round launcher icon for extra-extra-high-density screens.

## `app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp`

This is the launcher icon for extra-extra-extra-high-density screens.

## `app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp`

This is the foreground of the launcher icon for extra-extra-extra-high-density screens.

## `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp`

This is the round launcher icon for extra-extra-extra-high-density screens.

## `app/src/main/res/values/colors.xml`

This file defines the colors for the app.

## `app/src/main/res/values/ic_launcher_background.xml`

This file defines the background color for the launcher icon.

## `app/src/main/res/values/strings.xml`

This file defines the strings for the app.

## `app/src/main/res/values/themes.xml`

This file defines the themes for the app.

## `app/src/main/res/values-night/themes.xml`

This file defines the themes for the app in night mode.

## `app/src/main/res/xml/backup_rules.xml`

This file defines the backup rules for the app.

## `app/src/main/res/xml/data_extraction_rules.xml`

This file defines the data extraction rules for the app.

## `app/src/main/res/xml/qr_widget_info.xml`

This file provides metadata for the QR code widget.
