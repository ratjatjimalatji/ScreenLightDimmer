package com.example.screenlightsaver.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.screenlightsaver.AppContainer
import com.example.screenlightsaver.domain.OnboardingRepository
import com.example.screenlightsaver.domain.OverlayPermissionChecker
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Decides whether onboarding is shown. It is shown once, until the user either grants the
 * overlay permission or chooses "Maybe later".
 */
class OnboardingViewModel(
    private val repository: OnboardingRepository,
    private val permissionChecker: OverlayPermissionChecker,
) : ViewModel() {

    val isCompleted: StateFlow<Boolean> = repository.isCompleted

    init {
        refreshPermission()
    }

    /** Call when returning from system settings. */
    fun refreshPermission() {
        if (permissionChecker.canDrawOverlays()) repository.markCompleted()
    }

    fun onSkip() = repository.markCompleted()

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                OnboardingViewModel(container.onboardingRepository, container.overlayPermissionChecker)
            }
        }
    }
}
