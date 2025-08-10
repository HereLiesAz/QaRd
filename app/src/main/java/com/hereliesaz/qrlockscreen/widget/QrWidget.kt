package com.hereliesaz.qrLockscreen.widget

import android.content.Context
import androidx.glance.GlanceId
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
import androidx.glance.unit.dp
import com.hereliesaz.qrLockscreen.data.QrDataStore
import kotlinx.coroutines.flow.first
import androidx.compose.ui.graphics.Color as GlanceColor

class QrWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val appWidgetId = id.toString().filter { it.isDigit() }.toInt()
        val config = dataStore.getConfig(appWidgetId).first()

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceColor.White) // This can be transparent
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
}