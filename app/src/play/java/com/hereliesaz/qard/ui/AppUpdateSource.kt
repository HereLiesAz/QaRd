package com.hereliesaz.qard.ui

import android.app.Activity
import com.hereliesaz.aznavrail.updates.AzUpdateChecker
import com.hereliesaz.aznavrail.updates.play.AzPlayUpdates

/** Checks the installed distribution without accessing QR content. */
internal fun checkAppUpdate(activity: Activity) {
    AzUpdateChecker.check(activity, "HereLiesAz/QaRd", playSource = AzPlayUpdates)
}
