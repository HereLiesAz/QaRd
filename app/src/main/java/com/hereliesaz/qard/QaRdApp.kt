package com.hereliesaz.qard

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import com.hereliesaz.qard.ads.initAds

/**
 * Application entry point. [initAds] is flavor-specific: it initializes the AdMob
 * SDK in the `play` build and is a no-op in the ad-free `foss` build.
 */
class QaRdApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Make on-demand dynamic feature module code/resources available to the
        // whole process after Play installs a split at runtime.
        SplitCompat.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        initAds(this)
    }
}
