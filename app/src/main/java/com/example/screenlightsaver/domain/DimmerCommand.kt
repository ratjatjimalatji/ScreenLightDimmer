package com.example.screenlightsaver.domain

/** Every user intent that can change the dimmer, regardless of where it came from (app, widget). */
sealed interface DimmerCommand {
    data object TurnOn : DimmerCommand
    data object TurnOff : DimmerCommand
    data object Toggle : DimmerCommand
    data object Increase : DimmerCommand
    data object Decrease : DimmerCommand
    data class SetLevel(val level: Float) : DimmerCommand
}
