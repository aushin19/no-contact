# Android Mobile Design — Foundations Reference

Source: https://developer.android.com/design/ui/mobile/guides/foundations/

---

## Table of Contents
1. [Accessibility](#1-accessibility)
2. [System Bars](#2-system-bars)
3. [Glossary](#3-glossary)
4. [Platform Translation](#4-platform-translation)

---

## 1. Accessibility

Source: https://developer.android.com/design/ui/mobile/guides/foundations/accessibility

Designing for accessibility benefits everyone — 15%+ of the global population has a disability. Android's platform and Jetpack libraries provide extensive hooks; designers and developers share responsibility.

### Touch Targets
- **Minimum:** 48×48dp for any interactive element
- **Spacing:** At least 8dp between adjacent targets
- Small visual elements (icons, toggle nubs) can be smaller visually if the touch target padding is added via `Modifier.minimumInteractiveComponentSize()` in Compose or `minHeight`/`minWidth` in Views

### Color & Contrast
| Context | Minimum Ratio |
|---|---|
| Normal text (< 18sp regular / < 14sp bold) | 4.5 : 1 |
| Large text (≥ 18sp regular / ≥ 14sp bold) | 3 : 1 |
| UI components & graphical objects | 3 : 1 |
| Decorative / disabled | No requirement |

- Never rely on color alone to convey information — add icons, patterns, or labels
- Test with Material 3 dynamic color in both light and dark; auto-generated tonal palettes are WCAG-compliant by design

### Screen Reader (TalkBack) Support
- Every interactive element needs a meaningful `contentDescription`
- Group related elements with `Modifier.semantics(mergeDescendants = true)`
- Use `Role` in Compose semantics (`Role.Button`, `Role.Checkbox`, etc.)
- Avoid redundant descriptions — e.g. don't say "button" in the content description if Role.Button is set
- Custom actions: expose via `customActions` in semantics for gestures that TalkBack can't replicate
- Live regions: use `liveRegion = LiveRegionMode.Polite` for dynamic content updates

### Focus & Keyboard Navigation
- Tab order should match reading order (top-to-bottom, LTR/RTL aware)
- Override focus traversal with `Modifier.focusProperties { next = ... }` when default is wrong
- All dialogs and bottom sheets must trap focus while open; return focus on dismiss
- Avoid focus traps that prevent escape

### Text Sizing & Display
- Support system font scale (up to 200% on Android 13+) — use `sp` units, never `dp` for text
- Allow content to reflow; avoid truncation at large font sizes
- Test with "Large Text" accessibility setting

### Motion & Animation
- Respect `prefers-reduced-motion` via `LocalAccessibilityManager.current.isReduceMotionEnabled`
- Provide non-animated alternatives or shorten durations significantly

### Semantic Structure
- Use headings: `Modifier.semantics { heading() }` for section titles
- Provide state descriptions: `Modifier.semantics { stateDescription = "Checked" }`
- Error messages: associate with the field using `Modifier.semantics { error("Required") }`

### Compose Accessibility API Reference
```kotlin
// Touch target
Modifier.minimumInteractiveComponentSize()

// Content description
Modifier.semantics { contentDescription = "Close dialog" }

// Heading
Modifier.semantics { heading() }

// Merge children
Modifier.semantics(mergeDescendants = true) { }

// Custom action
Modifier.semantics {
    customActions = listOf(
        CustomAccessibilityAction("Delete item") { onDelete(); true }
    )
}
```

---

## 2. System Bars

Source: https://developer.android.com/design/ui/mobile/guides/foundations/system-bars

System bars = **status bar** (top) + **navigation bar** (bottom). As of Android 15 / targetSdk 35, all apps are edge-to-edge by default — app content draws behind both bars.

### Edge-to-Edge Fundamentals
```kotlin
// In Activity.onCreate() — required for API < 35 opt-in
enableEdgeToEdge()   // androidx.activity:activity:1.8+
```
- Content fills the full screen including behind system bars
- System bars are transparent (or translucent) by default
- App is responsible for handling **window insets** so content isn't obscured

### Window Insets Handling

| Inset Type | When to Use |
|---|---|
| `WindowInsets.safeDrawing` | Anything that must not be clipped (background, full-bleed images) |
| `WindowInsets.safeContent` | Interactive content that must not be obscured |
| `WindowInsets.systemBars` | Status + navigation bars combined |
| `WindowInsets.statusBars` | Status bar only |
| `WindowInsets.navigationBars` | Navigation bar (gesture or button) |
| `WindowInsets.ime` | Software keyboard |
| `WindowInsets.displayCutout` | Camera cutouts / notches |

```kotlin
// Compose — apply padding for safe content
Box(
    Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeContent)
) { ... }

// Apply only bottom inset (e.g. bottom nav bar)
NavigationBar(
    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
)

// Consume insets so children don't double-apply
Modifier.consumeWindowInsets(WindowInsets.navigationBars)
```

### Status Bar
- Transparent on Android 6+ with light/dark icon tint
- Control icon tint via `WindowInsetsController`:
```kotlin
ViewCompat.getWindowInsetsController(window.decorView)?.apply {
    isAppearanceLightStatusBars = true   // dark icons on light bg
}
```
- In Compose: `systemBarsDarkContentEnabled` on `SystemUiController` (Accompanist) or direct `WindowInsetsControllerCompat`

### Navigation Bar
- Three modes: **gesture** (swipe), **2-button** (back + home), **3-button** (back/home/recents)
- Cannot be programmatically forced — user controls this
- Gesture nav: nav bar is a thin strip (~20dp); avoid placing interactive content in bottom 20dp without inset padding
- Button nav: nav bar ~48dp tall
- Always handle both via insets, not hardcoded values

### Scrim / Protection
- For light content behind a transparent nav bar, add a gradient scrim
- Material 3 `NavigationBar` component auto-handles inset padding
- `BottomAppBar` + `FloatingActionButton` combo auto-adjusts with `Scaffold` in Compose

### Display Cutouts (Notches)
```kotlin
// Allow content to render in cutout area
window.attributes.layoutInDisplayCutoutMode =
    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
// Then guard interactive content with displayCutout insets
```

### Immersive Mode
- **Lean back:** Auto-hide bars on touch; use for video playback
- **Immersive:** Bars hide, swipe to reveal temporarily; use for games/reading
- **Sticky immersive:** Swipe shows bars semi-transparent, auto-hides again; max immersion

```kotlin
WindowInsetsControllerCompat(window, view).apply {
    hide(WindowInsetsCompat.Type.systemBars())
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}
```

---

## 3. Glossary

Source: https://developer.android.com/design/ui/mobile/guides/foundations/glossary

### Layout & Structure Terms
| Term | Definition |
|---|---|
| **Window size class** | Breakpoint category: Compact (<600dp), Medium (600–840dp), Expanded (>840dp) |
| **Canonical layout** | Predefined adaptive layout pattern: list-detail, feed, supporting pane |
| **Pane** | A distinct content region within a layout; multi-pane = multiple simultaneous panes |
| **Safe area** | Region of screen free from system UI occlusion |
| **Insets** | Padding values describing where system UI intrudes into app space |
| **Edge-to-edge** | Content draws behind system bars; app manages insets explicitly |
| **Scaffold** | Compose layout structure providing slots for app bars, FAB, nav bar, snackbar |
| **dp (density-independent pixel)** | Resolution-independent unit; 1dp = 1px at 160 dpi (mdpi) |
| **sp (scale-independent pixel)** | Like dp but also scales with user font size preference |

### Component Terms
| Term | Definition |
|---|---|
| **FAB (Floating Action Button)** | Primary action button, floats above content |
| **Bottom sheet** | Panel that slides up from bottom edge |
| **Modal bottom sheet** | Overlays content; requires dismiss to continue |
| **Standard bottom sheet** | Coexists with content; can be partially visible |
| **Snackbar** | Brief message with optional action; auto-dismisses |
| **Chip** | Compact, interactive element for filters/tags/actions |
| **Navigation bar** | Bottom component with 3–5 destination icons (do not confuse with system nav bar) |
| **Navigation drawer** | Side panel with full destination list |
| **Top app bar** | Header bar with title, leading nav, trailing actions |

### Design System Terms
| Term | Definition |
|---|---|
| **Material Design 3 (MD3)** | Current Android design system; replaces MD2 |
| **Dynamic color** | Algorithm that extracts colors from user's wallpaper to generate a harmonious theme |
| **Color scheme** | Full set of semantic color roles derived from seed color |
| **Tonal palette** | 13-tone gradient from each key color |
| **Color role** | Named slot (primary, secondary, surface, error, etc.) with foreground/background pairs |
| **Shape** | Corner radius system: None/Extra Small/Small/Medium/Large/Extra Large/Full |
| **Elevation** | Z-axis layering; in MD3 expressed via tonal color overlay, not shadow alone |
| **Typography scale** | Named text styles: Display, Headline, Title, Body, Label (each: Large/Medium/Small) |

### Navigation Terms
| Term | Definition |
|---|---|
| **Back stack** | LIFO stack of destinations; Back gesture pops the top |
| **Predictive back** | Android 13+ feature; preview of destination before completing back gesture |
| **Deep link** | URI that navigates directly to a specific in-app destination |
| **Task** | Unit of work comprising a back stack; apps can have multiple tasks |

---

## 4. Platform Translation

Source: https://developer.android.com/design/ui/mobile/guides/foundations/translate-designs

Translating designs from iOS, web, or other platforms to Android requires adaptation — not pixel-perfect port.

### Key Differences: Android vs iOS

| Aspect | Android | iOS |
|---|---|---|
| Navigation | Back gesture (edge swipe or button) | Swipe-right or back button |
| Bottom nav | Navigation Bar (MD3) | Tab Bar |
| Top nav | Top App Bar | Navigation Bar (top) |
| Primary action | FAB or top-right action | Bottom toolbar or top-right |
| List items | Material ListItem with dividers optional | UITableView cell style |
| Dialogs | AlertDialog — max 3 actions | UIAlertController |
| Switches | Material Switch | UISwitch |
| Date picker | Material DatePicker (calendar or input) | UIDatePicker (wheel/calendar) |
| Back indicator | System back gesture/button | Left-chevron in nav bar |

### Translation Checklist
- [ ] Replace iOS navigation controller patterns with Android back stack + NavController
- [ ] Replace UITabBar with MD3 NavigationBar (bottom) or NavigationRail (medium/expanded)
- [ ] Replace iOS sheets with MD3 bottom sheets or full-screen dialogs
- [ ] Apply MD3 color roles — do not hardcode iOS system colors
- [ ] Use Android typography scale (Display/Headline/Title/Body/Label)
- [ ] Replace iOS haptics with Android `HapticFeedbackConstants` equivalents
- [ ] Handle Android-specific gestures: predictive back, edge swipe, long press for contextual menu
- [ ] Test with TalkBack (≠ VoiceOver) — semantics API differs

### Android-Unique Patterns (No iOS Equivalent)
- **Predictive back gesture** — visual preview before completing back
- **Dynamic color** — wallpaper-extracted theming
- **App widgets** — home screen interactive panels
- **Notification actions** — inline replies and action buttons from notification shade
- **Share sheet** — OS-level Sharesheet, not app-custom
- **Picture-in-picture (PiP)** — for media/video apps
- **Bubbles** — floating conversation heads
