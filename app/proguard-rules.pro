# Add project specific ProGuard rules here.
# Minification (isMinifyEnabled) is ON for the release build — see
# app/build.gradle.kts and docs/play-delivery.md. These rules
# keep reflection/serialization working under R8.

# --- kotlinx.serialization -------------------------------------------------
# The plugin generates a synthetic Companion + .Companion.serializer(); keep
# generated serializers and the @Serializable types they belong to. (The library
# also ships consumer rules, but we keep these explicit for our own models.)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.hereliesaz.qard.** {
    *** Companion;
}
-keepclasseswithmembers class com.hereliesaz.qard.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.hereliesaz.qard.**$$serializer { *; }

# --- NanoHTTPD -------------------------------------------------------------
# Reflectively-touched HTTP server used by the file-transfer feature.
-keep class org.nanohttpd.** { *; }
-dontwarn org.nanohttpd.**

# --- Play Feature Delivery / SplitCompat -----------------------------------
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# Dynamic feature entry points are loaded by class name after install; keep them.
-keep class com.hereliesaz.qard.feature.** { *; }
