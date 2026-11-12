package com.example.screenlightsaver.ui

import com.example.screenlightsaver.domain.OnboardingRepository
import com.example.screenlightsaver.domain.OverlayPermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {

    private class FakeOnboardingRepository : OnboardingRepository {
        override val isCompleted = MutableStateFlow(false)
        override fun markCompleted() { isCompleted.value = true }
    }

    private val repository = FakeOnboardingRepository()
    private var permission = false
    private val checker = OverlayPermissionChecker { permission }

    @Test
    fun showsOnboarding_whenPermissionMissing() {
        val vm = OnboardingViewModel(repository, checker)
        assertFalse(vm.isCompleted.value)
    }

    @Test
    fun skipsOnboarding_whenPermissionAlreadyGranted() {
        permission = true
        val vm = OnboardingViewModel(repository, checker)
        assertTrue(vm.isCompleted.value)
    }

    @Test
    fun completes_afterReturningFromSettingsWithPermission() {
        val vm = OnboardingViewModel(repository, checker)
        permission = true
        vm.refreshPermission()
        assertTrue(vm.isCompleted.value)
    }

    @Test
    fun maybeLater_completesWithoutPermission() {
        val vm = OnboardingViewModel(repository, checker)
        vm.onSkip()
        assertTrue(vm.isCompleted.value)
    }
}
