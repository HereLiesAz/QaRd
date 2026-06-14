package com.hereliesaz.qard.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
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
import com.hereliesaz.qard.data.QrConfig
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.data.hasInfo
import kotlinx.coroutines.flow.first

class QrWidget : GlanceAppWidget() {

    // Recompose with the exact size the launcher gives us so the QR stays crisp
    // when the user long-presses the widget and resizes it.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        // Load the config up front (provideGlance is suspend). Doing it here rather
        // than reactively inside provideContent guarantees the widget renders real
        // content on its single composition — collectAsState's later emissions
        // aren't reliably picked up by Glance, which left the tile blank/invisible.
        val config = try {
            dataStore.getConfig(appWidgetId).first()
        } catch (e: Exception) {
            Log.e("WidgetFlow", "Failed to load config for widget $appWidgetId", e)
            QrConfig()
        }

        provideContent {
            val action = actionRunCallback<WidgetTapAction>(
                parameters = actionParametersOf(
                    ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID) to appWidgetId
                )
            )

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(createTransparentBitmap()))
                    .padding(8.dp)
                    .clickable(action),
                contentAlignment = Alignment.Center
            ) {
                val dataIsNotBlank = config.data.any {
                    when (it) {
                        is QrData.Links -> it.links.any { link -> link.isNotBlank() }
                        is QrData.Contact -> it.hasInfo()
                        is QrData.SocialMedia -> it.links.any { social -> social.url.isNotBlank() }
                    }
                }

                if (dataIsNotBlank) {
                    val qrBitmap = QrGenerator.generate(config)?.scaledForWidget()
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
