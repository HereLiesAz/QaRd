package com.hereliesaz.qrlockscreen.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.hereliesaz.qrlockscreen.data.QrDataStore
import com.hereliesaz.qrlockscreen.widget.QrWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class QrWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QrWidget()
    private val coroutineScope = MainScope()

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