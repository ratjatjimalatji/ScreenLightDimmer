package com.example.screenlightsaver.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.screenlightsaver.MainActivity
import com.example.screenlightsaver.R
import com.example.screenlightsaver.domain.DimmerCommand
import com.example.screenlightsaver.domain.DimmerState
import com.example.screenlightsaver.service.DimmerCommandIntents
import com.example.screenlightsaver.service.DimmerService

/** Turns a [DimmerState] into widget [RemoteViews]. Knows nothing about when it is called. */
class DimmerWidgetRenderer(private val context: Context) {

    fun render(manager: AppWidgetManager, widgetIds: IntArray, state: DimmerState, canDrawOverlays: Boolean) {
        if (widgetIds.isEmpty()) return
        manager.updateAppWidget(widgetIds, build(state, canDrawOverlays))
    }

    private fun build(state: DimmerState, canDrawOverlays: Boolean) =
        RemoteViews(context.packageName, R.layout.widget_dimmer).apply {
            val status = if (state.isActive) R.string.widget_status_on else R.string.widget_status_off
            setTextViewText(R.id.widget_status, context.getString(status, state.percent))
            setTextViewText(R.id.widget_percent, "${state.percent}%")

            setInt(
                R.id.widget_toggle, "setBackgroundResource",
                if (state.isActive) R.drawable.widget_toggle_on_bg else R.drawable.widget_toggle_off_bg,
            )
            setInt(
                R.id.widget_toggle, "setColorFilter",
                ContextCompat.getColor(
                    context, if (state.isActive) R.color.widget_on_primary else R.color.widget_on_button,
                ),
            )
            setContentDescription(
                R.id.widget_toggle,
                context.getString(if (state.isActive) R.string.turn_off else R.string.turn_on),
            )

            setOnClickPendingIntent(R.id.widget_header, openApp())
            setOnClickPendingIntent(
                R.id.widget_toggle,
                if (canDrawOverlays) servicePendingIntent(DimmerCommand.Toggle) else openApp(),
            )
            setOnClickPendingIntent(R.id.widget_decrease, levelPendingIntent(DimmerCommand.Decrease, state.isActive))
            setOnClickPendingIntent(R.id.widget_increase, levelPendingIntent(DimmerCommand.Increase, state.isActive))
        }

    /** While the overlay is shown the service is running, so talk to it directly. */
    private fun levelPendingIntent(command: DimmerCommand, isActive: Boolean): PendingIntent =
        if (isActive) {
            servicePendingIntent(command)
        } else {
            PendingIntent.getBroadcast(
                context, requestCode(command),
                DimmerCommandIntents.toIntent(context, DimmerWidgetProvider::class.java, command),
                FLAGS,
            )
        }

    /** Widget taps are allowed to start a foreground service from the background. */
    private fun servicePendingIntent(command: DimmerCommand): PendingIntent {
        val intent = DimmerCommandIntents.toIntent(context, DimmerService::class.java, command)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, requestCode(command), intent, FLAGS)
        } else {
            PendingIntent.getService(context, requestCode(command), intent, FLAGS)
        }
    }

    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        context, REQUEST_OPEN_APP,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        FLAGS,
    )

    private fun requestCode(command: DimmerCommand) = DimmerCommandIntents.actionFor(command).hashCode()

    private companion object {
        const val REQUEST_OPEN_APP = 100
        const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
