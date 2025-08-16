// G:/My Drive/QaRd/app/src/main/java/com/hereliesaz/qard/widget/QrWidget.kt
package com.hereliesaz.qard.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.ui.ConfigActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class QrWidget : GlanceAppWidget() {

    @OptIn(ExperimentalGlanceApi::class)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val appWidgetId = getAppWidgetId(context, id)
        Log.d(
            "WidgetFlow",
            "provideGlance for widget ID: $appWidgetId (from GlanceId $id)"
        ) // MODIFIED LOG
        // if (appWidgetId == 0) { // Add a check for invalid appWidgetId if needed
        //     Log.e("WidgetFlow", "Invalid appWidgetId (0) for GlanceId $id. Cannot retrieve config.")
        //     // Potentially provide default content or an error message without trying to fetch config
        //     provideContent { Text("Error: Widget ID not found.") }
        //     return
        // }

        // Uncomment the line below for timing troubleshooting only
        delay(200)

        val config = dataStore.getConfig(appWidgetId).first()
        Log.d(
            "WidgetFlow",
            "Config received in widget (for ID $appWidgetId): $config"
        ) // MODIFIED LOG
        val clickAction: Action = actionStartActivity(ConfigActivity::class.java) {
            // This lambda block configures the Intent internally for Glance
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }.allowBackgroundActivityStarts()
        // *** FIX ENDS HERE ***

        provideContent {


            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(createTransparentBitmap()))
                    .padding(8.dp)
                    .clickable(clickAction), // <--- Use the new clickAction                contentAlignment = Alignment.Center
            ) {
                val dataIsNotBlank = config.data.any {
                    when (it) {
                        is QrData.Links -> it.links.any { link -> link.isNotBlank() }
                        is QrData.Contact -> it.name.isNotBlank()
                        is QrData.SocialMedia -> it.links.any { social -> social.url.isNotBlank() }
                    }
                }
                Log.d(
                    "WidgetFlow",
                    "Widget dataIsNotBlank (for ID $appWidgetId): $dataIsNotBlank"
                ) // MODIFIED LOG
                if (dataIsNotBlank) {
                    val qrBitmap = QrGenerator.generate(config)
                    if (qrBitmap != null) {
                        Image(
                            provider = ImageProvider(qrBitmap),
                            contentDescription = "User-defined QR Code",
                            modifier = GlanceModifier.fillMaxSize()
                        )
                    } else {
                        Text("Error generating QR Code. (Data present: ${config.data.isNotEmpty()})") // ADDED HINT
                    }
                } else {
                    Text("Tap to configure widget (ID: $appWidgetId).") // ADDED HINT
                }
            }
        }
    }

    private fun getAppWidgetId(context: Context, glanceId: GlanceId): Int {
        val id = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        Log.d(
            "WidgetFlow",
            "getAppWidgetId called: GlanceId $glanceId maps to AppWidgetId $id"
        ) // ADDED LOG
        return id
    }

    private fun createTransparentBitmap(): Bitmap {
        val bitmap = createBitmap(1, 1)
        bitmap.eraseColor(0)
        return bitmap
    }
}