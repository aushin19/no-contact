---
name: android-studio-ide
description: >
  Complete Android Studio IDE usage guide — installation, UI navigation, project structure,
  Gradle builds, code writing, emulator/device setup, debugging, testing, profiling, and
  publishing to Google Play. Use this skill whenever the user asks about Android Studio setup,
  IDE configuration, emulator issues, Gradle errors, debugging in Android Studio, running apps,
  signing APKs/AABs, Logcat, Layout Inspector, Android Profiler, Gemini in Android Studio,
  AVD manager, keyboard shortcuts, build variants, lint, annotations, or any Android IDE
  workflow question. Trigger even for casual phrasings like "how do I run my app", "why is
  my build failing", "how to create AVD", "Android Studio won't launch", or "how to sign my APK".
---

# Android Studio IDE — Complete Usage Skill

**Current stable release:** Android Studio Panda 4 (2025)
**Based on:** IntelliJ IDEA platform
**Official docs:** https://developer.android.com/studio/intro

---

## Quick Reference — Which Reference File to Read

| Task | Reference File |
|------|---------------|
| IDE layout, panels, shortcuts, settings | `references/01-ui-navigation.md` |
| Project structure, Gradle, build variants, deps | `references/02-project-gradle.md` |
| Writing code, templates, Compose, Lint, resources | `references/03-write-code.md` |
| Running app, AVD/emulator, physical device, debug | `references/04-run-debug.md` |
| Unit/UI/instrumented tests, coverage | `references/05-test.md` |
| Profiler, memory, CPU, network analysis | `references/06-profile-performance.md` |
| Signing, versioning, AAB, Play upload | `references/07-publish.md` |
| Gemini AI assistant, code suggestions, AI features | `references/08-gemini-ai.md` |

**Always read the relevant reference file(s) before answering.** Multiple files may apply.

---

## Android Studio — Core Concepts (Always in Context)

### What It Is
Official IDE for Android development. Built on IntelliJ IDEA. Supports Kotlin (primary), Java, C++/NDK. Handles the full dev lifecycle: write → build → run → debug → test → profile → publish.

### Installation Requirements
- **Windows:** 64-bit Windows 8/10/11; 8 GB RAM min (16 GB rec); 8 GB disk; 1280×800 screen
- **macOS:** macOS 10.14+; Apple Silicon or Intel; 8 GB RAM min
- **Linux:** 64-bit; glibc 2.31+; KDE or GNOME; 8 GB RAM min
- **Download:** https://developer.android.com/studio

### Key Mental Model: The Dev Loop
```
Create Project → Write Code → Build (Gradle) → Run/Debug (Emulator/Device)
     ↑                                                        |
     └──────────── Test → Profile → Fix → Publish ───────────┘
```

---

## Workflow Basics

### Starting a New Project
1. **File > New > New Project**
2. Choose template (Empty Activity, Basic Views Activity, etc.)
3. Set: Name, Package name, Save location, Language (Kotlin/Java), Min SDK
4. Click **Finish** → Gradle syncs automatically

### Opening Existing Project
- **File > Open** → select root directory containing `build.gradle` / `build.gradle.kts`
- Or: **File > New > Import Project** for Eclipse/Maven projects

### Key First-Time Setup
- Install SDK: **SDK Manager** (toolbar icon or Tools > SDK Manager)
- Create emulator: **AVD Manager** (toolbar icon or Tools > Device Manager)
- Sign in: Profile icon in toolbar → enables Gemini, Firebase, App Quality Insights

---

## Common Quick Answers (No Reference File Needed)

**Sync Gradle:** File > Sync Project with Gradle Files (or elephant icon in toolbar)

**Invalidate Caches:** File > Invalidate Caches / Restart → fixes most IDE weirdness

**Find anything:** Double-tap **Shift** → Search Everywhere

**Run app:** **Shift+F10** (Win/Linux) / **Ctrl+R** (Mac)

**Debug app:** **Shift+F9** (Win/Linux) / **Ctrl+D** (Mac)

**Build APK:** Build > Build Bundle(s) / APK(s) > Build APK(s)

**Logcat:** View > Tool Windows > Logcat (or **Alt+6**)

**Terminal:** View > Tool Windows > Terminal (or **Alt+F12**)

---

## Troubleshooting — First Steps

1. **Gradle sync failure** → check internet, proxy settings, `build.gradle` syntax → read `references/02-project-gradle.md`
2. **Emulator won't start** → check HAXM/KVM, RAM allocation, API level → read `references/04-run-debug.md`
3. **App crashes on launch** → check Logcat for stack trace → read `references/04-run-debug.md`
4. **Slow IDE** → increase heap: Help > Change Memory Settings → read `references/01-ui-navigation.md`
5. **Can't find feature** → Double-tap Shift → type feature name

---

## Reference Files Index

- `references/01-ui-navigation.md` — Full UI anatomy, tool windows, editor tabs, shortcuts table, IDE config
- `references/02-project-gradle.md` — Project structure, Gradle DSL, build variants, flavors, deps, NDK
- `references/03-write-code.md` — Code editor, templates, Compose tooling, Layout Editor, Lint, annotations, resources
- `references/04-run-debug.md` — AVD Manager, Emulator controls, physical device setup, Debugger, Logcat, ADB
- `references/05-test.md` — Test types, test runner, Espresso, managed devices, coverage, CI
- `references/06-profile-performance.md` — Android Profiler, CPU/Memory/Network profilers, heap dumps, traces
- `references/07-publish.md` — App signing, build types, ProGuard/R8, AAB, version codes, Play upload
- `references/08-gemini-ai.md` — Gemini in Android Studio, AI code completion, chat, transform, agent mode
