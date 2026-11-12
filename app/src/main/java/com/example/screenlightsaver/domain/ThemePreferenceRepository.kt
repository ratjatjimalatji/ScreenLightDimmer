package com.example.screenlightsaver.domain

import kotlinx.coroutines.flow.StateFlow

/** Stores the user's light/dark choice. `null` means "follow the system". */
interface ThemePreferenceRepository {
    val darkModeOverride: StateFlow<Boolean?>
    fun setDarkMode(enabled: Boolean)
}
