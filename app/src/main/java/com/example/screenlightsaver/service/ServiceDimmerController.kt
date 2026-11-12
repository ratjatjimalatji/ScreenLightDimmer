package com.example.screenlightsaver.service

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.screenlightsaver.domain.DimmerCommand
import com.example.screenlightsaver.domain.DimmerController
import com.example.screenlightsaver.domain.DimmerStateReducer
import com.example.screenlightsaver.domain.DimmerStateRepository
import com.example.screenlightsaver.domain.OverlayPermissionChecker

/**
 * Routes commands to [DimmerService] only when the overlay is (or will be) visible; otherwise
 * the new state is saved directly so no foreground service is started needlessly.
 */
class ServiceDimmerController(
    private val context: Context,
    private val repository: DimmerStateRepository,
    private val permissionChecker: OverlayPermissionChecker,
) : DimmerController {

    override fun dispatch(command: DimmerCommand) {
        val current = repository.state.value
        val next = DimmerStateReducer.reduce(current, command, permissionChecker.canDrawOverlays())
        if (current.isActive || next.isActive) {
            ContextCompat.startForegroundService(
                context,
                DimmerCommandIntents.toIntent(context, DimmerService::class.java, command),
            )
        } else {
            repository.save(next)
        }
    }
}
