package com.example.screenlightsaver.domain

fun interface OverlayPermissionChecker {
    fun canDrawOverlays(): Boolean
}
