package com.hereliesaz.qard.feature.transfer

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hereliesaz.qard.splitcompat.SplitCompatUtils

/**
 * Placeholder entry point for the on-demand `:feature_transfer` dynamic feature module.
 *
 * The base app launches this by class name only after the module has been installed
 * with `SplitInstallManager` (see docs/play-delivery.md). Because the module's code and
 * resources are delivered on-demand, every component in it must install SplitCompat in
 * `attachBaseContext` so the freshly-downloaded split is visible to this process. That
 * goes through [SplitCompatUtils] (flavor-specific: real on `play`, a no-op on FOSS) so
 * this module pulls in no proprietary Play Core code in the FOSS build.
 *
 * Replace this with the real UI when the file-transfer feature is migrated here.
 */
class TransferFeatureActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Make the on-demand module's resources/code available to this component.
        SplitCompatUtils.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = TextView(this).apply {
            text = getString(com.hereliesaz.qard.R.string.feature_transfer_title)
            gravity = Gravity.CENTER
        }
        setContentView(view)
    }
}
