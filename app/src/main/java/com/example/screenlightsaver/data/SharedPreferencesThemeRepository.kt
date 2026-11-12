package com.example.screenlightsaver.data

import android.content.Context
import androidx.core.content.edit
import com.example.screenlightsaver.domain.ThemePreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesThemeRepository(context: Context) : ThemePreferenceRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _override = MutableStateFlow(
        if (prefs.contains(KEY_DARK)) prefs.getBoolean(KEY_DARK, false) else null
    )
    override val darkModeOverride: StateFlow<Boolean?> = _override.asStateFlow()

    override fun setDarkMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DARK, enabled) }
        _override.value = enabled
    }

    private companion object {
        const val PREFS_NAME = "theme_preferences"
        const val KEY_DARK = "dark_mode"
    }
}
