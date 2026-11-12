package com.example.screenlightsaver.data

import android.content.Context
import androidx.core.content.edit
import com.example.screenlightsaver.domain.DimmerState
import com.example.screenlightsaver.domain.DimmerStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the darkness level. `isActive` is intentionally kept in memory only: after a process
 * restart there is no overlay, so the dimmer must start inactive.
 */
class SharedPreferencesDimmerStateRepository(context: Context) : DimmerStateRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        DimmerState(isActive = false, level = prefs.getFloat(KEY_LEVEL, DimmerState.DEFAULT_LEVEL))
    )
    override val state: StateFlow<DimmerState> = _state.asStateFlow()

    override fun save(state: DimmerState) {
        if (state.level != _state.value.level) prefs.edit { putFloat(KEY_LEVEL, state.level) }
        _state.value = state
    }

    private companion object {
        const val PREFS_NAME = "dimmer_state"
        const val KEY_LEVEL = "level"
    }
}
