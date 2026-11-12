package com.example.screenlightsaver.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DimmerStateReducerTest {

    private fun reduce(state: DimmerState, command: DimmerCommand, permission: Boolean = true) =
        DimmerStateReducer.reduce(state, command, permission)

    @Test
    fun toggle_flipsActiveState() {
        assertTrue(reduce(DimmerState(isActive = false), DimmerCommand.Toggle).isActive)
        assertFalse(reduce(DimmerState(isActive = true), DimmerCommand.Toggle).isActive)
    }

    @Test
    fun turnOn_withoutOverlayPermission_staysInactive() {
        assertFalse(reduce(DimmerState(), DimmerCommand.TurnOn, permission = false).isActive)
    }

    @Test
    fun increaseAndDecrease_stepByTenPercentWithoutFloatDrift() {
        val up = reduce(DimmerState(level = 0.4f), DimmerCommand.Increase)
        assertEquals(0.5f, up.level, 0f)
        val down = reduce(up, DimmerCommand.Decrease)
        assertEquals(0.4f, down.level, 0f)
    }

    @Test
    fun levels_areClampedToValidRange() {
        assertEquals(DimmerState.MAX_LEVEL, reduce(DimmerState(level = 0.95f), DimmerCommand.Increase).level, 0f)
        assertEquals(DimmerState.MIN_LEVEL, reduce(DimmerState(level = 0.1f), DimmerCommand.Decrease).level, 0f)
        assertEquals(DimmerState.MIN_LEVEL, reduce(DimmerState(), DimmerCommand.SetLevel(-3f)).level, 0f)
    }

    @Test
    fun levelChange_keepsActiveFlag() {
        val state = reduce(DimmerState(isActive = true, level = 0.2f), DimmerCommand.SetLevel(0.7f))
        assertTrue(state.isActive)
        assertEquals(0.7f, state.level, 0f)
    }

    @Test
    fun overlayAlpha_neverExceedsTouchSafeMaximum() {
        assertEquals(DimmerState.MAX_OVERLAY_ALPHA, DimmerState(level = 1f).overlayAlpha, 0f)
        assertTrue(DimmerState.MAX_OVERLAY_ALPHA <= 0.8f)
    }
}
