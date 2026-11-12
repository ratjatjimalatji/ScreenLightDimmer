package com.example.screenlightsaver.domain

/** Entry point for changing the dimmer. UI and widgets depend on this abstraction, not on the service. */
fun interface DimmerController {
    fun dispatch(command: DimmerCommand)
}
