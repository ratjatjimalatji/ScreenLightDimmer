package com.example.screenlightsaver

import android.app.Application

class ScreenLightApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
