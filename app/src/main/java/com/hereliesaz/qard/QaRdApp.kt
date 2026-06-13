package com.hereliesaz.qard

import android.app.Application
import com.hereliesaz.qard.ads.initAds

/**
 * Application entry point. [initAds] is flavor-specific: it initializes the AdMob
 * SDK in the `play` build and is a no-op in the ad-free `foss` build.
 */
class QaRdApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initAds(this)
    }
}
