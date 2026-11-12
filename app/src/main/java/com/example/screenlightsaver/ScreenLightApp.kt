package com.example.screenlightsaver

import android.app.Application
import com.example.screenlightsaver.widget.WidgetDimmerStateObserver

class ScreenLightApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this).apply {
            addObserver(WidgetDimmerStateObserver(this@ScreenLightApp, overlayPermissionChecker))
        }
    }
}
