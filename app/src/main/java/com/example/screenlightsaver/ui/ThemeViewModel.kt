package com.example.screenlightsaver.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.screenlightsaver.AppContainer
import com.example.screenlightsaver.domain.ThemePreferenceRepository
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel(private val repository: ThemePreferenceRepository) : ViewModel() {

    val darkModeOverride: StateFlow<Boolean?> = repository.darkModeOverride

    /** Switches to the opposite of what is currently on screen. */
    fun onToggleLightMode(currentlyDark: Boolean) = repository.setDarkMode(!currentlyDark)

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ThemeViewModel(container.themePreferenceRepository) }
        }
    }
}
