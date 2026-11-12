package com.example.screenlightsaver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.screenlightsaver.MainActivity
import com.example.screenlightsaver.R
import com.example.screenlightsaver.domain.DimmerCommand
import com.example.screenlightsaver.domain.DimmerState

/** Builds the foreground-service notification. */
class DimmerNotificationFactory(private val context: Context) {

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = context.getString(R.string.notification_channel_description) }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun create(state: DimmerState): Notification {
        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val turnOff = PendingIntent.getService(
            context, 1,
            DimmerCommandIntents.toIntent(context, DimmerService::class.java, DimmerCommand.TurnOff),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.darkness_percent, state.percent))
            .setSmallIcon(R.drawable.ic_dimmer)
            .setContentIntent(openApp)
            .addAction(0, context.getString(R.string.turn_off), turnOff)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "dimmer_service_channel"
    }
}
