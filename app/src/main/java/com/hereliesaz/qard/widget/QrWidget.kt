package com.hereliesaz.qard.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.ui.ConfigActivity
import kotlinx.coroutines.flow.first

class QrWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val appWidgetId = getAppWidgetId(context, id)
        Log.d("WidgetFlow", "provideGlance for widget ID: $appWidgetId")
        val config = dataStore.getConfig(appWidgetId).first()
        Log.d("WidgetFlow", "Config received in widget: $config")

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(createTransparentBitmap())) // Use transparent background
                    .padding(8.dp)
                    .clickable(
                        onClick = actionStartActivity(
                            Intent(context, ConfigActivity::class.java).apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val dataIsNotBlank = config.data.any {
                    when (it) {
                        is QrData.Links -> it.links.any { link -> link.isNotBlank() }
                        is QrData.Contact -> it.name.isNotBlank()
                        is QrData.SocialMedia -> it.links.any { social -> social.url.isNotBlank() }
                    }
                }
                Log.d("WidgetFlow", "Widget dataIsNotBlank: $dataIsNotBlank")
                if (dataIsNotBlank) {
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

    private fun getAppWidgetId(context: Context, glanceId: GlanceId): Int {
        return GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
    }

    private fun createTransparentBitmap(): Bitmap {
        val bitmap = createBitmap(1, 1)
        bitmap.eraseColor(0)
        return bitmap
    }
}