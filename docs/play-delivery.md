# Google Play delivery — App Bundle, signing & modular delivery

How QaRd is packaged and shipped to Google Play: a **signed Android App Bundle (AAB)**
with a **monotonic versionCode**, automatic device splits, and a dynamic feature module
scaffold for on-demand delivery.

> The ad-free **foss** flavor still ships as a single signed **APK** to GitHub Releases
> via `.github/workflows/build.yml`. The Play path described here builds the **play**
> flavor (with the AdMob SDK) as an **AAB**.

---

## 1. Build a signed AAB locally

The release `signingConfig` reads a git-ignored `local.properties`:

```properties
# local.properties (never commit this)
KEYSTORE=/absolute/path/to/upload-keystore.jks
KEYSTORE_SECRET=<keystore password>
KEY_ALIAS=<key alias>
KEY_SECRET=<key password>
```

Then:

```bash
# Play flavor, signed, AAB:
./gradlew bundlePlayRelease

# Output:
app/build/outputs/bundle/playRelease/app-play-release.aab
```

If no keystore is configured the build falls back to the debug key (so contributors can
still build), which Play will reject — configure the keystore above for real uploads.

### versionCode override

`versionName` comes from `version.properties` (`major.minor.patch`). `versionCode` is:

- **`-PversionBuild=<n>` when passed** — wins over everything. CI passes
  `git rev-list --count HEAD`, which strictly increases on every commit, so Play never
  sees a duplicate or lower code.
- **auto-increment otherwise** — local release/bundle builds bump `versionBuild` in
  `version.properties` and write it back (original behaviour, unchanged).

```bash
./gradlew bundlePlayRelease -PversionBuild=$(git rev-list --count HEAD)
```

---

## 2. Publish via CI

`.github/workflows/play-release.yml` is `workflow_dispatch`-triggered with inputs:

| Input | Default | Meaning |
| --- | --- | --- |
| `track` | `internal` | `internal` / `alpha` / `beta` / `production` |
| `status` | `draft` | `draft` (staged in console) or `completed` (rolled out) |
| `publish` | `false` | **off** = build + upload the `.aab` as a CI artifact only; **on** = also push to Play |

The job: checkout (full history) → materialize the upload keystore from secrets → JDK 17
+ Gradle → `bundlePlayRelease` with the commit-count versionCode and injected signing →
upload the `.aab` artifact → (if `publish`) `r0adkll/upload-google-play@v1`.

Defaults are safe: with `publish=false` nothing reaches Play — you just get the signed
bundle as a downloadable artifact to sanity-check.

---

## 3. Modular delivery & size

### Automatic splits (free)

An AAB is **not** a single APK. Google Play generates and serves per-device **density**,
**ABI**, and **language** split APKs from it automatically — each user downloads only what
their device needs. No extra artifacts to build or configure; this is on the moment you
ship an AAB instead of an APK.

### Dynamic feature module: `:feature_transfer`

`:feature_transfer` is a `com.android.dynamic-feature` module wired into `settings.gradle`
and the base module's `android.dynamicFeatures`. Its manifest declares **on-demand**
delivery (`<dist:delivery><dist:on-demand/>`), so its code/resources are **not** in the
base install — Play delivers them at runtime on request.

Today it ships a small placeholder (`TransferFeatureActivity`) so the bundle/Play Feature
Delivery wiring is real and CI-verifiable. The file-transfer feature itself
(`LocalFileServer` / `NetworkUtils` / `FileSendSection`) still lives in `:app` because it
is compile-time-coupled to `ConfigActivity`; migrating it on-demand is a follow-up that
needs the runtime-load indirection below plus a device test.

**Loading an on-demand module at runtime** (from the base app):

```kotlin
val manager = SplitInstallManagerFactory.create(context)
val request = SplitInstallRequest.newBuilder()
    .addModule("feature_transfer")
    .build()
manager.startInstall(request)
    .addOnSuccessListener { /* module installed; launch its Activity by class name */ }
    .addOnFailureListener { /* surface error / retry */ }
```

Both the base `Application` (`QaRdApp.attachBaseContext`) and the module's Activity install
`SplitCompat` so freshly-downloaded splits are visible to the running process. The base
module stays installable on its own; on-demand modules only resolve when the app is
installed **from Play** (a sideloaded/foss APK has no SplitInstall backend).

### R8 / minification

`isMinifyEnabled = false` for `release` today. Enabling R8 + resource shrinking would
shrink the bundle further, but the app uses `kotlinx.serialization` (`@Serializable`
models), reflective NanoHTTPD, Glance, and on-demand class loading — all of which need
correct keep rules. `app/proguard-rules.pro` already contains those rules, but **enabling
R8 must be validated on a device build first** (serialization round-trips, the widget, and
file transfer), so it is left off until then.

### Play in-app updates (optional follow-up)

For nudging users onto newer versions, consider the Play in-app updates API
(`com.google.android.play:app-update-ktx`) — flexible or immediate flows. Not wired up
here; noted as a follow-up.

---

## 4. Required repo secrets

| Secret | Used for |
| --- | --- |
| `KEYSTORE_PRIVATE` | Upload key private key (PEM) — assembled into the JKS in CI |
| `KEYSTORE_CHAIN` | Upload key certificate chain (PEM) |
| `KEYSTORE_PASSWORD` | Keystore + store password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | Google Cloud service-account JSON with Play publishing access |

The signing secrets match the names already used by `build.yml`; only
`PLAY_SERVICE_ACCOUNT_JSON` is new.

### One-time Play setup (maintainer)

1. In **Google Cloud Console**, create a service account; create a JSON key for it and
   store the JSON as the `PLAY_SERVICE_ACCOUNT_JSON` repo secret.
2. In the **Play Console** → *Users & permissions*, invite that service account and grant
   it release permissions (at least the target track).
3. **The first release of a brand-new app must be uploaded manually** in the Play Console
   before the API can publish subsequent builds. Until that first manual upload exists,
   `r0adkll/upload-google-play` will fail.
4. Recommended: keep **Play App Signing** enabled — the keystore above is then your
   *upload* key, and Play re-signs with the app signing key it holds.

---

## 5. Privacy / Data safety

The **play** flavor bundles the **AdMob** SDK, which collects the advertising ID and
device/usage signals and sends them to Google for ads. This has Play **Data safety**
implications:

- Declare data collection/sharing for advertising in the Play Console Data safety form.
- The merged `play` manifest includes the `com.google.android.gms.permission.AD_ID`
  permission (added by `play-services-ads`); declare advertising-ID usage accordingly.
- Provide a privacy policy URL covering ads/third-party data.

The **foss** flavor contains **no** ad SDK and makes no third-party network calls beyond
the user-initiated same-Wi-Fi file hand-off, so its data-safety surface is minimal.
