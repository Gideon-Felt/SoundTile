package com.gdfelt.soundtile

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import android.service.quicksettings.TileService

class Tiling : TileService() {

    private val audioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateTile()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        registerReceiver(receiver, IntentFilter().apply {
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        })
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        try { unregisterReceiver(receiver) } catch (_: IllegalArgumentException) { }
    }

    override fun onClick() {
        // Check if the app has permission to modify DND
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Handler(Looper.getMainLooper()).post {
                showPermissionRequestDialog()
            }
            return
        }

        // Show the new state straight away; waiting for the system broadcast to come back
        // reads as a lag on the tile. updateTile() corrects it if the change doesn't land.
        render(SoundState.current(audioManager, notificationManager).next())

        startForegroundService(Intent(this, ToggleService::class.java))
    }

    private fun updateTile() =
        render(SoundState.current(audioManager, notificationManager))

    private fun render(state: SoundState) {
        val tile = qsTile ?: return

        when (state) {
            SoundState.NORMAL -> {
                tile.state = STATE_ACTIVE
                tile.label = getString(R.string.normal)
                tile.icon = Icon.createWithResource(this, R.drawable.normal)
                tile.subtitle = getString(R.string.app_name)
            }

            SoundState.VIBRATE -> {
                tile.state = STATE_INACTIVE
                tile.label = getString(R.string.vibrate)
                tile.icon = Icon.createWithResource(this, R.drawable.vibrate)
                tile.subtitle = getString(R.string.app_name)
            }

            SoundState.SILENT -> {
                tile.state = STATE_INACTIVE
                tile.label = getString(R.string.silent)
                tile.icon = Icon.createWithResource(this, R.drawable.sound_off)
                tile.subtitle = getString(R.string.app_name)
            }

            SoundState.PRIORITY -> {
                tile.state = STATE_ACTIVE
                tile.label = getString(R.string.priority)
                tile.icon = Icon.createWithResource(this, R.drawable.dnd_on)
                tile.subtitle = getString(R.string.priority_on)
            }

            SoundState.TOTAL_SILENCE -> {
                tile.state = STATE_ACTIVE
                tile.label = getString(R.string.total_silence)
                tile.icon = Icon.createWithResource(this, R.drawable.total_silence)
                tile.subtitle = getString(R.string.priority_off)
            }
        }

        tile.updateTile()
    }

    private fun showPermissionRequestDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.permission_description))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                PendingIntent.getActivity(this,0,
                    Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ).send()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        showDialog(dialog)
    }
}