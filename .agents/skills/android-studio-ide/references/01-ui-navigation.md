# Android Studio — UI Navigation & Configuration

**Source:** https://developer.android.com/studio/intro/user-interface

---

## IDE Window Anatomy

```
┌─────────────────────────────────────────────────────────────┐
│  Menu Bar  │  Toolbar (Run, AVD, SDK, Sync, Profile icons)  │
├──────┬──────────────────────────────────────────┬───────────┤
│      │                                          │           │
│  P   │         Editor Area                      │ Structure /│
│  r   │   (tabs per open file)                   │ Bookmarks  │
│  o   │                                          │           │
│  j   ├──────────────────────────────────────────┤           │
│  e   │                                          │           │
│  c   │   Tool Windows (bottom / sides)          │           │
│  t   │   Logcat | Build | Run | Problems | etc  │           │
│      │                                          │           │
├──────┴──────────────────────────────────────────┴───────────┤
│  Status Bar: Gradle status | Inspections | Git branch | VCS  │
└─────────────────────────────────────────────────────────────┘
```

---

## Main UI Components

### 1. Toolbar
Key icons left → right:
- **Hammer** — Make Project
- **Module selector** — choose which module/config to run
- **Device selector** — target device or emulator
- **▶ Run / 🐛 Debug / 🔥 Apply Changes** — launch controls
- **Profiler** — open Android Profiler
- **AVD Manager** — virtual device manager
- **SDK Manager** — SDK platforms & tools
- **Sync** (elephant) — Sync Project with Gradle Files
- **Search** — Search Everywhere
- **Profile icon** — Google account / Gemini sign-in

### 2. Project Tool Window (`Alt+1` / `Cmd+1`)
Views (dropdown at top of panel):
- **Android** (default) — module-centric, hides irrelevant files
- **Project** — real filesystem tree
- **Packages** — by Java/Kotlin package
- **Problems** — files with lint/compile issues

Key folders in Android view:
```
app/
├── manifests/       → AndroidManifest.xml
├── java/            → Kotlin/Java source + test source
│   ├── com.pkg.app  → main source set
│   ├── com.pkg.app (androidTest)
│   └── com.pkg.app (test)
├── res/             → layouts, drawables, values, etc.
└── Gradle Scripts/  → build.gradle.kts, gradle.properties, settings.gradle.kts
```

### 3. Editor Area
- Multiple files open as **tabs**
- **Split editor:** Right-click tab > Split Right/Down; or drag tab
- **Recent files:** `Ctrl+E` / `Cmd+E`
- **Navigate back/forward:** `Ctrl+Alt+←/→` / `Cmd+[/]`
- **Gutter:** line numbers, breakpoints, VCS change indicators, folding arrows

### 4. Tool Windows
Open via **View > Tool Windows** or keyboard shortcuts:

| Window | Shortcut (Win) | Shortcut (Mac) | Purpose |
|--------|---------------|----------------|---------|
| Project | Alt+1 | Cmd+1 | File tree |
| Bookmarks | Alt+2 | Cmd+2 | Bookmarked lines |
| Find | Alt+3 | Cmd+3 | Find results |
| Run | Alt+4 | Cmd+4 | App run output |
| Debug | Alt+5 | Cmd+5 | Debugger |
| Logcat | Alt+6 | Cmd+6 | Device logs |
| Structure | Alt+7 | Cmd+7 | File structure |
| Git | Alt+9 | Cmd+9 | VCS operations |
| Build | — | — | Gradle build output |
| Terminal | Alt+F12 | Opt+F12 | Shell |
| Profiler | — | — | Performance profiling |
| Device Manager | — | — | AVD + physical devices |

**Hide/show all tool windows:** `Ctrl+Shift+F12` / `Cmd+Shift+F12`

### 5. Status Bar (bottom)
- **Left:** current file path
- **Center:** background task progress (Gradle, indexing)
- **Right:** inspections count | encoding | line endings | Git branch | memory indicator

Click the **memory indicator** (rightmost) to run GC / see heap usage.

---

## Essential Keyboard Shortcuts

### Navigation
| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Search Everywhere | Double Shift | Double Shift |
| Find Action | Ctrl+Shift+A | Cmd+Shift+A |
| Go to File | Ctrl+Shift+N | Cmd+Shift+O |
| Go to Class | Ctrl+N | Cmd+O |
| Go to Symbol | Ctrl+Alt+Shift+N | Cmd+Opt+O |
| Go to Declaration | Ctrl+B / F4 | Cmd+B / F4 |
| Go to Implementation | Ctrl+Alt+B | Cmd+Opt+B |
| Recent Files | Ctrl+E | Cmd+E |
| Navigate Back | Ctrl+Alt+← | Cmd+[ |
| Navigate Forward | Ctrl+Alt+→ | Cmd+] |
| Go to Line | Ctrl+G | Cmd+L |
| File Structure | Ctrl+F12 | Cmd+F12 |

### Editing
| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Auto-complete | Ctrl+Space | Ctrl+Space |
| Smart complete | Ctrl+Shift+Space | Ctrl+Shift+Space |
| Quick fix / Intention | Alt+Enter | Opt+Enter |
| Reformat code | Ctrl+Alt+L | Cmd+Opt+L |
| Optimize imports | Ctrl+Alt+O | Ctrl+Opt+O |
| Duplicate line | Ctrl+D | Cmd+D |
| Delete line | Ctrl+Y | Cmd+Delete |
| Move line up/down | Shift+Alt+↑/↓ | Shift+Opt+↑/↓ |
| Toggle comment | Ctrl+/ | Cmd+/ |
| Block comment | Ctrl+Shift+/ | Cmd+Shift+/ |
| Surround with | Ctrl+Alt+T | Cmd+Opt+T |
| Unwrap | Ctrl+Shift+Delete | Cmd+Shift+Delete |
| Select word | Ctrl+W (extend) | Opt+↑ |
| Multi-cursor | Alt+Click | Opt+Click |
| Column selection | Alt+Shift+Insert | Opt+Shift+Insert |

### Refactoring
| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Rename | Shift+F6 | Shift+F6 |
| Extract Method | Ctrl+Alt+M | Cmd+Opt+M |
| Extract Variable | Ctrl+Alt+V | Cmd+Opt+V |
| Inline | Ctrl+Alt+N | Cmd+Opt+N |
| Refactor This | Ctrl+Alt+Shift+T | Ctrl+T |
| Safe Delete | Alt+Delete | Cmd+Delete |

### Build & Run
| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Run | Shift+F10 | Ctrl+R |
| Debug | Shift+F9 | Ctrl+D |
| Run with Coverage | — | — |
| Apply Changes (hot reload) | Ctrl+F10 | Ctrl+Cmd+F10 |
| Stop | Ctrl+F2 | Cmd+F2 |
| Build | Ctrl+F9 | Cmd+F9 |
| Rebuild | — | — |

### VCS
| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Commit | Ctrl+K | Cmd+K |
| Push | Ctrl+Shift+K | Cmd+Shift+K |
| Update Project | Ctrl+T | Cmd+T |
| VCS Operations | Alt+` | Ctrl+V |

---

## New UI vs Classic UI

Android Studio introduced a **New UI** (default from Hedgehog 2023.1+):
- Compact toolbar, icons moved to corners
- Tool window bars replaced by floating buttons
- Settings: **View > Appearance > New UI** (toggle)

**New UI differences:**
- Main menu collapses into hamburger (Windows/Linux)
- Run widget combined: device + config selector inline
- Gemini icon in toolbar
- Right-side panel: Gemini chat, Structure

---

## IDE Configuration

### Settings / Preferences
**File > Settings** (Win/Linux) | **Android Studio > Preferences** (Mac)
Shortcut: `Ctrl+Alt+S` / `Cmd+,`

Key settings paths:
```
Appearance & Behavior
  └── Appearance → Theme (Light/Dark/High Contrast)
  └── System Settings → Memory settings, project open behavior

Keymap → customize all shortcuts; import/export keymaps

Editor
  └── General → Auto-import, strip trailing whitespace
  └── Code Style → per-language formatting rules
  └── Inspections → enable/disable lint & IntelliJ inspections
  └── File and Code Templates → modify file templates
  └── Live Templates → code snippets (type shortcut + Tab)

Build, Execution, Deployment
  └── Gradle → Gradle JDK, build tool version, offline mode
  └── Compiler → parallel compilation, VM options

Tools
  └── Google Accounts → manage signed-in accounts
  └── External Tools → configure CLI tools
  └── Emulator → launch in separate window, snapshot settings
```

### Memory Settings
**Help > Change Memory Settings**
- Default heap: 1280 MB
- Recommended for large projects: 2048–4096 MB
- Requires IDE restart

### Proxy Settings
**File > Settings > Appearance & Behavior > System Settings > HTTP Proxy**
- Manual or auto-detect proxy
- Set for Gradle separately in `~/.gradle/gradle.properties`:
  ```
  systemProp.http.proxyHost=proxy.company.com
  systemProp.http.proxyPort=8080
  systemProp.https.proxyHost=proxy.company.com
  systemProp.https.proxyPort=8080
  ```

### Plugins
**File > Settings > Plugins**
- Marketplace tab: search, install (requires restart)
- Installed tab: enable/disable/uninstall
- Key official plugins: Kotlin, Android, Jetpack Compose, Firebase, Gemini

---

## Live Templates (Code Snippets)

Type abbreviation in editor → press **Tab** to expand.

| Abbreviation | Expands To |
|-------------|-----------|
| `fun` | function declaration |
| `main` | main() function |
| `sout` | println() |
| `fori` | for loop with index |
| `comp` | Composable function scaffold |
| `toast` | Toast.makeText() |
| `logd` | Log.d() |
| `loge` | Log.e() |
| `newInstance` | Fragment newInstance() pattern |

Add custom: **Settings > Editor > Live Templates** → "+" → define abbreviation + template text.

---

## VCS Integration

**Git setup:** Android Studio auto-detects `.git` in project root.
- **Enable VCS:** VCS > Enable Version Control Integration
- **Clone:** File > New > Project from Version Control

**Git tool window** (`Alt+9`):
- **Log tab** — commit history, branch graph, file diff
- **Console tab** — raw Git output
- **Local Changes** — unstaged/staged changes

**Gutter indicators:**
- 🟦 Blue line = modified line
- 🟩 Green line = added line
- ▶ Red triangle = deleted lines

**Commit workflow:** `Ctrl+K` → review changes → write message → Commit or Commit & Push.

---

## Useful IDE Actions (Search Everywhere)

Double-tap Shift → type any of these:
- `Sync Project` — Gradle sync
- `Invalidate Caches` — fix IDE index issues
- `Analyze APK` — open APK analyzer
- `Memory Settings` — heap config
- `Theme` — switch color theme
- `Font` — editor font settings
- `Color Scheme` — syntax highlighting
- `New UI` — toggle new/classic UI
