package com.hereliesaz.qard.widget

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.glance.ExperimentalGlanceApi
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

    @OptIn(ExperimentalGlanceApi::class)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val appWidgetId = getAppWidgetId(context, id)
        Log.d("WidgetFlow", "provideGlance for widget ID: $appWidgetId")
        val config = dataStore.getConfig(appWidgetId).first()
        Log.d("WidgetFlow", "Config received in widget: $config")

        provideContent {
            val intent = Intent(context, ConfigActivity::class.java)
            val activityOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ActivityOptions.makeBasic().setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            } else {
                null
            }
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(createTransparentBitmap())) // Use transparent background
                    .padding(8.dp)
                    .clickable(actionStartActivity(intent, activityOptions = activityOptions?.toBundle())),
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
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(0)
        return bitmap
    }
}