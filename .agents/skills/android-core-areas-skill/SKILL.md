---
name: android-core-areas
description: >
  Authoritative reference for 15 Android core development areas from developer.android.com/develop.
  Use this skill whenever the user asks about Android data storage, file access, Room database,
  SharedPreferences, DataStore, runtime permissions, user identity, sign-in, navigation graphs,
  deep links, intents, intent filters, background work, WorkManager, services, foreground services,
  alarms, AlarmManager, audio/video/media, ExoPlayer, camera, CameraX, sensors, location,
  FusedLocationProvider, network connectivity, app compatibility, AAB, app bundles, or Google Play
  publishing. Also trigger for questions about scoped storage, JobScheduler, MediaSession,
  Bluetooth, Wi-Fi Direct, NFC, backward compatibility, or any question that maps to the Android
  Develop Core Areas section. Always consult this skill before answering Android platform questions
  — your training data may reference outdated APIs.
---

# Android Core Areas — Reference Skill

Source: https://developer.android.com/develop#core-areas  
Last verified: 2025. Always link users to canonical docs for latest changes.

---

## 1. Data & Files

**Canonical:** https://developer.android.com/guide/topics/data  
**Overview:** https://developer.android.com/training/data-storage

### Storage Categories

| Category | Use For | API |
|---|---|---|
| App-specific internal | Private files, no permission needed | `Context.filesDir`, `Context.cacheDir` |
| App-specific external | Larger private files | `Context.getExternalFilesDir()` |
| Shared storage — media | Photos, audio, video shared with other apps | `MediaStore` |
| Shared storage — docs | Non-media files, user-chosen | Storage Access Framework (`DocumentsContract`) |
| Preferences | Key-value primitives | `DataStore<Preferences>` (preferred) or `SharedPreferences` |
| Database | Structured relational data | Room (`androidx.room`) |

### Key APIs & Libraries

**Room (SQLite abstraction)**
- `@Entity`, `@Dao`, `@Database` annotations
- `@Query`, `@Insert`, `@Update`, `@Delete`
- `Flow`/`LiveData` return types for reactive queries
- Migrations: `Migration(from, to)` blocks; `fallbackToDestructiveMigration()` escape hatch
- Relations: `@Relation`, `@Embedded`, junction tables via `@Junction`
- Docs: https://developer.android.com/training/data-storage/room

**DataStore** (replaces SharedPreferences)
- `Preferences DataStore` — untyped key-value
- `Proto DataStore` — typed via Protocol Buffers
- Coroutine + Flow based; safe on UI thread
- Dep: `androidx.datastore:datastore-preferences`
- Docs: https://developer.android.com/topic/libraries/architecture/datastore

**Shared Storage / Scoped Storage**
- Android 10+ enforces scoped storage — apps can't access arbitrary external paths
- `MediaStore` for media; `photo-picker` API (no permission needed, Android 13+)
- Storage Access Framework for documents: `ACTION_OPEN_DOCUMENT`, `ACTION_CREATE_DOCUMENT`
- `MANAGE_EXTERNAL_STORAGE` permission only for file manager apps
- Photo picker: https://developer.android.com/training/data-storage/shared/photo-picker

**Content Providers**
- Share data across apps: extend `ContentProvider`, expose via `authorities` manifest entry
- `FileProvider` (Jetpack) for sharing files via `content://` URIs safely
- Docs: https://developer.android.com/guide/topics/providers/content-providers

### Gotchas
- Never use `MODE_WORLD_READABLE/WRITABLE` — removed API 17+
- `cacheDir` files may be deleted by OS under storage pressure
- Room queries on main thread throw by default; use coroutines or `allowMainThreadQueries()` only in tests

---

## 2. Permissions

**Canonical:** https://developer.android.com/guide/topics/permissions/overview

### Permission Types

| Type | Granted | Examples |
|---|---|---|
| Install-time (normal) | Automatically on install | `INTERNET`, `VIBRATE` |
| Runtime (dangerous) | User prompt at runtime | `CAMERA`, `READ_CONTACTS`, `ACCESS_FINE_LOCATION` |
| Signature | Same certificate only | Custom app-to-app perms |
| Special app access | Settings UI required | `MANAGE_EXTERNAL_STORAGE`, `SYSTEM_ALERT_WINDOW` |

### Request Flow (Runtime)
1. Declare in `AndroidManifest.xml`
2. Check: `ContextCompat.checkSelfPermission(ctx, permission)`
3. Rationale check: `ActivityCompat.shouldShowRequestPermissionRationale()`
4. Request: `ActivityResultLauncher<String>` + `ActivityResultContracts.RequestPermission()`
5. Handle result in callback

```kotlin
val launcher = registerForActivityResult(RequestPermission()) { granted ->
    if (granted) { /* proceed */ } else { /* explain or degrade */ }
}
launcher.launch(Manifest.permission.CAMERA)
```

### Key Docs
- Permission groups: https://developer.android.com/guide/topics/permissions/overview#permission-groups
- Best practices: https://developer.android.com/training/permissions/usage-notes
- One-time permissions (Android 11+): https://developer.android.com/training/permissions/one-time
- Permission auto-reset (Android 11+): unused app permissions reset after months of non-use

### Gotchas
- Never request permissions at app launch without context
- `READ_MEDIA_IMAGES/VIDEO/AUDIO` replaces `READ_EXTERNAL_STORAGE` on Android 13+
- Background location (`ACCESS_BACKGROUND_LOCATION`) requires separate request AFTER foreground location granted
- Bluetooth perms changed significantly in Android 12 (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`)

---

## 3. User Identity

**Canonical:** https://developer.android.com/training/sign-in

### Sign-In Options

| Method | Library | Use Case |
|---|---|---|
| Google Sign-In (Credential Manager) | `androidx.credentials` | First-party Google identity |
| Passkeys | `androidx.credentials` | Passwordless, phishing-resistant |
| Federated identity (OIDC/OAuth) | Custom + AppAuth | Third-party IdPs |
| Account Manager | `AccountManager` (platform) | System-level account management |

### Credential Manager (preferred, Android 14+ primary, backport to API 16)
- Unified API for passwords, passkeys, federated sign-in
- Dep: `androidx.credentials:credentials`, `androidx.credentials:credentials-play-services-auth`
- `CredentialManager.getCredential(request)` — retrieve
- `CredentialManager.createCredential(request)` — register passkey
- Docs: https://developer.android.com/training/sign-in/passkeys

### Google Identity Services
- One Tap: `BeginSignInRequest` → `SignInClient.beginSignIn()`
- Returns `PendingIntent`; launch with `IntentSenderRequest`
- After success: extract `GoogleIdTokenCredential` → verify on server
- Docs: https://developer.android.com/training/sign-in/credential-manager-siwg

### Gotchas
- Deprecated: `GoogleSignIn` (legacy) — migrate to Credential Manager
- Always verify ID tokens server-side; never trust client-only
- Handle `NoCredentialException` gracefully (user has no saved creds)

---

## 4. Navigation

**Canonical:** https://developer.android.com/guide/navigation/use-graph/navigate

### Navigation Component (Jetpack)

**Core artifacts:**
- `androidx.navigation:navigation-fragment-ktx`
- `androidx.navigation:navigation-ui-ktx`
- `androidx.navigation:navigation-compose` (Compose)

**Concepts:**
- `NavGraph` — XML or code-defined destination graph
- `NavController` — controls back stack; `findNavController()` from Fragment/Activity
- `NavHost` / `NavHostFragment` — container that swaps destinations
- `SafeArgs` plugin — type-safe argument passing between destinations

**Navigate:**
```kotlin
navController.navigate(R.id.action_home_to_detail)
// or with args (SafeArgs):
navController.navigate(HomeFragmentDirections.actionHomeToDetail(itemId = 42))
```

**Deep links:**
- Explicit: `NavDeepLinkBuilder` — PendingIntent to specific destination
- Implicit: declare `<deepLink app:uri="..."/>` in nav graph; handle in Activity
- Docs: https://developer.android.com/guide/navigation/design/deep-link

**Compose Navigation:**
- `NavHost { composable("route") { } }`
- `navController.navigate("route")`
- Type-safe routes via `@Serializable` objects (Nav 2.8+)
- Docs: https://developer.android.com/guide/navigation/navigation-compose

### Gotchas
- Use `popUpTo` + `inclusive` to avoid back-stack buildup in bottom nav
- `launchSingleTop = true` prevents duplicate destinations on re-select
- Shared element transitions require `Transition` API wiring in both fragments

---

## 5. Intents & Intent Filters

**Canonical:** https://developer.android.com/guide/components/intents-filters

### Intent Types

| Type | Target | Example |
|---|---|---|
| Explicit | Specific component | Start own Activity/Service |
| Implicit | Action + category | `ACTION_VIEW`, `ACTION_SEND` |

### Common Implicit Intent Actions
- `Intent.ACTION_VIEW` — view URI (browser, maps, dialer)
- `Intent.ACTION_SEND` / `ACTION_SEND_MULTIPLE` — share content
- `Intent.ACTION_PICK` — pick item from data source
- `Intent.ACTION_DIAL` / `ACTION_CALL` — phone

### Intent Filters (Manifest)
```xml
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="text/plain" />
</intent-filter>
```

### Pending Intents
- Wrap intent for deferred execution (notifications, alarms, widgets)
- `PendingIntent.FLAG_IMMUTABLE` required Android 12+
- `PendingIntent.FLAG_MUTABLE` only when mutation needed (e.g., inline reply)

### Resolving & Safety
- `Intent.resolveActivity()` — check handler exists before startActivity (API < 30)
- Android 11+: declare `<queries>` in manifest to query other app packages
- `startActivity()` from non-Activity context requires `FLAG_ACTIVITY_NEW_TASK`

### Gotchas
- Implicit intents to exported=false components blocked Android 12+
- `PackageManager.queryIntentActivities()` requires `<queries>` declaration API 30+

---

## 6. Background Tasks

**Canonical:** https://developer.android.com/develop/background-work/background-tasks

### Decision Tree

```
Task needs to run while app is visible?
  → Coroutines (viewModelScope / lifecycleScope)

Task deferred, constraint-based, persistent?
  → WorkManager

Task immediate, short, one-off?
  → Coroutines + foreground service if long-running

Exact timing required?
  → AlarmManager (see §8)

User-initiated long operation (download, upload)?
  → Foreground Service
```

### WorkManager (primary recommendation for deferrable work)
- Dep: `androidx.work:work-runtime-ktx`
- Survives process death and device reboots
- `Worker` / `CoroutineWorker` — define work unit
- `WorkRequest` — `OneTimeWorkRequest` or `PeriodicWorkRequest`
- `Constraints` — network, charging, battery not low, storage
- Chaining: `beginWith().then().enqueue()`
- Unique work: `enqueueUniqueWork()` — avoid duplicate chains
- Docs: https://developer.android.com/topic/libraries/architecture/workmanager

### Coroutines for Short Background Work
- `viewModelScope.launch(Dispatchers.IO) { }` — tied to ViewModel lifecycle
- `lifecycleScope.launch { }` — tied to Activity/Fragment
- Cancelled automatically on lifecycle destroy

### Restrictions (Android 8+)
- Background execution limits: implicit broadcast receivers removed; background service limits
- Apps in background can't start services — use `startForegroundService()` + call `startForeground()` within 5s
- Exact alarm restrictions: Android 12+ requires `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM`

---

## 7. Services

**Canonical:** https://developer.android.com/guide/components/services

### Service Types

| Type | Use Case | Key Method |
|---|---|---|
| Started Service | Background task without UI binding | `onStartCommand()` |
| Bound Service | IPC / client-server in same or different process | `onBind()` returns `IBinder` |
| Foreground Service | Long-running user-visible task | `startForeground(id, notification)` |
| Background Service | Restricted; avoid on API 26+ | Legacy |

### Foreground Services (primary pattern for long tasks)
- Requires persistent notification
- Declare type in manifest: `android:foregroundServiceType="camera|location|mediaPlayback|..."` 
- Android 14: must declare type; call `startForeground(id, notif, serviceType)` with matching type
- Types: `camera`, `connectedDevice`, `dataSync`, `health`, `location`, `mediaPlayback`, `mediaProjection`, `microphone`, `phoneCall`, `remoteMessaging`, `shortService`, `specialUse`, `systemExempted`
- Docs: https://developer.android.com/develop/background-work/services/foreground-services

### Bound Services
- Same process: `Binder` subclass
- Different process: `Messenger` (simpler) or AIDL (complex IPC)
- Lifecycle: alive while ≥1 client bound; destroyed when last client unbinds

### IntentService — Deprecated
- Replaced by `WorkManager` + `CoroutineWorker`

### Gotchas
- `startForegroundService()` → must call `startForeground()` within 5s or ANR/crash
- Services run on main thread by default — dispatch work to background thread/coroutine
- Bound service `onBind()` called once; subsequent binds reuse same `IBinder`

---

## 8. Alarms

**Canonical:** https://developer.android.com/training/scheduling

### AlarmManager
- System-level scheduling; survives app death
- `setExact()` — exact, inexact trigger batching avoided
- `setExactAndAllowWhileIdle()` — fires even in Doze mode
- `setAlarmClock()` — user-visible; shown in status bar; highest priority
- `setRepeating()` — inexact repeating (prefer `PeriodicWorkRequest` for deferrable)

### Permission Requirements (Android 12+)
- `SCHEDULE_EXACT_ALARM` — user-grantable; can be revoked
- `USE_EXACT_ALARM` — auto-granted; restricted to specific use cases (alarms, reminders, calendar)
- Handle `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` broadcast

### Doze Mode Considerations
- Standard alarms deferred during Doze
- Use `setExactAndAllowWhileIdle()` only for time-critical (once per 9min limit in Doze)
- Declare `RECEIVE_BOOT_COMPLETED` to reschedule alarms after reboot

### BroadcastReceiver for Alarm Delivery
```kotlin
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { /* do work */ }
}
// PendingIntent must use FLAG_IMMUTABLE
val pi = PendingIntent.getBroadcast(ctx, 0, Intent(ctx, AlarmReceiver::class.java),
    PendingIntent.FLAG_IMMUTABLE)
alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
```

### Gotchas
- WorkManager preferred for deferrable/constraint-based scheduling
- AlarmManager for user-facing alarms and calendar events only (per Play policy)

---

## 9. Audio & Video

**Canonical:** https://developer.android.com/guide/topics/media  
**Dev Center:** https://developer.android.com/media

### Media3 / ExoPlayer (recommended)
- Dep: `androidx.media3:media3-exoplayer`, `media3-ui`, `media3-session`
- Replaces standalone ExoPlayer 2.x and MediaPlayer for most use cases
- `ExoPlayer` instance → set `MediaItem` → `prepare()` → `play()`
- `PlayerView` / `PlayerSurface` for UI
- `MediaSession` (Media3) — expose controls to system UI, Wear, Auto
- Adaptive streaming: HLS, DASH, SmoothStreaming built-in
- Docs: https://developer.android.com/media/media3

### MediaPlayer (platform, simpler use cases)
- `MediaPlayer.create(ctx, uri)` → `start()` / `pause()` / `release()`
- Must call `release()` — holds codec resources
- State machine: Idle → Initialized → Prepared → Started/Paused/Stopped

### Audio Focus
- Must request audio focus before playback; abandon on pause/stop
- `AudioManager.requestAudioFocus()` / `AudioFocusRequest` (API 26+)
- Handle `AUDIOFOCUS_LOSS`, `AUDIOFOCUS_LOSS_TRANSIENT`, `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK`

### Audio Recording
- `MediaRecorder` — simple recording to file
- `AudioRecord` — raw PCM access for processing
- Permission: `RECORD_AUDIO`

### Video Recording
- `MediaRecorder` with `setVideoSource(CAMERA)`
- CameraX VideoCapture use case (see §10) preferred for modern apps

### Notifications & Media Controls
- `MediaStyleNotification` with `MediaSession` token for lock screen / system controls
- Docs: https://developer.android.com/media/implement/surfaces/mobile

---

## 10. Camera

**Canonical:** https://developer.android.com/training/camera/choose-camera-library

### CameraX (recommended)
- Dep: `androidx.camera:camera-camera2`, `camera-lifecycle`, `camera-view`, `camera-video`
- Use cases: `Preview`, `ImageCapture`, `ImageAnalysis`, `VideoCapture`
- `ProcessCameraProvider.getInstance(ctx)` → bind use cases to lifecycle
- `PreviewView` — handles Surface setup automatically
- `ImageAnalysis.Analyzer` — per-frame processing (ML Kit integration)
- Consistent API across Android 5.0+ devices
- Docs: https://developer.android.com/training/camerax

```kotlin
val cameraProvider = ProcessCameraProvider.getInstance(this).get()
val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
val imageCapture = ImageCapture.Builder().build()
cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
```

### Camera2 (low-level)
- Full manual control: exposure, focus, raw capture
- `CameraManager` → `CameraDevice` → `CaptureSession` → `CaptureRequest`
- Significantly more boilerplate; use only when CameraX insufficient
- Docs: https://developer.android.com/training/camera2

### Permission
- `CAMERA` runtime permission required
- `RECORD_AUDIO` for video with audio

### Gotchas
- CameraX extensions (Bokeh, HDR, Night, Face Retouch) via `camera-extensions` dep
- Always release camera in `onPause()` / via lifecycle binding
- CameraX handles rotation and aspect ratio automatically; Camera2 does not

---

## 11. Sensors

**Canonical:** https://developer.android.com/guide/topics/sensors/sensors_overview

### Sensor Framework
- `SensorManager` — access via `getSystemService(SENSOR_SERVICE)`
- `Sensor` — represents hardware sensor
- `SensorEventListener` — receive data via `onSensorChanged(event)`, `onAccuracyChanged()`

### Sensor Categories

| Category | Examples |
|---|---|
| Motion | Accelerometer, Gyroscope, Gravity, Linear Acceleration, Step Counter/Detector |
| Position | Magnetometer, Proximity, Game Rotation Vector |
| Environment | Ambient Temperature, Light, Pressure (Barometer), Humidity |

### Registration
```kotlin
val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_NORMAL)
// Always unregister:
sensorManager.unregisterListener(listener)
```

### Sampling Rates
- `SENSOR_DELAY_NORMAL` (200ms) — UI updates
- `SENSOR_DELAY_GAME` (20ms) — games
- `SENSOR_DELAY_FASTEST` — max rate; high power
- API 19+: `registerListener(listener, sensor, samplingPeriodUs, maxReportLatencyUs)` for batching

### Step Counter / Detector
- `TYPE_STEP_COUNTER` — cumulative since last reboot; persists across app restarts
- `TYPE_STEP_DETECTOR` — event per step; no count
- Permission: `ACTIVITY_RECOGNITION` (Android 10+)

### Gotchas
- Unregister in `onPause()` — sensors drain battery significantly
- Sensor availability varies by device; always null-check `getDefaultSensor()`
- Use `Sensor.TYPE_ROTATION_VECTOR` over raw accelerometer + magnetometer for orientation

---

## 12. User Location

**Canonical:** https://developer.android.com/training/location

### Fused Location Provider (recommended)
- Dep: `com.google.android.gms:play-services-location`
- `FusedLocationProviderClient` — smart fusion of GPS, Wi-Fi, cell
- `getCurrentLocation(priority, cancellationToken)` — one-shot
- `requestLocationUpdates(request, callback, looper)` — continuous
- Priority: `PRIORITY_HIGH_ACCURACY` (GPS), `PRIORITY_BALANCED_POWER_ACCURACY` (Wi-Fi/cell), `PRIORITY_LOW_POWER`, `PRIORITY_PASSIVE`

```kotlin
val client = LocationServices.getFusedLocationProviderClient(this)
val request = CurrentLocationRequest.Builder().setPriority(PRIORITY_HIGH_ACCURACY).build()
client.getCurrentLocation(request, cancellationToken).addOnSuccessListener { loc -> }
```

### Permissions
- `ACCESS_COARSE_LOCATION` — approximate (city block)
- `ACCESS_FINE_LOCATION` — precise (GPS)
- `ACCESS_BACKGROUND_LOCATION` — Android 10+; request separately AFTER foreground granted
- Android 12+: users can grant approximate location even when fine is requested

### Geofencing
- `GeofencingClient.addGeofences()` with `GeofenceRequest`
- Max 100 geofences per app
- `GeofenceBroadcastReceiver` handles `GEOFENCE_TRANSITION_ENTER/EXIT/DWELL`
- Docs: https://developer.android.com/training/location/geofencing

### Platform LocationManager (no Play Services required)
- `getSystemService(LOCATION_SERVICE) as LocationManager`
- `requestLocationUpdates(provider, minTime, minDistance, listener)`
- Use when Play Services unavailable (AOSP devices, China)

### Gotchas
- Background location requires prominent disclosure + Play policy compliance
- Mock locations: `android.permission.ACCESS_MOCK_LOCATION` for testing
- `getLastKnownLocation()` may be null or stale; always validate timestamp

---

## 13. Connectivity

**Canonical:** https://developer.android.com/develop/connectivity

### Network State
- `ConnectivityManager.getNetworkCapabilities(activeNetwork)` — check capabilities
- `NetworkCallback` — register for network changes
- `NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET).build()`
- Deprecated: `ConnectivityManager.activeNetworkInfo` (removed API 29+)

### HTTP / REST
- **OkHttp** — low-level HTTP client (Retrofit uses it internally)
- **Retrofit** — type-safe REST; annotations define endpoints
- **Ktor Client** — Kotlin-first, coroutine-native
- Dep: `com.squareup.retrofit2:retrofit`, `com.squareup.okhttp3:okhttp`

### Bluetooth
- Classic BT: `BluetoothAdapter` → `BluetoothDevice` → `BluetoothSocket`
- BLE: `BluetoothLeScanner` → `connectGatt()` → `BluetoothGattCallback`
- Android 12+ perms: `BLUETOOTH_SCAN` (scanning), `BLUETOOTH_CONNECT` (connect/pair)
- Companion Device Manager: `CompanionDeviceManager` for persistent pairing
- Docs: https://developer.android.com/develop/connectivity/bluetooth

### Wi-Fi
- Wi-Fi Direct: `WifiP2pManager` for peer-to-peer without AP
- Network Suggestion API: `WifiNetworkSuggestion` to suggest networks (API 29+)
- Wi-Fi Aware (NAN): proximity-based comms without AP (API 26+)
- Docs: https://developer.android.com/develop/connectivity/wifi

### NFC
- `NfcAdapter` → `enableForegroundDispatch()` / `enableReaderMode()`
- NDEF read: parse `NdefMessage` from intent
- HCE (Host-based Card Emulation): `HostApduService` for card emulation
- Permission: `NFC`
- Docs: https://developer.android.com/develop/connectivity/nfc

### USB
- `UsbManager` → enumerate devices or accessories
- Permission: request `UsbManager.requestPermission()` at runtime
- Docs: https://developer.android.com/develop/connectivity/usb

---

## 14. App Compatibility

**Canonical:** https://developer.android.com/guide/app-compatibility

### Core Principle
- `minSdk` — oldest supported Android version
- `targetSdk` — declared behavior version; triggers platform compat changes
- `compileSdk` — API level used to compile; should be latest stable

### AndroidX / Jetpack
- All modern APIs via AndroidX — backported, decoupled from OS releases
- `androidx.core:core-ktx` — Kotlin extensions for platform APIs
- `AppCompatActivity` — compat backport for ActionBar, night mode, etc.

### Behavior Changes by API Level (key ones)
| API | Change |
|---|---|
| 26 | Background service limits; notification channels required |
| 28 | Cleartext traffic blocked by default; Apache HTTP removed |
| 29 | Scoped storage enforced; `ACCESS_BACKGROUND_LOCATION` separate |
| 30 | Package visibility restrictions; `<queries>` required |
| 31 | PendingIntent mutability flags required; exact alarm permission |
| 33 | Per-media-type storage permissions; notification runtime permission (`POST_NOTIFICATIONS`) |
| 34 | Foreground service type required; photo picker enhancements |
| 35 | Edge-to-edge enforced; predictive back enforced |

### Testing Compatibility
- `@SdkSuppress(minSdkVersion = X)` — skip tests below API level
- `Build.VERSION.SDK_INT` guards for runtime API checks
- `BuildCompat.isAtLeastX()` for pre-release checks
- Android Lint: `NewApi` check flags API usage above minSdk

### Desugaring
- `coreLibraryDesugaring` — backport Java 8+ APIs (streams, time) to older devices
- Enable in `compileOptions` + add `com.android.tools:desugar_jdk_libs` dep

---

## 15. App Bundles

**Canonical:** https://developer.android.com/guide/app-bundle

### Android App Bundle (AAB)
- `.aab` format — replaces monolithic APK for Play Store distribution
- Google Play generates optimized APKs per device config (ABI, density, language)
- Required for new apps on Play Store (since Aug 2021)
- Build: `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`

### Dynamic Feature Modules
- Deliver features on-demand, conditionally, or at install
- `com.android.dynamic-feature` plugin
- `SplitInstallManager` — request, monitor, handle module installs at runtime
- Install modes: `onDemand`, `onInstall`, `conditionally`
- Docs: https://developer.android.com/guide/playcore/feature-delivery

### Play Asset Delivery
- Large asset packs (>150MB) delivered via Play
- Pack types: `install-time`, `fast-follow`, `on-demand`
- `AssetPackManager` for on-demand/fast-follow
- Docs: https://developer.android.com/guide/playcore/asset-delivery

### Play Feature Delivery vs Asset Delivery
- Feature Delivery → code + resources in dynamic modules
- Asset Delivery → large binary assets (textures, ML models, audio)

### Testing Locally
- `bundletool` — build and test APK sets locally
- `bundletool build-apks --bundle=app.aab --output=app.apks --connected-device`
- `bundletool install-apks --apks=app.apks`
- Download: https://github.com/google/bundletool

### Signing
- Upload key → Play signs with app signing key (Google manages delivery key)
- Enroll in Play App Signing (mandatory for AAB)

### Gotchas
- Dynamic features can't be accessed via reflection from base module without install check
- `SplitCompat.install(ctx)` required in Application class for immediate access post-install
- Instant apps use same AAB format with `<dist:module dist:instant="true"/>`

---

## Quick Reference: Jetpack Library Index

| Topic | Primary Jetpack Dep |
|---|---|
| Room | `androidx.room:room-runtime`, `room-ktx` |
| DataStore | `androidx.datastore:datastore-preferences` |
| WorkManager | `androidx.work:work-runtime-ktx` |
| Navigation | `androidx.navigation:navigation-fragment-ktx` |
| CameraX | `androidx.camera:camera-camera2`, `camera-lifecycle` |
| Media3 | `androidx.media3:media3-exoplayer` |
| Credentials | `androidx.credentials:credentials` |
| Lifecycle | `androidx.lifecycle:lifecycle-viewmodel-ktx` |

## Quick Reference: Doc URLs

| Topic | URL |
|---|---|
| Data & Files | https://developer.android.com/training/data-storage |
| Permissions | https://developer.android.com/guide/topics/permissions/overview |
| User Identity | https://developer.android.com/training/sign-in |
| Navigation | https://developer.android.com/guide/navigation/use-graph/navigate |
| Intents | https://developer.android.com/guide/components/intents-filters |
| Background Tasks | https://developer.android.com/develop/background-work/background-tasks |
| Services | https://developer.android.com/guide/components/services |
| Alarms | https://developer.android.com/training/scheduling |
| Audio & Video | https://developer.android.com/guide/topics/media |
| Camera | https://developer.android.com/training/camera/choose-camera-library |
| Sensors | https://developer.android.com/guide/topics/sensors/sensors_overview |
| Location | https://developer.android.com/training/location |
| Connectivity | https://developer.android.com/develop/connectivity |
| App Compatibility | https://developer.android.com/guide/app-compatibility |
| App Bundles | https://developer.android.com/guide/app-bundle |
