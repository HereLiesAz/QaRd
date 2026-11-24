package com.hereliesaz.qard.ui

import android.app.Activity
import com.hereliesaz.aznavrail.updates.AzUpdateChecker

/** Checks the installed distribution without accessing QR content. */
internal fun checkAppUpdate(activity: Activity) {
    AzUpdateChecker.check(activity, "HereLiesAz/QaRd")
}
