package com.example.screenlightsaver

import android.content.Context
import com.example.screenlightsaver.data.AndroidOverlayPermissionChecker
import com.example.screenlightsaver.data.SharedPreferencesDimmerStateRepository
import com.example.screenlightsaver.data.SharedPreferencesThemeRepository
import com.example.screenlightsaver.domain.DimmerController
import com.example.screenlightsaver.domain.DimmerStateObserver
import com.example.screenlightsaver.domain.DimmerStateRepository
import com.example.screenlightsaver.domain.OverlayPermissionChecker
import com.example.screenlightsaver.domain.ThemePreferenceRepository
import com.example.screenlightsaver.service.ServiceDimmerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Composition root: the only place that knows concrete implementations. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val overlayPermissionChecker: OverlayPermissionChecker = AndroidOverlayPermissionChecker(appContext)
    val dimmerStateRepository: DimmerStateRepository = SharedPreferencesDimmerStateRepository(appContext)
    val themePreferenceRepository: ThemePreferenceRepository = SharedPreferencesThemeRepository(appContext)
    val dimmerController: DimmerController =
        ServiceDimmerController(appContext, dimmerStateRepository, overlayPermissionChecker)

    private val observers = mutableListOf<DimmerStateObserver>()

    init {
        dimmerStateRepository.state
            .onEach { state -> observers.forEach { it.onDimmerStateChanged(state) } }
            .launchIn(scope)
    }

    /** Register an extra state consumer (e.g. widgets) without changing this class. */
    fun addObserver(observer: DimmerStateObserver) {
        observers += observer
        observer.onDimmerStateChanged(dimmerStateRepository.state.value)
    }

    /** Re-push the current state, e.g. after the overlay permission changed. */
    fun notifyObservers() {
        val state = dimmerStateRepository.state.value
        observers.forEach { it.onDimmerStateChanged(state) }
    }
}
