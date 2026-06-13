package com.hereliesaz.qard.ads

import android.app.Activity
import android.app.Application
import android.content.Context
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

// Google's official TEST ad unit IDs. Replace with your real AdMob unit IDs before
// publishing (the AdMob app id lives in src/play/AndroidManifest.xml). Using the
// test IDs in development is required by AdMob policy to avoid invalid traffic.
private const val BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
private const val INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

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
        }
    )
}

/**
 * Keeps a single interstitial preloaded and reloads it after each show, so the
 * Save screen can present one immediately on demand.
 */
class InterstitialController(private val context: Context) {
    private var ad: InterstitialAd? = null

    fun load() {
        InterstitialAd.load(
            context,
            INTERSTITIAL_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: InterstitialAd) {
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
                }
            }
        )
    }

    fun showIfReady(activity: Activity) {
        ad?.show(activity)
    }
}

@Composable
fun rememberInterstitialController(): InterstitialController {
    val context = LocalContext.current
    return remember { InterstitialController(context).apply { load() } }
}
