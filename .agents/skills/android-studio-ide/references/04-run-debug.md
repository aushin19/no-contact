# Android Studio — Run, Debug & Devices

**Sources:**
- https://developer.android.com/studio/run
- https://developer.android.com/studio/run/managing-avds
- https://developer.android.com/studio/run/emulator
- https://developer.android.com/studio/debug
- https://developer.android.com/studio/debug/logcat

---

## Running Your App

### Run Controls
| Action | Shortcut (Win) | Shortcut (Mac) | Description |
|--------|---------------|----------------|-------------|
| Run | Shift+F10 | Ctrl+R | Build + deploy + launch |
| Debug | Shift+F9 | Ctrl+D | Run with debugger attached |
| Apply Changes | Ctrl+F10 | Ctrl+Cmd+F10 | Push code changes (no reinstall) |
| Apply Changes + Restart Activity | Ctrl+Shift+F10 | Ctrl+Shift+Cmd+F10 | Restart activity after changes |
| Stop | Ctrl+F2 | Cmd+F2 | Kill the running app |
| Attach Debugger | — | — | Debug > Attach to Process |

### Run/Debug Configuration
**Run > Edit Configurations** (or dropdown next to Run button):
- **Module** — which module to deploy
- **Installation Options** — Always install, install only if changed
- **Launch Options** — Default activity, specific activity, nothing (service)
- **Deployment Target** — USB device, emulator, Firebase Test Lab
- **Before launch** — Gradle tasks to run before deploying

Multiple configs: e.g., one for app, one for library tests, one for specific instrumented test.

### Apply Changes — Limitations
Cannot Apply Changes (requires full reinstall) when:
- `AndroidManifest.xml` changed
- Resources modified (some cases)
- Native code changed
- App process crashed
- Library dependency changed

---

## Device Manager (AVD Manager)

**Open:** Toolbar > Device Manager icon, or **View > Tool Windows > Device Manager**, or **Tools > Device Manager**

Two tabs:
- **Virtual** — Android Virtual Devices (AVDs)
- **Physical** — connected physical devices

### Create AVD
1. Device Manager > **+** (Create Virtual Device)
2. **Hardware profile** — Pixel 6, Pixel Tablet, Wear OS watch, etc.
3. **System Image** — choose API level (download if needed)
   - **Recommended** tab = images with Google Play
   - **x86 Images** = faster (hardware acceleration)
   - **ARM Images** = use only for ARM-specific testing
4. **AVD Config:**
   - RAM (2048 MB min, 4096 MB recommended)
   - Internal storage (min 2 GB)
   - SD Card (optional)
   - Startup orientation
   - Multi-core CPU
5. **Finish** → AVD created

### AVD Actions (in Device Manager)
| Action | Effect |
|--------|--------|
| ▶ Launch | Start emulator |
| ✏️ Edit | Change hardware/system image |
| ⬇ Cold Boot | Boot without snapshot |
| 📷 Snapshot | Take/manage snapshots |
| Wipe Data | Factory reset AVD |
| Duplicate | Clone existing AVD |
| Delete | Remove AVD |

### Recommended AVD Configuration
```
Hardware: Pixel 6 or Pixel 8
System Image: API 34 (Android 14) with Google APIs
RAM: 4096 MB
VM Heap: 256 MB
Internal Storage: 6 GB
Graphics: Automatic (Hardware if HAXM/KVM available)
```

---

## Android Emulator

### Hardware Acceleration Setup

**Windows/Linux (Intel):** Install HAXM via SDK Manager > SDK Tools > Intel x86 Emulator Accelerator (HAXM). Or enable KVM on Linux:
```bash
# Check KVM support
egrep -c '(vmx|svm)' /proc/cpuinfo  # >0 means supported
sudo apt install qemu-kvm
sudo adduser $USER kvm
```

**macOS Apple Silicon:** ARM emulator images run natively (no setup needed). x86 images use Rosetta.

**Windows (AMD):** Enable Hyper-V in Windows Features; HAXM not required.

### Emulator Controls

**Toolbar (right side of emulator):**
- Power / Volume / Rotate / Screenshot
- Back / Home / Overview
- Fold (foldable device profiles)
- Extended controls (three dots `...`)

**Extended Controls (`...` button or `Ctrl+Shift+A`):**

| Panel | Features |
|-------|---------|
| Location | GPS coordinates, GPX/KML route playback |
| Cellular | Network type, signal strength |
| Battery | Level, charger state |
| Camera | Webcam as virtual camera |
| Phone | Incoming calls, SMS |
| Directional Pad | D-pad input |
| Microphone | Audio injection |
| Fingerprint | Simulate fingerprint touch |
| Virtual sensors | Accelerometer, gyroscope, magnetometer |
| Snapshots | Save/load emulator state |
| Settings | Proxy, OpenGL renderer |
| Help | Emulator shortcuts |

### Emulator Keyboard Shortcuts
| Action | Key |
|--------|-----|
| Home | Fn+Home (macOS) / Home |
| Back | Esc |
| Overview (recents) | F2 |
| Power | F3 |
| Volume Up/Down | Ctrl+= / Ctrl+- |
| Camera | Ctrl+5 |
| Rotate CCW/CW | Ctrl+← / Ctrl+→ |
| Screenshot | Ctrl+S |

### Emulator Networking
- Emulator can access host machine at `10.0.2.2` (maps to `localhost` of host)
- Emulator's own `localhost` is `10.0.2.15`
- Internet access via host's connection by default
- Set proxy: Extended Controls > Settings > Proxy
- Multiple emulators: console port starts at 5554, increments by 2

---

## Physical Device Setup

### Enable Developer Options
1. Settings > About Phone > tap **Build Number** 7 times
2. Developer Options appear in Settings (or Settings > System)

### Enable USB Debugging
Settings > Developer Options > **USB debugging** → ON

### Connect via USB
1. Connect USB cable
2. Accept "Allow USB debugging" dialog on device
3. Device appears in Device Manager and device selector

### Connect via Wi-Fi (Android 11+)
1. Settings > Developer Options > **Wireless debugging** → ON
2. In Android Studio: Device Manager > **Pair using Wi-Fi**
3. Scan QR code shown on device, or enter pairing code

### Connect via ADB over Wi-Fi (older method)
```bash
# While USB connected:
adb tcpip 5555
# Disconnect USB, then:
adb connect <device-ip>:5555
```

### OEM USB Drivers (Windows only)
Download from device manufacturer or via SDK Manager > SDK Tools > Google USB Driver.

---

## Debugger

### Breakpoints
- **Click gutter** to add line breakpoint
- **Right-click breakpoint** → configure:
  - Condition: `count > 5` — only break when true
  - Log message: print without stopping
  - Suspend: thread vs all
  - Hit count: break every N hits
- **View all:** Run > View Breakpoints (`Ctrl+Shift+F8`)
- **Disable all:** `Ctrl+Shift+F8` > uncheck
- **Temporary:** Run > Toggle Temporary Line Breakpoint

Breakpoint types:
- **Line breakpoint** (most common)
- **Method breakpoint** — triggers on method enter/exit
- **Field watchpoint** — triggers when field read/written
- **Exception breakpoint** — triggers on specific exception

### Debugger Controls
| Action | Shortcut (Win) | Shortcut (Mac) |
|--------|---------------|----------------|
| Resume | F9 | Cmd+Opt+R |
| Step Over | F8 | F8 |
| Step Into | F7 | F7 |
| Force Step Into | Alt+Shift+F7 | Opt+Shift+F7 |
| Step Out | Shift+F8 | Shift+F8 |
| Run to Cursor | Alt+F9 | Opt+F9 |
| Evaluate Expression | Alt+F8 | Opt+F8 |
| Toggle Breakpoint | Ctrl+F8 | Cmd+F8 |

### Debug Window Panels
- **Frames** — call stack; click frame to navigate
- **Variables** — current scope variables, expandable objects
- **Watches** — custom expressions evaluated continuously
- **Threads** — all active threads; switch between them
- **Console** — stdout/stderr + Logcat filtered to app

### Inline Variable Values
With debugger paused: variable values show inline in editor (gray text). Hover for full value/object inspection.

### Evaluate Expression (`Alt+F8`)
Execute arbitrary Kotlin/Java code in current debug context:
```kotlin
// Examples
myList.size
user.name.uppercase()
database.getAllUsers()
```

### Attach Debugger to Running Process
**Run > Attach Debugger to Android Process** → pick from list → attaches without restarting app. Useful for:
- Debugging app started from launcher
- Release builds with `isDebuggable = true`

---

## Logcat

**Open:** `Alt+6` or View > Tool Windows > Logcat

### Log Levels (severity order)
| Level | Method | Color |
|-------|--------|-------|
| Verbose | `Log.v(TAG, msg)` | White |
| Debug | `Log.d(TAG, msg)` | Blue |
| Info | `Log.i(TAG, msg)` | Green |
| Warning | `Log.w(TAG, msg)` | Yellow |
| Error | `Log.e(TAG, msg)` | Red |
| Assert | `Log.wtf(TAG, msg)` | Red |

### Logcat Filtering
New Logcat panel (Android Studio Dolphin+) uses query syntax:
```
# By tag
tag:MyApp

# By package
package:com.example.app

# By level
level:ERROR

# By message content
message:NullPointer

# Combined
tag:Retrofit level:DEBUG

# Regex
tag~:My.*

# Negate
-tag:ExoPlayer
```

Save filter as named filter for reuse.

### Log Best Practices
```kotlin
// Define TAG per class
private const val TAG = "MainActivity"

// Use BuildConfig to strip verbose logs in release
if (BuildConfig.DEBUG) {
    Log.d(TAG, "Debug info: $data")
}

// Timber library (recommended for production apps)
// Auto-strips in release, no-tag needed
Timber.d("User loaded: %s", userId)
Timber.e(exception, "Failed to fetch data")
```

---

## Layout Inspector

**View > Tool Windows > Layout Inspector** (app must be running)

### For Views (XML layouts)
- Visual 3D layer view — drag to see overdraw layers
- Component tree — hierarchy with properties
- Attributes panel — all properties of selected view
- "Highlight overlapping views" — detect overdraw

### For Compose
- Composable tree with recomposition counts
- Click composable → see modifier chain, parameters
- **Recomposition badge** — shows how many times each composable recomposed
- Jump to source from tree

---

## App Quality Insights (Crashlytics + Vitals)

**View > Tool Windows > App Quality Insights**

Requires:
- Firebase Crashlytics configured in project
- Signed in with Google account

Features:
- View crash reports from production/beta
- Click stack frame → jump to source line
- Filter by: version, OS, device
- Android Vitals ANR reports

---

## ADB (Android Debug Bridge) — Key Commands

Run from terminal (or Android Studio Terminal `Alt+F12`):

```bash
# Device management
adb devices                         # list connected devices
adb -s <serial> shell               # shell on specific device

# App management
adb install app-debug.apk           # install APK
adb install -r app-debug.apk        # reinstall (keep data)
adb uninstall com.example.app       # uninstall

# File transfer
adb push local.txt /sdcard/         # copy to device
adb pull /sdcard/file.txt ./        # copy from device

# Logs
adb logcat                          # all logs
adb logcat -s MyTag                 # filter by tag
adb logcat *:E                      # errors only
adb logcat -c                       # clear log buffer

# Screen
adb shell screencap /sdcard/screen.png
adb pull /sdcard/screen.png
adb shell screenrecord /sdcard/video.mp4

# Input simulation
adb shell input tap 500 300         # tap at x,y
adb shell input text "hello"        # type text
adb shell input keyevent 26         # key event (26=power)

# Activity/Intent
adb shell am start -n com.pkg/.MainActivity
adb shell am force-stop com.example.app

# Network
adb shell ifconfig                  # network interfaces
adb forward tcp:8080 tcp:8080       # port forwarding host→device
adb reverse tcp:8080 tcp:8080       # port forwarding device→host
```

---

## Common Run/Debug Issues

| Issue | Fix |
|-------|-----|
| "No devices" in selector | Check USB debugging; try `adb devices` in terminal |
| Emulator very slow | Enable hardware acceleration (HAXM/KVM); allocate more RAM; use x86 image |
| `INSTALL_FAILED_INSUFFICIENT_STORAGE` | Clear AVD storage or wipe data |
| App crashes immediately | Check Logcat for exception; most common: NullPointerException, missing permission |
| Breakpoints not hit | Ensure debug build; check "Debugger attached" notification; try re-attaching |
| `Apply Changes` fails | Do full Run instead; check for manifest/resource changes |
| Wi-Fi ADB disconnects | Re-run `adb tcpip 5555`; check firewall; use USB instead |
| Emulator black screen on start | Cold boot AVD; wipe data; check GPU settings in AVD config |
