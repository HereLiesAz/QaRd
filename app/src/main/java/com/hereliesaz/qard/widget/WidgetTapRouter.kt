package com.hereliesaz.qard.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.hereliesaz.qard.ui.ConfigActivity
import com.hereliesaz.qard.ui.DetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Routes taps on the home-screen widget into a single-tap or double-tap action.
 *
 * Home-screen widgets are [android.widget.RemoteViews]; they only receive click
 * callbacks (no raw touch stream), so a real [android.view.GestureDetector] is
 * impossible. Instead we count taps within a short window: the first tap arms a
 * delayed "single tap" job; a second tap inside [DOUBLE_TAP_WINDOW_MS] cancels
 * that job and fires the "double tap" action. This mirrors the multi-tap widget
 * approach used in the QuicLoc app.
 *
 *  - single tap -> open [DetailActivity] (see the info the code contains)
 *  - double tap -> open [ConfigActivity] (the construction screen) for the config
 */
object WidgetTapRouter {

    private const val DOUBLE_TAP_WINDOW_MS = 280L

    private val scope = CoroutineScope(SupervisorJob())

    // Pending single-tap jobs keyed by appWidgetId.
    private val pendingSingleTaps = mutableMapOf<Int, Job>()

    fun onTap(context: Context, appWidgetId: Int) {
        // Use the application context: this singleton scope outlives the
        // short-lived receiver/widget context that delivered the tap.
        val appContext = context.applicationContext
        synchronized(pendingSingleTaps) {
            val pending = pendingSingleTaps[appWidgetId]
            if (pending != null && pending.isActive) {
                // Second tap inside the window -> double tap.
                pending.cancel()
                pendingSingleTaps.remove(appWidgetId)
                launchConstruction(appContext, appWidgetId)
                return
            }

            // First tap -> arm a delayed single-tap action.
            val job = scope.launch {
                delay(DOUBLE_TAP_WINDOW_MS)
                if (isActive) {
                    launchDetail(appContext, appWidgetId)
                    synchronized(pendingSingleTaps) { pendingSingleTaps.remove(appWidgetId) }
                }
            }
            pendingSingleTaps[appWidgetId] = job
        }
    }

    private fun launchDetail(context: Context, appWidgetId: Int) {
        val intent = DetailActivity.widgetIntent(context, appWidgetId).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun launchConstruction(context: Context, appWidgetId: Int) {
        val intent = Intent(context, ConfigActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(ConfigActivity.EXTRA_MODE, ConfigActivity.MODE_WIDGET_CONFIG)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

/** Glance action invoked on every widget tap; delegates to [WidgetTapRouter]. */
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
            WidgetTapRouter.onTap(context, appWidgetId)
        }
    }
}
