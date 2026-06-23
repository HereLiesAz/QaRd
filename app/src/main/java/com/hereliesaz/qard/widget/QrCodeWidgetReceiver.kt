package com.hereliesaz.qard.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import com.hereliesaz.qard.data.QrDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QrCodeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QrWidget()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PIN_SUCCESS) {
            handlePinSuccess(context, intent)
            return
        }
        super.onReceive(context, intent)
    }

    private fun handlePinSuccess(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val store = QrDataStore(context)
                val config = store.takePendingPinConfig() ?: return@launch
                store.saveConfig(appWidgetId, config)
                val saved = store.getSavedConfigs().first()
                store.saveConfigs((saved + config).distinct())
                QrWidget().updateAll(context)
            } catch (e: Exception) {
                Log.e("QrCodeWidgetReceiver", "Failed to handle pin success for widget $appWidgetId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ACTION_PIN_SUCCESS = "com.hereliesaz.qard.action.PIN_QR_CODE_WIDGET_SUCCESS"

        fun pinSuccessCallback(context: Context): PendingIntent {
            val intent = Intent(context, QrCodeWidgetReceiver::class.java).apply {
                action = ACTION_PIN_SUCCESS
                component = ComponentName(context, QrCodeWidgetReceiver::class.java)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, 1, intent, flags)
        }
    }
}
