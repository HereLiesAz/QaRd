# File Descriptions

This file contains a list of all non-ignored files in the project and a brief description of what each file is supposed to do.

## `.github/workflows/`

- `android-ci.yml`: GitHub Actions workflow that runs CI checks on push/PR to `main`.
- `build.yml`: Workflow for building the project.
- `generate-wrapper.yml`: Workflow that ensures the Gradle wrapper is present and up to date.
- `gradle.yaml`: Runs Gradle-based checks.
- `setup-java.yml`: Shared step to set up the Java environment.

## Root Files

- `.gitignore`: Specifies files and directories ignored by Git.
- `APP_DESCRIPTION.md`: Marketing description of the app for stores.
- `Agents.md`: High-level goal/prompt for the project's AI agents.
- `CHANGELOG.md`: Log of all notable changes to the project.
- `LICENSE`: The MIT License text.
- `PRIVACY_POLICY.md`: Explains data handling (offline-only, no collection).
- `README.md`: Project overview, features, and usage guide.
- `RELEASE_NOTES.md`: Human-readable summary of changes for each release.
- `build.gradle.kts`: Root Gradle build configuration.
- `settings.gradle`: Project structure and repository definitions.
- `version.properties`: Current version information (Major.Minor.Patch.Build).
- `setup.sh`: Script for setting up the development environment.

## `app/` (Android Application Module)

- `build.gradle.kts`: Application-level build script, dependencies, and Android settings.
- `src/main/AndroidManifest.xml`: Android manifest declaring components (Activities, Receivers) and permissions.

### `app/src/main/java/com/hereliesaz/qard/`

- `data/QrConfig.kt`: Data models for QR configurations, including data, shape, and colors.
- `data/QrDataStore.kt`: Handles persistent storage of saved QR configurations using Jetpack DataStore.
- `ui/MainActivity.kt`: The main entry point, showing the grid of saved QaRds and actions to create new ones.
- `ui/ConfigActivity.kt`: The editor/creation screen for customizing QR code data and appearance.
- `ui/DetailActivity.kt`: Displays a large view of a QR code with its decoded contents and actionable links.
- `ui/theme/`: Contains Compose theme definitions (Color, Type, Theme).
- `widget/QrGenerator.kt`: Core logic to render a QR code Bitmap from a configuration.
- `widget/QrImageExporter.kt`: Utility to save generated QR codes as PNG files to the device gallery.
- `widget/QrWidget.kt`: Implementation of the Home Screen widget using Jetpack Glance.
- `widget/QrWidgetReceiver.kt`: BroadcastReceiver that handles widget updates and lifecycle.
- `widget/WidgetTapRouter.kt`: Manages the single-tap vs. double-tap logic for widget interactions.
- `widget/shape/DiamondShapeFunction.kt`: Custom shape function for diamond-style QR modules.

### `commonMain/` (Shared Code)

- `qrcode/QRCodeShapesEnum.kt`: Defines the available QR code shape types.
- `qrcode/shape/`: Contains various `QRCodeShapeFunction` implementations (Circle, Default, RoundSquares, etc.) used by the generator.

## Resources

- `app/src/main/res/layout/widget_qr.xml`: XML layout used as a fallback or for widget rendering.
- `app/src/main/res/values/`: Strings, colors, and themes.
- `app/src/main/res/xml/`: Metadata for the widget (`qr_widget_info.xml`) and backup/extraction rules.
- `app/src/main/res/mipmap-*/`: App and launcher icons.
