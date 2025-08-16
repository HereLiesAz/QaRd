package com.hereliesaz.qard.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.hereliesaz.qard.data.QrDataStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class QrWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QrWidget()
    private val coroutineScope = MainScope()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        coroutineScope.launch {
            val glanceAppWidgetManager = GlanceAppWidgetManager(context)
            appWidgetIds.forEach { appWidgetId ->
                val glanceId = glanceAppWidgetManager.getGlanceIdBy(appWidgetId)
                glanceAppWidget.update(context, glanceId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val dataStore = QrDataStore(context)
        coroutineScope.launch {
            appWidgetIds.forEach { id ->
                dataStore.deleteConfig(id)
            }
        }
    }
}