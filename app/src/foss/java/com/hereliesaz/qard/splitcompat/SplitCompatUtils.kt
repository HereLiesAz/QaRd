package com.hereliesaz.qard.splitcompat

import android.app.Activity
import android.content.Context

/**
 * FOSS (ad-free) implementation. The FOSS build ships as a single APK distributed
 * outside Google Play, so there are no on-demand splits to attach and no proprietary
 * Play Core dependency — every call is a no-op. The matching `play` flavor (src/play)
 * provides the real SplitCompat-backed implementation.
 */
object SplitCompatUtils {
    fun install(context: Context) {
        // No dynamic feature splits in the FOSS build.
    }

    fun installActivity(activity: Activity) {
        // No dynamic feature splits in the FOSS build.
    }
}
