package com.example.screenlightsaver.domain

import kotlin.math.roundToInt

/**
 * Immutable snapshot of the dimmer.
 *
 * @property isActive whether the overlay is currently shown.
 * @property level user-facing darkness level in [MIN_LEVEL]..[MAX_LEVEL].
 */
data class DimmerState(
    val isActive: Boolean = false,
    val level: Float = DEFAULT_LEVEL,
) {
    /** Alpha applied to the black overlay window. */
    val overlayAlpha: Float get() = level * MAX_OVERLAY_ALPHA

    /** Level as a whole-number percentage for display. */
    val percent: Int get() = (level * 100).roundToInt()

    companion object {
        const val MIN_LEVEL = 0.05f
        const val MAX_LEVEL = 1f
        const val DEFAULT_LEVEL = 0.4f
        const val STEP = 0.1f

        /**
         * Android 12+ blocks touches passing through untrusted overlays more opaque than 0.8
         * (InputManager#getMaximumObscuringOpacityForTouch), so never exceed it.
         */
        const val MAX_OVERLAY_ALPHA = 0.8f

        fun clampLevel(level: Float): Float =
            ((level.coerceIn(MIN_LEVEL, MAX_LEVEL) * 100).roundToInt() / 100f)
    }
}
