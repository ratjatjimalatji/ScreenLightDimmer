package com.example.screenlightsaver.service

import android.content.Context
import android.content.Intent
import com.example.screenlightsaver.domain.DimmerCommand

/** Translates [DimmerCommand]s to and from [Intent]s so they can cross component boundaries. */
object DimmerCommandIntents {
    private const val PREFIX = "com.example.screenlightsaver.action."
    const val ACTION_TURN_ON = PREFIX + "TURN_ON"
    const val ACTION_TURN_OFF = PREFIX + "TURN_OFF"
    const val ACTION_TOGGLE = PREFIX + "TOGGLE"
    const val ACTION_INCREASE = PREFIX + "INCREASE"
    const val ACTION_DECREASE = PREFIX + "DECREASE"
    const val ACTION_SET_LEVEL = PREFIX + "SET_LEVEL"
    const val EXTRA_LEVEL = "extra_level"

    fun actionFor(command: DimmerCommand): String = when (command) {
        DimmerCommand.TurnOn -> ACTION_TURN_ON
        DimmerCommand.TurnOff -> ACTION_TURN_OFF
        DimmerCommand.Toggle -> ACTION_TOGGLE
        DimmerCommand.Increase -> ACTION_INCREASE
        DimmerCommand.Decrease -> ACTION_DECREASE
        is DimmerCommand.SetLevel -> ACTION_SET_LEVEL
    }

    fun toIntent(context: Context, target: Class<*>, command: DimmerCommand): Intent =
        Intent(context, target).apply {
            action = actionFor(command)
            if (command is DimmerCommand.SetLevel) putExtra(EXTRA_LEVEL, command.level)
        }

    fun fromIntent(intent: Intent): DimmerCommand? = when (intent.action) {
        ACTION_TURN_ON -> DimmerCommand.TurnOn
        ACTION_TURN_OFF -> DimmerCommand.TurnOff
        ACTION_TOGGLE -> DimmerCommand.Toggle
        ACTION_INCREASE -> DimmerCommand.Increase
        ACTION_DECREASE -> DimmerCommand.Decrease
        ACTION_SET_LEVEL -> intent.getFloatExtra(EXTRA_LEVEL, Float.NaN)
            .takeUnless { it.isNaN() }
            ?.let { DimmerCommand.SetLevel(it) }
        else -> null
    }
}
