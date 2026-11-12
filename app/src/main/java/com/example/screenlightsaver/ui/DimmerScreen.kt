package com.example.screenlightsaver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.screenlightsaver.R
import com.example.screenlightsaver.domain.DimmerState
import com.example.screenlightsaver.ui.theme.ScreenLightTheme

private val Presets = listOf(0.2f, 0.4f, 0.6f, 0.8f)

/** Stateless screen: everything it shows comes in, everything the user does goes out. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DimmerScreen(
    state: DimmerUiState,
    onRequestPermission: () -> Unit,
    onToggleDimmer: () -> Unit,
    onLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    topBarActions: @Composable () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                subtitle = { Text(stringResource(R.string.app_subtitle)) },
                actions = { topBarActions() },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AnimatedVisibility(visible = !state.hasPermission) {
                PermissionCard(onRequestPermission)
            }
            LevelBadge(state)
            PowerToggle(state, onToggleDimmer)
            LevelCard(state, onLevelChange)
            Text(
                text = stringResource(R.string.widget_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.permission_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.permission_body), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRequestPermission, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LevelBadge(state: DimmerUiState) {
    val motion = MaterialTheme.motionScheme
    val size by animateDpAsState(
        targetValue = if (state.isActive) 208.dp else 176.dp,
        animationSpec = motion.defaultSpatialSpec(),
        label = "badgeSize",
    )
    val container by animateColorAsState(
        targetValue = if (state.isActive) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = motion.defaultEffectsSpec(),
        label = "badgeColor",
    )
    val content = if (state.isActive) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = (if (state.isActive) MaterialShapes.Cookie9Sided else MaterialShapes.Circle).toShape()

    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .size(size)
            .clip(shape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${state.percent}%", style = MaterialTheme.typography.displayLarge, color = content)
            Text(
                stringResource(if (state.isActive) R.string.status_active else R.string.status_inactive),
                style = MaterialTheme.typography.labelLarge,
                color = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PowerToggle(state: DimmerUiState, onToggle: () -> Unit) {
    ToggleButton(
        checked = state.isActive,
        onCheckedChange = { onToggle() },
        enabled = state.hasPermission,
        contentPadding = PaddingValues(horizontal = 32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
    ) {
        Icon(painterResource(R.drawable.ic_power), contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
        Text(
            stringResource(if (state.isActive) R.string.turn_off else R.string.turn_on),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LevelCard(state: DimmerUiState, onLevelChange: (Float) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.darkness_level), style = MaterialTheme.typography.titleMedium)
            val sliderLabel = stringResource(R.string.darkness_level)
            Slider(
                value = state.level,
                onValueChange = onLevelChange,
                valueRange = DimmerState.MIN_LEVEL..DimmerState.MAX_LEVEL,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = sliderLabel },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                Presets.forEachIndexed { index, preset ->
                    val percent = (preset * 100).toInt()
                    ToggleButton(
                        checked = state.percent == percent,
                        onCheckedChange = { onLevelChange(preset) },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            Presets.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { role = Role.RadioButton },
                    ) {
                        Text("$percent%")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DimmerScreenPreview() {
    ScreenLightTheme(dynamicColor = false) {
        DimmerScreen(
            state = DimmerUiState(hasPermission = true, isActive = true, level = 0.6f),
            onRequestPermission = {},
            onToggleDimmer = {},
            onLevelChange = {},
        )
    }
}
