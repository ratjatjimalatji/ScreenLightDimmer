package com.example.screenlightsaver.data

import android.content.Context
import android.provider.Settings
import com.example.screenlightsaver.domain.OverlayPermissionChecker

class AndroidOverlayPermissionChecker(private val context: Context) : OverlayPermissionChecker {
    override fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)
}
