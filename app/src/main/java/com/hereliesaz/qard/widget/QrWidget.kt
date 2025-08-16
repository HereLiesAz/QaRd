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
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
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
import com.hereliesaz.qard.ui.ConfigActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class QrWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = QrDataStore(context)
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        Log.d("WidgetFlow", "provideGlance for widget ID: $appWidgetId (from GlanceId $id)")

        delay(200) // Small delay to ensure datastore consistency

        val config = dataStore.getConfig(appWidgetId).first()
        Log.d("WidgetFlow", "Config received in widget (for ID $appWidgetId): $config")

        provideContent {
            val action = actionRunCallback<ConfigActivityAction>(
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
                        is QrData.Contact -> it.name.isNotBlank()
                        is QrData.SocialMedia -> it.links.any { social -> social.url.isNotBlank() }
                    }
                }
                Log.d("WidgetFlow", "Widget dataIsNotBlank (for ID $appWidgetId): $dataIsNotBlank")

                if (dataIsNotBlank) {
                    val qrBitmap = QrGenerator.generate(config)
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
                    Text("Tap to configure widget (ID: $appWidgetId).")
                }
            }
        }
    }

    private fun createTransparentBitmap(): Bitmap {
        val bitmap = createBitmap(1, 1)
        bitmap.eraseColor(0) // Makes the bitmap transparent
        return bitmap
    }
}

class ConfigActivityAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val appWidgetId = parameters[ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID)]
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val intent = Intent(context, ConfigActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}