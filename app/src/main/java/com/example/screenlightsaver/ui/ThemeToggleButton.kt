package com.example.screenlightsaver.ui

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.screenlightsaver.R

/** Expressive shape-morphing toggle: checked = light mode. Shows the mode you will switch to. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeToggleButton(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalIconToggleButton(
        checked = !isDark,
        onCheckedChange = { onToggle() },
        shapes = IconButtonDefaults.toggleableShapes(),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
            contentDescription = stringResource(
                if (isDark) R.string.switch_to_light_mode else R.string.switch_to_dark_mode
            ),
        )
    }
}
