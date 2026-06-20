plugins {
    // AGP provides built-in Kotlin support (the base :app module likewise applies no
    // separate kotlin.android plugin), so applying org.jetbrains.kotlin.android here
    // would double-register the `kotlin` extension and fail.
    alias(libs.plugins.android.dynamic.feature)
}

// On-demand dynamic feature module scaffold. Today it ships a tiny placeholder
// entry point so the app-bundle / Play Feature Delivery wiring is real and
// CI-verifiable; the compile-time-coupled file-transfer feature still lives in
// :app. See docs/play-delivery.md for how to migrate a feature into this module
// and load it at runtime with SplitInstallManager.
android {
    namespace = "com.hereliesaz.qard.feature.transfer"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    // Mirror the base module's flavor dimension so this module's variants
    // (fossDebug/playDebug/…) match :app's exactly. Without it, the
    // implementation(project(":app")) dependency below is ambiguous — Gradle
    // can't choose between :app's foss and play runtime variants.
    flavorDimensions += "distribution"
    productFlavors {
        create("foss") { dimension = "distribution" }
        create("play") { dimension = "distribution" }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // A dynamic feature depends on the base app module; flavors (foss/play),
    // signing, and shared deps are all inherited from :app.
    implementation(project(":app"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
}
