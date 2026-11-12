package com.example.screenlightsaver.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.screenlightsaver.AppContainer
import com.example.screenlightsaver.domain.DimmerCommand
import com.example.screenlightsaver.domain.DimmerController
import com.example.screenlightsaver.domain.DimmerState
import com.example.screenlightsaver.domain.DimmerStateRepository
import com.example.screenlightsaver.domain.OverlayPermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

data class DimmerUiState(
    val hasPermission: Boolean = false,
    val isActive: Boolean = false,
    val level: Float = DimmerState.DEFAULT_LEVEL,
) {
    val percent: Int get() = (level * 100).roundToInt()
}

class DimmerViewModel(
    repository: DimmerStateRepository,
    private val controller: DimmerController,
    private val permissionChecker: OverlayPermissionChecker,
) : ViewModel() {

    private val hasPermission = MutableStateFlow(permissionChecker.canDrawOverlays())

    val uiState: StateFlow<DimmerUiState> =
        combine(repository.state, hasPermission) { state, permission ->
            DimmerUiState(hasPermission = permission, isActive = state.isActive, level = state.level)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = repository.state.value.let {
                DimmerUiState(hasPermission.value, it.isActive, it.level)
            },
        )

    fun refreshPermission() {
        hasPermission.value = permissionChecker.canDrawOverlays()
    }

    fun onToggleDimmer() = controller.dispatch(DimmerCommand.Toggle)

    fun onLevelChange(level: Float) = controller.dispatch(DimmerCommand.SetLevel(level))

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DimmerViewModel(
                    container.dimmerStateRepository,
                    container.dimmerController,
                    container.overlayPermissionChecker,
                )
            }
        }
    }
}
