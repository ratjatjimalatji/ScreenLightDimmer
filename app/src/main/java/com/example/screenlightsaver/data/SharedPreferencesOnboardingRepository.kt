package com.example.screenlightsaver.data

import android.content.Context
import androidx.core.content.edit
import com.example.screenlightsaver.domain.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesOnboardingRepository(context: Context) : OnboardingRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _isCompleted = MutableStateFlow(prefs.getBoolean(KEY_COMPLETED, false))
    override val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    override fun markCompleted() {
        if (_isCompleted.value) return
        prefs.edit { putBoolean(KEY_COMPLETED, true) }
        _isCompleted.value = true
    }

    private companion object {
        const val PREFS_NAME = "onboarding"
        const val KEY_COMPLETED = "completed"
    }
}
