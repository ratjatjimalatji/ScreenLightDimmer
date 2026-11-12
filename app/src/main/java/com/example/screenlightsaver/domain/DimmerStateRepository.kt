package com.example.screenlightsaver.domain

import kotlinx.coroutines.flow.StateFlow

/** Single source of truth for [DimmerState]. */
interface DimmerStateRepository {
    val state: StateFlow<DimmerState>
    fun save(state: DimmerState)
}
