package com.example.screenlightsaver

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class DimmerService : Service() {

    private var windowManager: WindowManager? = null
    private var dimView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var isViewAdded = false

    companion object {
        const val ACTION_START = "com.example.screenlightsaver.action.START"
        const val ACTION_STOP = "com.example.screenlightsaver.action.STOP"
        const val ACTION_UPDATE_ALPHA = "com.example.screenlightsaver.action.UPDATE_ALPHA"
        const val EXTRA_ALPHA = "extra_alpha"

        private const val CHANNEL_ID = "dimmer_service_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "DimmerService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DimmerService onCreate")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        val alpha = intent?.getFloatExtra(EXTRA_ALPHA, 0.4f) ?: 0.4f

        when (action) {
            ACTION_STOP -> {
                stopDimmer()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE_ALPHA -> {
                startForeground(NOTIFICATION_ID, createNotification(alpha))
                showOrUpdateOverlay(alpha)
            }
        }
        return START_STICKY
    }

    private fun showOrUpdateOverlay(alpha: Float) {
        val wm = windowManager ?: return

        if (dimView == null) {
            dimView = View(this).apply {
                setBackgroundColor(Color.BLACK)
            }

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT
            ).apply {
                this.alpha = alpha
            }
        } else {
            params?.alpha = alpha
        }

        try {
            if (!isViewAdded && dimView != null && params != null) {
                wm.addView(dimView, params)
                isViewAdded = true
                Log.d(TAG, "Overlay view added with alpha: $alpha")
            } else if (isViewAdded && dimView != null && params != null) {
                wm.updateViewLayout(dimView, params)
                Log.d(TAG, "Overlay view updated with alpha: $alpha")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing/updating overlay: ${e.message}", e)
        }
    }

    private fun stopDimmer() {
        if (isViewAdded && dimView != null && windowManager != null) {
            try {
                windowManager?.removeView(dimView)
                Log.d(TAG, "Overlay view removed")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view: ${e.message}", e)
            }
            isViewAdded = false
            dimView = null
        }
    }

    override fun onDestroy() {
        stopDimmer()
        super.onDestroy()
        Log.d(TAG, "DimmerService onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Dimmer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the screen overlay active in background"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(alpha: Float): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val darknessPercent = (alpha * 100).toInt()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Dimmer Active")
            .setContentText("Overlay darkness set to $darknessPercent%")
            .setSmallIcon(R.drawable.ic_menu_view)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
