---
name: android-samples-reference
description: >
  Official Android code samples and templates from developer.android.com/samples.
  Covers Kotlin/Java/C++ patterns across 11 categories: Architecture, Building,
  Camera & Media, Connectivity, Graphics, Sensors, Security & Privacy, Storage,
  Compose UI, Views UI, and Wearables. Use this skill whenever a user asks how to
  implement any Android feature, needs a code snippet, wants a component template,
  asks "how do I do X in Android", references any of the 11 sample categories, or
  wants to learn best practices for Android development. Trigger even for casual
  phrasing like "show me Android camera code", "how does Hilt work", "give me a
  Compose example", or "what's the pattern for Android networking". Always consult
  this skill before answering Android implementation questions — it contains
  authoritative Google-sourced patterns.
---

# Android Samples Reference Skill

Official Google sample index from [developer.android.com/samples](https://developer.android.com/samples).
Primary repo: [github.com/android](https://github.com/android)

---

## How to Use This Skill

1. Identify the **category** from the user's query (table below)
2. Load the corresponding **reference file** for deep snippets
3. Return: pattern name + concise snippet + GitHub link + key deps

---

## Category Index

| # | Category | Trigger Keywords | Reference File |
|---|----------|-----------------|----------------|
| 1 | Architecture | MVVM, ViewModel, Repository, Hilt, DI, Room, Flow, coroutines, LiveData, UseCase, Navigation | `references/categories/architecture.md` |
| 2 | Building | Gradle, build config, flavors, AGP, KSP, buildSrc, version catalog, CI | `references/categories/building.md` |
| 3 | Camera & Media | CameraX, camera2, photo, video, record, MediaPlayer, ExoPlayer, audio, capture | `references/categories/camera-media.md` |
| 4 | Connectivity | Bluetooth, BLE, WiFi, NFC, HTTP, Retrofit, OkHttp, network, socket, WorkManager | `references/categories/connectivity.md` |
| 5 | Graphics | Canvas, OpenGL, Vulkan, shader, animation, custom view, drawing, 2D/3D | `references/categories/graphics.md` |
| 6 | Sensors | Accelerometer, gyroscope, location, GPS, motion, activity recognition, health | `references/categories/sensors.md` |
| 7 | Security & Privacy | Keystore, biometric, encryption, permissions, SafetyNet, certificate pinning | `references/categories/security-privacy.md` |
| 8 | Storage | Room, DataStore, file I/O, SAF, MediaStore, SQLite, SharedPreferences | `references/categories/storage.md` |
| 9 | Compose UI | Composable, LazyColumn, Scaffold, state, theming, Material3, animation, navigation | `references/categories/compose-ui.md` |
| 10 | Views UI | RecyclerView, Fragment, XML layout, View Binding, custom view, MotionLayout | `references/categories/views-ui.md` |
| 11 | Wearables | Wear OS, watch face, health, tiles, complications, Compose for Wear | `references/categories/wearables.md` |

---

## Key GitHub Repositories

```
android/architecture-samples     → MVVM + Hilt + Room + Compose TODO app
android/nowinandroid             → Production-grade full app (Kotlin + Compose)
android/compose-samples          → All Compose UI patterns
android/camera-samples           → CameraX + camera2 samples
android/connectivity-samples     → BLE, WiFi, NFC patterns
android/storage-samples          → Room, DataStore, SAF, MediaStore
android/security-samples         → Biometric, Keystore, permissions
android/wear-os-samples          → Wear OS + Compose for Wear
android/graphics-samples         → Canvas, OpenGL ES, Vulkan
android/sensors-samples          → Motion, location, health sensors
android/play-billing-samples     → In-app billing patterns
```

---

## Quick-Reference Patterns (No File Load Needed)

### Coroutine scope in ViewModel
```kotlin
viewModelScope.launch {
    val result = repository.getData() // suspend fun
    _uiState.value = result
}
```

### Hilt entry point (Activity)
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() { ... }
```

### Compose state hoisting
```kotlin
@Composable
fun Counter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) { Text("$count") }
}
```

### Room DAO basics
```kotlin
@Dao interface ItemDao {
    @Query("SELECT * FROM items") fun getAll(): Flow<List<Item>>
    @Insert suspend fun insert(item: Item)
    @Delete suspend fun delete(item: Item)
}
```

### CameraX preview setup
```kotlin
val preview = Preview.Builder().build()
    .also { it.setSurfaceProvider(viewFinder.surfaceProvider) }
cameraProvider.bindToLifecycle(this, cameraSelector, preview)
```

---

## Response Format

When answering Android implementation queries, structure output as:

```
**Pattern**: [name]
**Category**: [from index]
**Sample Ref**: github.com/android/[repo]
**Min SDK / API**: [level]

[Kotlin snippet]

**Key deps** (build.gradle.kts):
[relevant dependencies]

**Notes**: [gotchas, alternatives, migration tips]
```

---

## When to Load Reference Files

- User asks for **full implementation** → load category `.md`
- User needs **multiple patterns** from same category → load once, answer all
- User asks quick **"how does X work"** → use quick-ref above, skip file load
- User shows existing code for review → load relevant category for best-practice comparison
