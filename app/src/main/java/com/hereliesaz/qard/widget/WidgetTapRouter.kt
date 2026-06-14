package com.hereliesaz.qard.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.hereliesaz.qard.ui.WidgetMenuActivity

/**
 * Glance action invoked on every widget tap. Opens [WidgetMenuActivity], a small
 * menu letting the user act on the code (like scanning it), view its details,
 * edit it, or open the QaRd app.
 */
class WidgetTapAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val appWidgetId =
            parameters[ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID)]
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            context.startActivity(WidgetMenuActivity.intent(context, appWidgetId))
        }
    }
}
