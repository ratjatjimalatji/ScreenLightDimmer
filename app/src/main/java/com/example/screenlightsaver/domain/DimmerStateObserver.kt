package com.example.screenlightsaver.domain

/** Something outside the app UI that must reflect state changes (e.g. home screen widgets). */
fun interface DimmerStateObserver {
    fun onDimmerStateChanged(state: DimmerState)
}
