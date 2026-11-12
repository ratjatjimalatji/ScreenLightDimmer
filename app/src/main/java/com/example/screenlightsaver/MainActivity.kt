package com.example.screenlightsaver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var hasOverlayPermissionState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasPermission by hasOverlayPermissionState

                    DimmerScreen(
                        hasPermission = hasPermission,
                        onRequestPermission = { requestOverlayPermission() },
                        onServiceStateChanged = { isRunning, alpha ->
                            if (isRunning) {
                                startDimmerService(alpha)
                            } else {
                                stopDimmerService()
                            }
                        },
                        onAlphaChanged = { alpha ->
                            updateDimmerAlpha(alpha)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasOverlayPermissionState.value = Settings.canDrawOverlays(this)
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun startDimmerService(alpha: Float) {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        val intent = Intent(this, DimmerService::class.java).apply {
            action = DimmerService.ACTION_START
            putExtra(DimmerService.EXTRA_ALPHA, alpha)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun updateDimmerAlpha(alpha: Float) {
        if (!Settings.canDrawOverlays(this)) return
        val intent = Intent(this, DimmerService::class.java).apply {
            action = DimmerService.ACTION_UPDATE_ALPHA
            putExtra(DimmerService.EXTRA_ALPHA, alpha)
        }
        startService(intent)
    }

    private fun stopDimmerService() {
        val intent = Intent(this, DimmerService::class.java).apply {
            action = DimmerService.ACTION_STOP
        }
        startService(intent)
    }
}

@Composable
fun DimmerScreen(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onServiceStateChanged: (Boolean, Float) -> Unit,
    onAlphaChanged: (Float) -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    // Slider value from 0f to 1f, representing darkness percentage
    var sliderValue by remember { mutableFloatStateOf(0.4f) }

    // Map slider (0.0..1.0) to effective overlay alpha (0.0..0.85 max for safety)
    val currentAlpha = sliderValue * 0.85f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = "Extra Screen Dimmer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lower your screen brightness beyond the default system minimum for comfortable night viewing.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!hasPermission) {
                    Text(
                        text = "Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To show the dimming overlay above other apps, please grant overlay permission.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Grant Permission")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isRunning) "Dimmer Active" else "Dimmer Inactive",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Overlay Darkness: ${(sliderValue * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isRunning,
                            onCheckedChange = { checked ->
                                isRunning = checked
                                onServiceStateChanged(isRunning, currentAlpha)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Darkness Level",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Slider(
                        value = sliderValue,
                        onValueChange = { newValue ->
                            sliderValue = newValue
                            if (isRunning) {
                                onAlphaChanged(newValue * 0.85f)
                            }
                        },
                        valueRange = 0.05f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val presets = listOf(0.2f, 0.4f, 0.6f, 0.8f)
                        presets.forEach { preset ->
                            OutlinedButton(
                                onClick = {
                                    sliderValue = preset
                                    if (isRunning) {
                                        onAlphaChanged(preset * 0.85f)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${(preset * 100).toInt()}%",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (!hasPermission) {
                    onRequestPermission()
                } else {
                    isRunning = !isRunning
                    onServiceStateChanged(isRunning, currentAlpha)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = when {
                    !hasPermission -> "Grant Permission"
                    isRunning -> "Stop Extra Dimmer"
                    else -> "Start Extra Dimmer"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
