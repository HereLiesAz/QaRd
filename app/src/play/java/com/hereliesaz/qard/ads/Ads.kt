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
// "qard-bottom-banner", "qard-presets-ad" and "qard-interstitial" respectively.
private const val BANNER_UNIT_ID = "ca-app-pub-7304740804770627/1570507628"
private const val PRESETS_BANNER_UNIT_ID = "ca-app-pub-7304740804770627/9257425959"
private const val INTERSTITIAL_UNIT_ID = "ca-app-pub-7304740804770627/6036324683"

fun initAds(app: Application) {
    MobileAds.initialize(app) {}
}

/** A standard 320x50 banner for [unitId]; the AdView is released with the composable. */
@Composable
private fun BannerAd(unitId: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        },
        // Release the AdView when it leaves composition to avoid a memory leak.
        onRelease = { adView -> adView.destroy() }
    )
}

/** Banner pinned to the bottom of every screen ("qard-bottom-banner"). */
@Composable
fun AdBanner(modifier: Modifier = Modifier) = BannerAd(BANNER_UNIT_ID, modifier)

/** Banner shown within the Presets screen ("qard-presets-ad"). */
@Composable
fun PresetsAdBanner(modifier: Modifier = Modifier) = BannerAd(PRESETS_BANNER_UNIT_ID, modifier)

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
        // Frequency cap: at most one interstitial per MIN_INTERVAL_MS (process-wide),
        // so opening the Save screen repeatedly doesn't spam ads.
        val now = System.currentTimeMillis()
        if (now - lastShownAtMs < MIN_INTERVAL_MS) return
        val current = ad ?: return
        lastShownAtMs = now
        current.show(activity)
    }

    companion object {
        private const val MAX_RETRIES = 5

        // Minimum gap between interstitials, shared across controller instances.
        private const val MIN_INTERVAL_MS = 60_000L
        private var lastShownAtMs = 0L
    }
}

@Composable
fun rememberInterstitialController(): InterstitialController {
    // Use the application context so the remembered controller never retains an Activity.
    val context = LocalContext.current.applicationContext
    return remember(context) { InterstitialController(context).apply { load() } }
}
