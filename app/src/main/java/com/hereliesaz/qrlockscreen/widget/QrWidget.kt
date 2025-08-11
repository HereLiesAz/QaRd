package com.hereliesaz.qrlockscreen.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.hereliesaz.qrlockscreen.data.QrDataStore
import com.hereliesaz.qrlockscreen.widget.QrWidgetReceiver
import kotlinx.coroutines.flow.first

class QrWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val appWidgetId = getAppWidgetId(context, id)
        val config = dataStore.getConfig(appWidgetId).first()

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(createTransparentBitmap())) // Use transparent background
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (config.data.isNotBlank()) {
                    val qrBitmap = QrGenerator.generate(config)
                    if (qrBitmap != null) {
                        Image(
                            provider = ImageProvider(qrBitmap),
                            contentDescription = "User-defined QR Code",
                            modifier = GlanceModifier.fillMaxSize()
                        )
                    } else {
                        // Handle generation error
                        Text("Error generating QR Code.")
                    }
                } else {
                    // Handle empty/unconfigured state
                    Text("Tap to configure widget.")
                }
            }
        }
    }

    private suspend fun getAppWidgetId(context: Context, glanceId: GlanceId): Int {
        val glanceManager = GlanceAppWidgetManager(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, QrWidgetReceiver::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            if (glanceManager.getGlanceIdBy(appWidgetId) == glanceId) {
                return appWidgetId
            }
        }
        return AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private fun createTransparentBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(0)
        return bitmap
    }
}