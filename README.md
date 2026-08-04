# Sound Tile

> A fork of [**SoundTile** by Mohammad Yasir](https://github.com/hafizmdyasir/SoundTile), maintained by [Gideon Felt](https://github.com/Gideon-Felt).

**Sound Tile** is a minimal, lightweight, Kotlin-based application for Android 15 and above that adds a Quick Settings tile for changing sound modes. Tapping the tile cycles through four states:

**Ringer → Vibrate → Priority Only → Total Silence → Ringer**

The first two set the ringer mode. The last two engage Android's real Do Not Disturb (zen mode), so the system DND indicator appears in the status bar and notifications are suppressed, not just silenced.

> [!WARNING]
> **Total Silence blocks alarms.** It uses `INTERRUPTION_FILTER_NONE`, which suppresses everything including alarms and timers. Don't leave the tile in this state overnight if you rely on an alarm to wake up.

If the ringer is set to silent from somewhere else, the tile displays that state honestly and the next tap returns to Ringer.

## Credit

All original work is by [**Mohammad Yasir**](https://github.com/hafizmdyasir) — the app, its icons, and the Quick Settings tile implementation this fork builds on. The upstream repository is [hafizmdyasir/SoundTile](https://github.com/hafizmdyasir/SoundTile).

Please consider starring the original project if you find this useful.

## What's different in this fork

- **Fixed the tile on Android 17.** Android 17 added background audio hardening, which silently ignores `AudioManager.setRingerMode()` unless the app has a visible activity or a running foreground service that is not of type `shortService`. Because the rejection is a no-op rather than an exception, the tile appeared to do nothing at all. The ringer change now runs inside a `specialUse` foreground service.
- **Added Do Not Disturb states.** Priority Only and Total Silence, via `NotificationManager.setInterruptionFilter()`.
- **Removed the tap latency.** The tile renders the new state immediately instead of waiting for the system broadcast to come back, so all four transitions feel the same.
- **Toolchain updates.** Android Gradle Plugin 9 with built-in Kotlin support, and no third-party dependencies.

## Permissions

The tile requires **Do Not Disturb access** (`android.permission.ACCESS_NOTIFICATION_POLICY`), which is requested the first time you tap the tile. Without it the tile cannot change modes at all.

`FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_SPECIAL_USE` are also declared, for the service that applies the change on Android 17.

## Installation

Add **Sound Tile** to your Quick Settings panel from its edit screen, then grant Do Not Disturb access when prompted on first tap.

Note that the release variant has no signing configuration, so `assembleRelease` produces an unsigned APK that will not install. Either build the debug variant or add your own `signingConfig`.

## Building

Clone the repository and open it in Android Studio, or build from the command line:

```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The logic lives in three small files: `Tiling.kt` (the tile and its display), `ToggleService.kt` (the foreground service that applies the change), and `SoundState.kt` (the state model and cycle order, shared by both).

## License

This fork's contributions are released under the [MIT License](LICENSE).

Note that the upstream project does not specify a license. If you intend to reuse this code, consider contacting the original author regarding the portions of the work that originate there.
