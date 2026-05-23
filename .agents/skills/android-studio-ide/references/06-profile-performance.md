# Android Studio — Profiling & Performance

**Sources:**
- https://developer.android.com/studio/profile
- https://developer.android.com/studio/profile/cpu-profiler
- https://developer.android.com/studio/profile/capture-heap-dump

---

## Android Profiler Overview

**Open:** View > Tool Windows > Profiler (app must be running)
Or: toolbar **Profiler** icon

### Profiler Sessions
- App must be in **debug** build OR **profileable** build for full data
- **Profileable** builds: get performance data without debuggable overhead (recommended for profiling)
- Select device + process from top dropdown

### Profiler Main Window
Three timeline graphs:
1. **CPU** — usage % over time
2. **Memory** — heap usage (Java/Kotlin + Native + Graphics)
3. **Network** — bytes sent/received per second
4. **Energy** — battery usage estimate (if supported)

Click any graph section → opens detailed profiler for that domain.

---

## CPU Profiler

**Open:** Click CPU graph in Profiler main window

### Recording Configurations

| Config | Method | Best For |
|--------|--------|---------|
| **Callstack Sample** | Sampling | Minimal overhead; find hotspots |
| **System Trace** | Perfetto/atrace | UI jank, frame timing, system calls |
| **Java/Kotlin Method Trace** | Instrumentation | Exact call counts + timing (high overhead) |
| **Native Allocation** | malloc sampling | C++ allocation hotspots |

### How to Record
1. Select configuration from dropdown
2. Click **Record** (red circle)
3. Perform the action you want to profile
4. Click **Stop**
5. Analyze trace in panel below

### Reading the CPU Trace

**Call Chart** (top-down flame chart):
- X-axis = time
- Y-axis = call depth (callers at top, callees below)
- Width = time spent in function
- Color: orange = your code, blue = system/framework

**Flame Chart** (aggregated):
- X-axis = total time proportion
- Same function calls merged across all invocations
- Wider = more total time spent

**Top Down / Bottom Up tabs:**
- Top Down: function → children call tree
- Bottom Up: most expensive leaves → callers

### Key Metrics to Watch
- **Wall clock time** — real time elapsed
- **Thread time** — CPU time actually consumed
- Look for: wide blocks in your code on main thread = jank source

### System Trace — Frame Analysis
Best for UI jank investigation:
```kotlin
// Add custom trace events in code
import android.os.Trace

Trace.beginSection("MyExpensiveOperation")
// ... code ...
Trace.endSection()
```

In system trace view:
- **Frames** row — each frame (green = <16ms, yellow = <32ms, red = >32ms)
- **Main thread** — look for long operations blocking Choreographer
- Click frame → shows which functions caused the jank

### Detect Jank
**Studio Jank Detection** (Jellyfish+):
- Profiler > CPU > **Jank Detection** tab
- Lists janky frames with frame duration
- Click frame → shows responsible call stack

---

## Memory Profiler

**Open:** Click Memory graph in Profiler main window

### Memory Categories
| Category | Description |
|----------|-------------|
| Java/Kotlin | Objects allocated in managed heap |
| Native | C/C++ allocations via malloc |
| Graphics | GL textures, framebuffers |
| Stack | Thread stacks |
| Code | APK, DEX, JIT |
| Others | Not categorized |

### Capture Heap Dump
**Heap Dump** button (stack of circles icon) → captures snapshot of all live objects.

In heap dump view:
- **Class list** — all classes, sorted by instance count or retained size
- **Instance list** — all live instances of selected class
- **Reference tree** — what holds reference to selected instance
- Look for: activity/fragment instances = memory leaks!

**Detect Memory Leaks:**
1. Navigate to a screen
2. Navigate back (should be garbage collected)
3. Force GC (Profiler > GC button)
4. Capture heap dump
5. Search for `Activity` / `Fragment` classes
6. If instances still exist → memory leak

### Record Allocations
Click **Record** (allocation tracking):
- Captures every object allocation with stack trace
- High overhead — use for short windows only
- Find which code path allocates most objects

### LeakCanary (Recommended)
Library that auto-detects memory leaks in debug builds:
```kotlin
// build.gradle.kts — debug only
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
```
No configuration needed — shows notification when leak detected.

---

## Network Profiler

**Open:** Click Network graph in Profiler main window

Shows:
- Bytes sent/received timeline
- Each network request as colored segment
- Click request → shows:
  - **Overview** — URL, method, status, timing
  - **Request** — headers + body
  - **Response** — headers + body
  - **Call stack** — which code triggered request

**Supported libraries:** OkHttp, HttpURLConnection (auto-detected). Retrofit auto-detected via OkHttp.

**Note:** Encrypted HTTPS is decrypted for display in profiler (debug builds only).

---

## Energy Profiler

Shows estimated energy usage:
- CPU, network, and location wake locks
- **System events** — Alarms, Jobs, Wake locks
- Identify unexpected background work draining battery

---

## App Startup Profiling

**Baseline Profiles** — pre-compile critical code paths:
```kotlin
// Use Macrobenchmark to generate
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "com.example.app",
        metrics = listOf(StartupTimingMetric()),
        startupMode = StartupMode.COLD,
        iterations = 10
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

Generate Baseline Profile: **Generate Baseline Profile** wizard in Studio (Hedgehog+).

---

## Standalone Profiler

Profile release builds without Android Studio:
```bash
# From SDK tools
$ANDROID_HOME/bin/profiler
```

Or: **Tools > Standalone Profiler**

Connects to profileable app on device. Useful for:
- Profiling without IDE overhead
- Remote profiling sessions
- CI performance testing

---

## Profileable vs Debuggable Builds

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        create("benchmark") {
            initWith(release)
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<application
    android:extractNativeLibs="false"
    android:profileable="true">  <!-- allows profiling without debug overhead -->
```

---

## Macrobenchmark Library

For measuring real-world performance (startup, scrolling, navigation):

```kotlin
// benchmarkModule/src/androidTest/
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollList() = benchmarkRule.measureRepeated(
        packageName = "com.example.app",
        metrics = listOf(FrameTimingMetric()),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            startActivityAndWait()
        }
    ) {
        // Scroll the list
        val list = device.findObject(By.res("itemList"))
        list.fling(Direction.DOWN)
    }
}
```

Run: `./gradlew :benchmark:connectedBenchmarkAndroidTest`

---

## Performance Quick Wins Checklist

**Startup:**
- [ ] Use App Startup library for initializing components
- [ ] Defer non-critical initialization
- [ ] Generate Baseline Profile
- [ ] Enable R8 shrinking (minifyEnabled = true)

**UI/Rendering:**
- [ ] No work on Main thread (use coroutines/flows)
- [ ] Use `LazyColumn`/`LazyRow` for lists
- [ ] `remember` expensive computations in Compose
- [ ] `key` parameter in Compose lists to avoid unnecessary recomposition
- [ ] Reduce composable recompositions (use `@Stable` / `@Immutable`)

**Memory:**
- [ ] No static references to Context/Activity/View
- [ ] Cancel coroutines in `onDestroy`
- [ ] Use `WeakReference` for callbacks
- [ ] Implement LeakCanary in debug builds

**Network:**
- [ ] Cache HTTP responses (OkHttp cache)
- [ ] Paginate large lists
- [ ] Cancel in-flight requests when not needed
- [ ] Use protobuf/flatbuffers instead of JSON for performance-critical paths

**Build speed:**
- [ ] `org.gradle.parallel=true`
- [ ] `org.gradle.caching=true`
- [ ] `org.gradle.configuration-cache=true`
- [ ] Modularize (only changed modules recompile)

---

## APK Analyzer

**Build > Analyze APK** (or drag `.apk`/`.aab` into Studio)

Shows:
- Total APK size + download size
- **File tree:** see each file's size (DEX, resources, native libs)
- **DEX file viewer:** see all classes + method counts
- **Method count** — toward 64K DEX limit
- **Manifest viewer** — merged manifest
- **Resources** — all resources with sizes
- Compare two APKs: **Compare With...** button

Key uses:
- Find large resources to optimize
- Detect accidentally bundled files
- Track size regression between releases
- Verify ProGuard/R8 removed expected classes
