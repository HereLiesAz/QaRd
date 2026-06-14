package com.hereliesaz.qard.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.data.hasInfo

class QrWidget : GlanceAppWidget() {

    // Recompose with the exact size the launcher gives us so the QR stays crisp
    // when the user long-presses the widget and resizes it.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        Log.d("WidgetFlow", "provideGlance for widget ID: $appWidgetId (from GlanceId $id)")

        provideContent {
            val config by dataStore.getConfig(appWidgetId).collectAsState(initial = null)
            Log.d("WidgetFlow", "Config received in widget (for ID $appWidgetId): $config")

            // Every tap is routed through WidgetTapRouter, which distinguishes
            // single tap (open detail) from double tap (open construction).
            val action = actionRunCallback<WidgetTapAction>(
                parameters = actionParametersOf(
                    ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID) to appWidgetId
                )
            )

            // Always emit a root Box so Glance never renders empty ("can't load
            // widget"); while config is still loading the box stays blank, which
            // avoids flashing the "Tap to configure" placeholder on every update.
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(createTransparentBitmap()))
                    .padding(8.dp)
                    .clickable(action),
                contentAlignment = Alignment.Center
            ) {
                val currentConfig = config
                if (currentConfig != null) {
                    val dataIsNotBlank = currentConfig.data.any {
                        when (it) {
                            is QrData.Links -> it.links.any { link -> link.isNotBlank() }
                            is QrData.Contact -> it.hasInfo()
                            is QrData.SocialMedia -> it.links.any { social -> social.url.isNotBlank() }
                        }
                    }

                    if (dataIsNotBlank) {
                        val qrBitmap = QrGenerator.generate(currentConfig)?.scaledForWidget()
                        if (qrBitmap != null) {
                            Image(
                                provider = ImageProvider(qrBitmap),
                                contentDescription = "User-defined QR Code",
                                modifier = GlanceModifier.fillMaxSize()
                            )
                        } else {
                            Text("Error generating QR Code.")
                        }
                    } else {
                        Text("Tap to configure")
                    }
                }
            }
        }
    }

    private fun createTransparentBitmap(): Bitmap {
        val bitmap = createBitmap(1, 1)
        bitmap.eraseColor(0) // Makes the bitmap transparent
        return bitmap
    }

    // A widget's RemoteViews can't carry a multi-MB bitmap across the binder —
    // the launcher then shows "can't show content". The generated QR can be
    // 700px+ (several MB as ARGB), so cap it to a safe, still-scannable size.
    private fun Bitmap.scaledForWidget(maxPx: Int = 384): Bitmap {
        val largest = maxOf(width, height)
        if (largest <= maxPx) return this
        val scale = maxPx.toFloat() / largest
        return Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    }
}
