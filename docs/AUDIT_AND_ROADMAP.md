# QaRd — App Audit & Roadmap

_Last updated: 2026-06_

This document records an audit of the QaRd codebase and a brainstormed roadmap of
features/upgrades. It accompanies the UX work that introduced a consistent
gesture model, in-app QR creation with image export, and widget resizing.

## What shipped in this pass

- **Clear gesture model** — single tap = *see the info the code contains*,
  double tap = *open the construction screen*. Applied both to the in-app
  saved-codes grid and to the home-screen widget. The widget uses
  timestamp-window tap counting (the only approach available to `RemoteViews`/
  Glance widgets, which receive click `PendingIntent`s, not a touch stream) —
  the same technique as the QuicLoc app.
- **"Preview" labels clarified** — the reused preview no longer always says a bare
  "Preview"; the saved grid drops the redundant label and the bottom sheet says
  "Preview of your saved QaRd".
- **Open app → create a QR → save as an image** — the launcher screen gained a
  "Create QR code" action that opens a standalone construction screen whose
  output is a PNG exported to the gallery (`Pictures/QaRd`).
- **Widget resize** — the widget declares `targetCell*`/`maxResize*` and uses
  Glance `SizeMode.Exact` so long-press → resize (an OS-native gesture) yields a
  crisp QR at any size.

## Audit findings (existing tech debt / correctness)

- **`ConfigActivity` is one very large composable** holding ~8 dialog booleans in
  `remember`; a `ViewModel` would make state survive process death and shrink the
  function.
- **`QrConfig` has no `id`/`label`/`createdAt`.** Saved configs are de-duped by
  whole-object equality (`.distinct()`), so identical-looking codes collide and
  you can't name, reliably edit, or delete an individual saved item. The saved
  grid even imports a `Delete` icon but exposes no delete action.
- **vCard `N:` misuse** — the structured-name field is filled with the full
  display name; spec wants a structured `N:` plus a separate `FN:`. Minor
  scanner-compatibility issue.
- **Error-correction level isn't configurable** — important before adding a
  center logo (logos eat modules and need higher ECC).
- **Limited content types** — only Links / Contact / SocialMedia; no WiFi, plain
  text, email, phone, SMS, or geo.
- **`Diamond` shape uses a `FavoriteBorder` placeholder icon** in the picker.
- **Hardcoded UI strings** in composables despite an existing `strings.xml`
  (no i18n).
- **`Agents.md` asks for a dedicated lock-screen QR surface** that keeps
  notifications off the code — not implemented.

## Roadmap (brainstorm — prioritized, not yet built)

### High value / low effort
- **Saved-item management:** add `id` + `label` + `createdAt`; rename, duplicate,
  delete, reorder, search the gallery.
- **Share sheet:** share the PNG (and/or the raw encoded data) via the Android
  share sheet, alongside save-to-gallery.
- **More content types:** WiFi (`WIFI:`), plain text, email (`mailto:`), phone
  (`tel:`), SMS, geo (`geo:`), calendar event.
- **Real `Diamond` icon** + fix the vCard `N:`/`FN:` split.

### Medium
- **Scan / import:** use the camera to scan an existing QR and clone it into the
  editor.
- **Branding:** center logo/avatar embedding + selectable error-correction level;
  separate "eye" color from body color; rounded-eye styles.
- **Export options:** SVG / PDF / vector output for print, and a selectable export
  resolution.
- **Launcher integration:** app shortcuts (long-press the icon → "New QR code",
  "Scan"); a themed/monochrome icon and Material You dynamic color.
- **Backup / restore:** export/import all configs as JSON.

### Larger
- **Dedicated lock-screen surface** per `Agents.md` that displays the QR and keeps
  notifications from covering it.
- **`ViewModel` + repository refactor** for `ConfigActivity` and the data layer.
- **Accessibility pass:** real content descriptions, larger tap targets, dynamic
  type support, and an instrumented-test suite (currently only stub tests exist).
