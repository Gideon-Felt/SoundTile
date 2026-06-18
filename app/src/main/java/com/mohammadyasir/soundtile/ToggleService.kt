package com.mohammadyasir.soundtile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ToggleService: Service() {

    override fun onBind(intent: Intent): IBinder? = null


    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val notification = createNotification()

        // REQUIRED: move service to foreground immediately
        startForeground(1, notification)
        val audioManager = getSystemService(AudioManager::class.java)
        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }
            else -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        }
        stopSelf()
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "my_channel"

        val channel = NotificationChannel(
            channelId,
            "Change Ringer Mode Service",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Running")
            .setContentText("Foreground service active, updating ringer mode.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}