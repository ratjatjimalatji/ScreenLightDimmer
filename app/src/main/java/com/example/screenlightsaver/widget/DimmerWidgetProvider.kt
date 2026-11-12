package com.example.screenlightsaver.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.example.screenlightsaver.ScreenLightApp
import com.example.screenlightsaver.service.DimmerCommandIntents

/** Home screen widget: power toggle plus darker/lighter buttons. */
class DimmerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val container = (context.applicationContext as ScreenLightApp).container
        DimmerWidgetRenderer(context).render(
            appWidgetManager,
            appWidgetIds,
            container.dimmerStateRepository.state.value,
            container.overlayPermissionChecker.canDrawOverlays(),
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val command = DimmerCommandIntents.fromIntent(intent) ?: return
        (context.applicationContext as ScreenLightApp).container.dimmerController.dispatch(command)
    }
}
