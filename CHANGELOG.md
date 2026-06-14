# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-06-13

### Added

- Gesture model on saved QR codes and the home-screen widget: single tap opens a
  detail view of the information the code contains; double tap opens the
  construction screen. Widget double-tap uses timestamp-window tap counting.
- In-app QR creation: a "Create QR code" action on the main screen opens a
  standalone construction screen and saves the result as a PNG image to the
  device gallery (`Pictures/QaRd`).
- New detail screen (`DetailActivity`) listing decoded contents, with tappable
  links.
- `docs/AUDIT_AND_ROADMAP.md` documenting an app audit and a feature roadmap.

### Changed

- Clarified the "Preview" labeling so users understand previews are their saved
  QR codes.
- The home-screen widget declares larger resize bounds and renders crisply when
  resized (Glance `SizeMode.Exact`).

## [1.0.0] - 2025-08-11

### Added

- Initial release of QaRd Widget.
- Generate a QR code from user-provided text.
- Customize the shape of the QR code (Square or Circle).
- Customize the foreground and background colors of the QR code.
- Add multiple QR code widgets to the home screen.
- Configuration screen for setting up new widgets.
