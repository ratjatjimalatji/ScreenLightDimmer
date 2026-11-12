package com.example.screenlightsaver.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.example.screenlightsaver.ScreenLightApp
import com.example.screenlightsaver.domain.DimmerState
import com.example.screenlightsaver.domain.DimmerStateReducer

/**
 * Foreground service that keeps the overlay alive. It applies incoming commands to the shared
 * state and renders the result; overlay drawing and notifications are delegated.
 */
class DimmerService : Service() {

    private lateinit var overlay: DimmerOverlay
    private lateinit var notifications: DimmerNotificationFactory
    private val container get() = (application as ScreenLightApp).container

    override fun onCreate() {
        super.onCreate()
        overlay = DimmerOverlay(this)
        notifications = DimmerNotificationFactory(this).also { it.ensureChannel() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val repository = container.dimmerStateRepository
        val current = repository.state.value
        // Callers use startForegroundService(), so we must always promote before anything else.
        promote(current)

        val command = intent?.let(DimmerCommandIntents::fromIntent)
        val next = command?.let {
            DimmerStateReducer.reduce(current, it, container.overlayPermissionChecker.canDrawOverlays())
        } ?: current
        repository.save(next)

        if (next.isActive) {
            overlay.show(next.overlayAlpha)
            promote(next)
        } else {
            overlay.hide()
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        overlay.hide()
        val repository = container.dimmerStateRepository
        repository.state.value.takeIf { it.isActive }?.let { repository.save(it.copy(isActive = false)) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun promote(state: DimmerState) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, DimmerNotificationFactory.NOTIFICATION_ID, notifications.create(state), type)
    }
}
