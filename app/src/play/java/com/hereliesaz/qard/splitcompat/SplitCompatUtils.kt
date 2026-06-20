package com.hereliesaz.qard.splitcompat

import android.app.Activity
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat

/**
 * Play implementation. Installs SplitCompat so on-demand dynamic feature modules that
 * Google Play delivers at runtime are visible to the running process / Activity. The
 * proprietary Play Core dependency lives only in this flavor (see `playImplementation`
 * in build.gradle.kts); the FOSS flavor (src/foss) provides no-op equivalents.
 */
object SplitCompatUtils {
    fun install(context: Context) {
        SplitCompat.install(context)
    }

    fun installActivity(activity: Activity) {
        SplitCompat.installActivity(activity)
    }
}
