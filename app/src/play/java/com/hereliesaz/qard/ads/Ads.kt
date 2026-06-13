package com.hereliesaz.qard.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

// Real AdMob unit IDs (the AdMob app id lives in src/play/AndroidManifest.xml).
// BANNER_UNIT_ID = "qard-bottom-banner"; INTERSTITIAL_UNIT_ID = "qard-interstitial".
private const val BANNER_UNIT_ID = "ca-app-pub-7304740804770627/1570507628"
private const val INTERSTITIAL_UNIT_ID = "ca-app-pub-7304740804770627/6036324683"

fun initAds(app: Application) {
    MobileAds.initialize(app) {}
}

/** A standard 320x50 banner; placed at the bottom of each screen. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        },
        // Release the AdView when it leaves composition to avoid a memory leak.
        onRelease = { adView -> adView.destroy() }
    )
}

/**
 * Keeps a single interstitial preloaded and reloads it after each show, so the
 * Save screen can present one immediately on demand. Holds the application
 * context (never an Activity) so it's safe to retain across configuration changes.
 */
class InterstitialController(context: Context) {
    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var ad: InterstitialAd? = null
    private var retryAttempt = 0

    fun load() {
        InterstitialAd.load(
            appContext,
            INTERSTITIAL_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: InterstitialAd) {
                    retryAttempt = 0
                    loaded.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            ad = null
                            load()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            ad = null
                            load()
                        }
                    }
                    ad = loaded
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    ad = null
                    // Retry with capped exponential backoff so a transient network
                    // failure doesn't disable interstitials for the whole session.
                    if (retryAttempt < MAX_RETRIES) {
                        retryAttempt++
                        val delayMs = (1000L shl retryAttempt).coerceAtMost(60_000L)
                        handler.postDelayed({ load() }, delayMs)
                    }
                }
            }
        )
    }

    fun showIfReady(activity: Activity) {
        ad?.show(activity)
    }

    companion object {
        private const val MAX_RETRIES = 5
    }
}

@Composable
fun rememberInterstitialController(): InterstitialController {
    // Use the application context so the remembered controller never retains an Activity.
    val context = LocalContext.current.applicationContext
    return remember(context) { InterstitialController(context).apply { load() } }
}
