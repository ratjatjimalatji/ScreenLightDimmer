package com.example.screenlightsaver.ui

import com.example.screenlightsaver.domain.DimmerCommand
import com.example.screenlightsaver.domain.DimmerController
import com.example.screenlightsaver.domain.DimmerState
import com.example.screenlightsaver.domain.DimmerStateRepository
import com.example.screenlightsaver.domain.OverlayPermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DimmerViewModelTest {

    private class FakeRepository(initial: DimmerState = DimmerState()) : DimmerStateRepository {
        override val state = MutableStateFlow(initial)
        override fun save(state: DimmerState) { this.state.value = state }
    }

    private val repository = FakeRepository()
    private val dispatched = mutableListOf<DimmerCommand>()
    private val controller = DimmerController { dispatched += it }
    private var permission = false
    private val permissionChecker = OverlayPermissionChecker { permission }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private fun collect(scope: kotlinx.coroutines.CoroutineScope, flow: StateFlow<*>) =
        scope.launch(UnconfinedTestDispatcher()) { flow.collect {} }

    @Test
    fun uiState_reflectsRepositoryAndPermission() = runTest {
        val vm = DimmerViewModel(repository, controller, permissionChecker)
        collect(backgroundScope, vm.uiState)
        assertFalse(vm.uiState.value.hasPermission)

        permission = true
        vm.refreshPermission()
        repository.save(DimmerState(isActive = true, level = 0.6f))

        assertEquals(DimmerUiState(hasPermission = true, isActive = true, level = 0.6f), vm.uiState.value)
        assertEquals(60, vm.uiState.value.percent)
    }

    @Test
    fun userActions_areDispatchedToController() {
        val vm = DimmerViewModel(repository, controller, permissionChecker)
        vm.onToggleDimmer()
        vm.onLevelChange(0.3f)
        assertEquals(listOf(DimmerCommand.Toggle, DimmerCommand.SetLevel(0.3f)), dispatched)
        assertTrue(repository.state.value == DimmerState())
    }
}
