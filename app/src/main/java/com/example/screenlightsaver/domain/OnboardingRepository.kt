package com.example.screenlightsaver.domain

import kotlinx.coroutines.flow.StateFlow

/** Remembers whether the user has finished (or skipped) onboarding. */
interface OnboardingRepository {
    val isCompleted: StateFlow<Boolean>
    fun markCompleted()
}
