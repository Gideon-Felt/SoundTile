package com.gdfelt.soundtile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder

/**
 * Android 17 silently ignores ringer changes unless the app has a visible activity or a running
 * foreground service that is not of type shortService, so the toggle runs here rather than
 * directly in [Tiling.onClick].
 */
class ToggleService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // The ringer change must happen while the service is already foreground.
        startForeground(NOTIFICATION_ID, createNotification())

        val audioManager = getSystemService(AudioManager::class.java)
        val notificationManager = getSystemService(NotificationManager::class.java)
        SoundState.current(audioManager, notificationManager)
            .next()
            .applyTo(audioManager, notificationManager)

        stopSelf()
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.toggling))
            .setSmallIcon(R.drawable.normal)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "ringer_toggle"
        const val NOTIFICATION_ID = 1
    }
}
