package com.example.screenlightsaver

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.screenlightsaver.ui.DimmerScreen
import com.example.screenlightsaver.ui.DimmerViewModel
import com.example.screenlightsaver.ui.theme.ScreenLightTheme

class MainActivity : ComponentActivity() {

    private val container get() = (application as ScreenLightApp).container
    private val dimmerViewModel: DimmerViewModel by viewModels { DimmerViewModel.factory(container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenLightTheme {
                val state by dimmerViewModel.uiState.collectAsStateWithLifecycle()
                DimmerScreen(
                    state = state,
                    onRequestPermission = ::requestOverlayPermission,
                    onToggleDimmer = dimmerViewModel::onToggleDimmer,
                    onLevelChange = dimmerViewModel::onLevelChange,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dimmerViewModel.refreshPermission()
    }

    private fun requestOverlayPermission() {
        startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
        )
    }
}
