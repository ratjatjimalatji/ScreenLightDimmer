package com.example.screenlightsaver.domain

/** Pure state transition logic, shared by the app, the service and the widget. */
object DimmerStateReducer {

    fun reduce(state: DimmerState, command: DimmerCommand, canShowOverlay: Boolean): DimmerState {
        val next = when (command) {
            DimmerCommand.TurnOn -> state.copy(isActive = true)
            DimmerCommand.TurnOff -> state.copy(isActive = false)
            DimmerCommand.Toggle -> state.copy(isActive = !state.isActive)
            DimmerCommand.Increase -> state.copy(level = DimmerState.clampLevel(state.level + DimmerState.STEP))
            DimmerCommand.Decrease -> state.copy(level = DimmerState.clampLevel(state.level - DimmerState.STEP))
            is DimmerCommand.SetLevel -> state.copy(level = DimmerState.clampLevel(command.level))
        }
        return if (next.isActive && !canShowOverlay) next.copy(isActive = false) else next
    }
}
