package com.hereliesaz.qard.ui

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hereliesaz.qard.ui.theme.QaRdTheme

/**
 * Launcher entry point. Tapping the app icon drops the user straight into the QR
 * editor (standalone mode) — there's no intermediate landing screen. Previously
 * saved codes are reachable from the editor's "Load" rail item.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QaRdTheme {
                ConfigScreen(
                    mode = ConfigActivity.MODE_STANDALONE,
                    appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID,
                    initialConfig = null,
                    onConfigComplete = {}
                )
            }
        }
    }
}
