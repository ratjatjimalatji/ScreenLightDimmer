package com.example.screenlightsaver.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager

/** Owns the full-screen black overlay window and nothing else. */
class DimmerOverlay(private val context: Context) {

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var view: View? = null
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    )

    fun show(alpha: Float) {
        params.alpha = alpha
        try {
            val current = view
            if (current == null) {
                view = View(context).apply { setBackgroundColor(Color.BLACK) }
                    .also { windowManager.addView(it, params) }
            } else {
                windowManager.updateViewLayout(current, params)
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "Unable to show overlay", e)
            view = null
        }
    }

    fun hide() {
        val current = view ?: return
        try {
            windowManager.removeView(current)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Unable to remove overlay", e)
        }
        view = null
    }

    private companion object {
        const val TAG = "DimmerOverlay"
    }
}
