import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.compose)
}

// --- Versioning -----------------------------------------------------------
// versionName is composed from version.properties (major/minor/patch); the
// build number (versionBuild) is auto-incremented on release builds.
val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        versionPropsFile.inputStream().use { load(it) }
    }
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

var currentVersionCode = versionProps.getProperty("versionBuild", "1").toInt()

// An explicit override always wins, e.g. CI passes a strictly-increasing value via
// `-PversionBuild=$(git rev-list --count HEAD)` so Play never sees a duplicate or
// lower versionCode. When no override is supplied we keep the original local
// behaviour: auto-increment on release/bundle builds and persist it to
// version.properties.
val versionBuildOverride = project.findProperty("versionBuild")?.toString()?.trim()?.toIntOrNull()
val isReleaseBuild = gradle.startParameter.taskNames.any {
    it.contains("Release", ignoreCase = true) || it.contains("bundle", ignoreCase = true)
}
if (versionBuildOverride != null) {
    currentVersionCode = versionBuildOverride
} else if (isReleaseBuild) {
    currentVersionCode++
    versionProps.setProperty("versionBuild", currentVersionCode.toString())
    versionPropsFile.outputStream().use {
        versionProps.store(it, "Auto-incremented by release build")
    }
}

val verMajor = versionProps.getProperty("versionMajor", "1")
val verMinor = versionProps.getProperty("versionMinor", "0")
val verPatch = versionProps.getProperty("versionPatch", "0")
val currentVersionName = "$verMajor.$verMinor.$verPatch"

android {
    namespace = "com.hereliesaz.qard"
    compileSdk = 37 // SDK 37 is not yet stable/standard; changing to 35 for better compatibility

    // Dynamic feature modules. These ship inside the Android App Bundle and are
    // delivered by Google Play (install-time / on-demand per each module's
    // <dist:module> manifest), keeping the base install lean. They are NOT part
    // of the standalone foss APK published to GitHub Releases.
    dynamicFeatures += ":feature_transfer"

    defaultConfig {
        applicationId = "com.hereliesaz.qard"
        minSdk = 26
        targetSdk = 37
        versionCode = currentVersionCode
        versionName = currentVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Two distributions: "foss" is the ad-free build the repo's CI compiles and
    // publishes to GitHub Releases; "play" carries the AdMob banner + interstitial
    // for the Play Store. Ad SDK, ad code, and the AdMob manifest entries live only
    // in the play flavor (src/play), so the foss artifact contains no ads at all.
    flavorDimensions += "distribution"
    productFlavors {
        create("foss") {
            dimension = "distribution"
            isDefault = true
        }
        create("play") {
            dimension = "distribution"
        }
    }

    // Release signing config sourced from local.properties (KEYSTORE points to
    // the .jks file; KEYSTORE_SECRET / KEY_SECRET / KEY_ALIAS hold the
    // credentials). Only registered when the keystore file is actually present
    // so CI (which injects signing via -Pandroid.injected.signing.* flags) and
    // contributors without the keystore can still build.
    val keystorePath = localProperties.getProperty("KEYSTORE")
    val releaseKeystore = keystorePath?.let { file(it) }
    val hasReleaseKeystore = releaseKeystore?.exists() == true
    if (hasReleaseKeystore) {
        signingConfigs.create("release") {
            storeFile = releaseKeystore
            storePassword = localProperties.getProperty("KEYSTORE_SECRET")
            keyAlias = localProperties.getProperty("KEY_ALIAS")
            keyPassword = localProperties.getProperty("KEY_SECRET")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Sign with the release keystore from local.properties when available,
            // otherwise fall back to the debug key so the build still succeeds.
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // Core & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.appcompat)

    // Glance for AppWidgets
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // DataStore for Persistence
    implementation(libs.androidx.datastore.preferences)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // QR Code Generation
    implementation(libs.qrcode.kotlin.android)

    // Color Picker
    implementation(libs.compose.color.picker)

    // Material Kolor for palette generation
    implementation(libs.material.kolor)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Menu
    implementation(libs.aznavrail)

    // Play Feature Delivery — SplitInstallManager / SplitCompat for installing and
    // loading on-demand dynamic feature modules at runtime. Proprietary Google Play
    // Core code, so it's confined to the `play` flavor to keep the FOSS build (and
    // F-Droid distribution) free of non-free dependencies. Flavor-specific
    // SplitCompatUtils (src/{foss,play}) wraps it so shared code never references it.
    "playImplementation"(libs.play.feature.delivery)

    // Local HTTP server for same-Wi-Fi file hand-off (sender side).
    implementation(libs.nanohttpd)

    // Google AdMob — play flavor only, so the foss build pulls in no ad SDK.
    "playImplementation"(libs.play.services.ads)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.constraintlayout)
}