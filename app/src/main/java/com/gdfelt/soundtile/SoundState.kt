package com.gdfelt.soundtile

import android.app.NotificationManager
import android.media.AudioManager

/**
 * The states the tile cycles through. [SILENT] is reachable only by changing the ringer elsewhere,
 * so it is displayed but skipped by [next].
 */
enum class SoundState {
    NORMAL,
    VIBRATE,
    SILENT,
    PRIORITY,
    TOTAL_SILENCE;

    fun next(): SoundState = when (this) {
        NORMAL -> VIBRATE
        VIBRATE -> PRIORITY
        PRIORITY -> TOTAL_SILENCE
        TOTAL_SILENCE -> NORMAL
        SILENT -> NORMAL
    }

    fun applyTo(audioManager: AudioManager, notificationManager: NotificationManager) {
        when (this) {
            NORMAL, VIBRATE, SILENT -> {
                // Leave Do Not Disturb before touching the ringer, or zen mode overrides it.
                notificationManager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_ALL
                )
                audioManager.ringerMode = when (this) {
                    VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                    SILENT -> AudioManager.RINGER_MODE_SILENT
                    else -> AudioManager.RINGER_MODE_NORMAL
                }
            }

            PRIORITY -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                notificationManager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                )
            }

            TOTAL_SILENCE -> notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_NONE
            )
        }
    }

    companion object {
        fun current(
            audioManager: AudioManager,
            notificationManager: NotificationManager
        ): SoundState = when (notificationManager.currentInterruptionFilter) {
            NotificationManager.INTERRUPTION_FILTER_NONE -> TOTAL_SILENCE

            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            NotificationManager.INTERRUPTION_FILTER_ALARMS -> PRIORITY

            else -> when (audioManager.ringerMode) {
                AudioManager.RINGER_MODE_VIBRATE -> VIBRATE
                AudioManager.RINGER_MODE_SILENT -> SILENT
                else -> NORMAL
            }
        }
    }
}
