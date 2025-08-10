package com.hereliesaz.qrlockscreen.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.hereliesaz.qrlockscreen.data.QrDataStore
import kotlinx.coroutines.flow.first

class QrWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val config = dataStore.getConfig(getAppWidgetId(id)).first()

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

    private fun getAppWidgetId(glanceId: GlanceId): Int {
        // This is a temporary and fragile way to get the appWidgetId
        return glanceId.toString().filter { it.isDigit() }.toIntOrNull() ?: -1
    }

    private fun createTransparentBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(0)
        return bitmap
    }
}