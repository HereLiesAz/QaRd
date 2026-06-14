package com.hereliesaz.qard.ads

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ad-free (FOSS) implementation. Every ad entry point is a no-op so the build the
 * GitHub repo compiles ships without the AdMob SDK or any ads. The matching `play`
 * flavor (src/play) provides the real AdMob-backed implementation.
 */

fun initAds(app: Application) {
    // No ads in the FOSS build.
}

/** No banner in the FOSS build, so it reserves no space. */
val AdBannerHeight: Dp = 0.dp

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    // No ads in the FOSS build — render nothing.
}

@Composable
fun PresetsAdBanner(modifier: Modifier = Modifier) {
    // No ads in the FOSS build — render nothing.
}

class InterstitialController {
    fun showIfReady(activity: Activity) {
        // No ads in the FOSS build.
    }
}

@Composable
fun rememberInterstitialController(): InterstitialController =
    remember { InterstitialController() }
