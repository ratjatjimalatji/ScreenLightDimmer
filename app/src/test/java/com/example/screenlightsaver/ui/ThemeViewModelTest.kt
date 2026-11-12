package com.example.screenlightsaver.ui

import com.example.screenlightsaver.domain.ThemePreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThemeViewModelTest {

    private class FakeThemeRepository : ThemePreferenceRepository {
        override val darkModeOverride = MutableStateFlow<Boolean?>(null)
        override fun setDarkMode(enabled: Boolean) { darkModeOverride.value = enabled }
    }

    @Test
    fun followsSystemUntilUserToggles() {
        val vm = ThemeViewModel(FakeThemeRepository())
        assertNull(vm.darkModeOverride.value)
    }

    @Test
    fun toggle_switchesToOppositeOfWhatIsShown() {
        val vm = ThemeViewModel(FakeThemeRepository())
        vm.onToggleLightMode(currentlyDark = true)
        assertEquals(false, vm.darkModeOverride.value)
        vm.onToggleLightMode(currentlyDark = false)
        assertEquals(true, vm.darkModeOverride.value)
    }
}
