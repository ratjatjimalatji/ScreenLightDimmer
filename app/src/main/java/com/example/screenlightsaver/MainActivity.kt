package com.example.screenlightsaver

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.screenlightsaver.ui.DimmerScreen
import com.example.screenlightsaver.ui.DimmerViewModel
import com.example.screenlightsaver.ui.OnboardingScreen
import com.example.screenlightsaver.ui.OnboardingViewModel
import com.example.screenlightsaver.ui.ThemeToggleButton
import com.example.screenlightsaver.ui.ThemeViewModel
import com.example.screenlightsaver.ui.theme.ScreenLightTheme

class MainActivity : ComponentActivity() {

    private val container get() = (application as ScreenLightApp).container
    private val dimmerViewModel: DimmerViewModel by viewModels { DimmerViewModel.factory(container) }
    private val themeViewModel: ThemeViewModel by viewModels { ThemeViewModel.factory(container) }
    private val onboardingViewModel: OnboardingViewModel by viewModels { OnboardingViewModel.factory(container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val override by themeViewModel.darkModeOverride.collectAsStateWithLifecycle()
            val isDark = override ?: isSystemInDarkTheme()
            LaunchedEffect(isDark) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            ScreenLightTheme(darkTheme = isDark) {
                val onboardingDone by onboardingViewModel.isCompleted.collectAsStateWithLifecycle()
                AnimatedContent(
                    targetState = onboardingDone,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboarding",
                ) { done ->
                    if (!done) {
                        OnboardingScreen(
                            onGrantPermission = ::requestOverlayPermission,
                            onSkip = onboardingViewModel::onSkip,
                        )
                    } else {
                        val state by dimmerViewModel.uiState.collectAsStateWithLifecycle()
                        DimmerScreen(
                            state = state,
                            onRequestPermission = ::requestOverlayPermission,
                            onToggleDimmer = dimmerViewModel::onToggleDimmer,
                            onLevelChange = dimmerViewModel::onLevelChange,
                            topBarActions = {
                                ThemeToggleButton(
                                    isDark = isDark,
                                    onToggle = { themeViewModel.onToggleLightMode(isDark) },
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dimmerViewModel.refreshPermission()
        onboardingViewModel.refreshPermission()
        container.notifyObservers()
    }

    private fun requestOverlayPermission() {
        startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
        )
    }
}
