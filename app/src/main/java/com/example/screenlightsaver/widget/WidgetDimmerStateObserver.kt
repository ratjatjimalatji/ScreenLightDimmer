package com.example.screenlightsaver.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.example.screenlightsaver.domain.DimmerState
import com.example.screenlightsaver.domain.DimmerStateObserver
import com.example.screenlightsaver.domain.OverlayPermissionChecker

/** Keeps every placed widget in sync with the dimmer state. */
class WidgetDimmerStateObserver(
    private val context: Context,
    private val permissionChecker: OverlayPermissionChecker,
) : DimmerStateObserver {

    private val renderer = DimmerWidgetRenderer(context)

    override fun onDimmerStateChanged(state: DimmerState) {
        val manager = AppWidgetManager.getInstance(context) ?: return
        val ids = manager.getAppWidgetIds(ComponentName(context, DimmerWidgetProvider::class.java))
        renderer.render(manager, ids, state, permissionChecker.canDrawOverlays())
    }
}
