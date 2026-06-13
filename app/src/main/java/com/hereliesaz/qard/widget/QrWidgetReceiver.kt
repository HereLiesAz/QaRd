package com.hereliesaz.qard.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import com.hereliesaz.qard.data.QrDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QrWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QrWidget()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PIN_SUCCESS) {
            handlePinSuccess(context, intent)
            return
        }
        super.onReceive(context, intent)
    }

    /**
     * Fired by the launcher once the user drops the pinned widget. The new widget's
     * id arrives in [AppWidgetManager.EXTRA_APPWIDGET_ID]; we pair it with the config
     * the user stashed before requesting the pin and render it.
     */
    private fun handlePinSuccess(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        // Do the DataStore I/O off the main thread; goAsync() keeps the receiver
        // alive until the coroutine finishes so we don't risk an ANR.
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val store = QrDataStore(context)
                val config = store.takePendingPinConfig() ?: return@launch
                store.saveConfig(appWidgetId, config)
                val saved = store.getSavedConfigs().first()
                store.saveConfigs((saved + config).distinct())
                QrWidget().updateAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ACTION_PIN_SUCCESS = "com.hereliesaz.qard.action.PIN_WIDGET_SUCCESS"

        /**
         * Builds the success-callback [PendingIntent] handed to
         * [AppWidgetManager.requestPinAppWidget]. The launcher adds the new widget's
         * id to this intent's extras before firing it.
         */
        fun pinSuccessCallback(context: Context): PendingIntent {
            val intent = Intent(context, QrWidgetReceiver::class.java).apply {
                action = ACTION_PIN_SUCCESS
                component = ComponentName(context, QrWidgetReceiver::class.java)
            }
            // The launcher needs to add EXTRA_APPWIDGET_ID, so the intent must be
            // mutable. Pre-API 31 PendingIntents are mutable by default.
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, 0, intent, flags)
        }
    }
}
