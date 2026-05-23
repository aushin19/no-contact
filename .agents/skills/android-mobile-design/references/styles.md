# Android Mobile Design — Styles Reference

Source: https://developer.android.com/design/ui/mobile/guides/styles/

---

## Table of Contents
1. [Color System](#1-color-system)
2. [Themes](#2-themes)
3. [Typography](#3-typography)
4. [Motion & Animation](#4-motion--animation)

---

## 1. Color System

Source: https://developer.android.com/design/ui/mobile/guides/styles/color

Material Design 3 uses a tonal color system built from **key colors** → **tonal palettes** → **color roles** → **color scheme**.

### Key Colors
| Key Color | Purpose |
|---|---|
| **Primary** | Most prominent, main brand expression |
| **Secondary** | Accent, less prominent UI elements |
| **Tertiary** | Contrasting accent for balance/variety |
| **Neutral** | Surfaces, backgrounds, text |
| **Neutral Variant** | Subtle surfaces and outlines |
| **Error** | Error states |

### Tonal Palettes
Each key color generates a 13-tone palette from 0 (black) to 100 (white). These tones become color roles.

### Color Roles (Complete List)
| Role | Light | Dark | Use |
|---|---|---|---|
| Primary | tone 40 | tone 80 | Key components, active states |
| On Primary | tone 100 | tone 20 | Text/icons on primary |
| Primary Container | tone 90 | tone 30 | Less prominent primary fills |
| On Primary Container | tone 10 | tone 90 | Text/icons on primary container |
| Secondary | tone 40 | tone 80 | Less prominent components |
| On Secondary | tone 100 | tone 20 | |
| Secondary Container | tone 90 | tone 30 | |
| On Secondary Container | tone 10 | tone 90 | |
| Tertiary | tone 40 | tone 80 | Contrasting accent |
| On Tertiary | tone 100 | tone 20 | |
| Tertiary Container | tone 90 | tone 30 | |
| On Tertiary Container | tone 10 | tone 90 | |
| Error | tone 40 | tone 80 | Error states |
| On Error | tone 100 | tone 20 | |
| Error Container | tone 90 | tone 30 | |
| On Error Container | tone 10 | tone 90 | |
| Surface | tone 98 | tone 6 | Default background |
| On Surface | tone 10 | tone 90 | Primary text on surface |
| Surface Variant | tone 90 | tone 30 | Alternative surface fills |
| On Surface Variant | tone 30 | tone 80 | Secondary text on surface |
| Surface Container Lowest | tone 100 | tone 4 | Lowest card/sheet surface |
| Surface Container Low | tone 96 | tone 10 | |
| Surface Container | tone 94 | tone 12 | Default card/sheet surface |
| Surface Container High | tone 92 | tone 17 | |
| Surface Container Highest | tone 90 | tone 22 | Highest card/sheet surface |
| Outline | tone 50 | tone 60 | Borders, dividers |
| Outline Variant | tone 80 | tone 30 | Subtle borders |
| Inverse Surface | tone 20 | tone 90 | Tooltip, snackbar bg |
| Inverse On Surface | tone 95 | tone 20 | Text on inverse surface |
| Inverse Primary | tone 80 | tone 40 | Action on inverse surface |
| Shadow | tone 0 | tone 0 | Drop shadows |
| Scrim | tone 0 | tone 0 | Modal overlay |

### Dynamic Color (Android 12+)
```kotlin
// In your theme — auto-applies wallpaper-based colors
val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
val colorScheme = when {
    dynamicColor && darkTheme -> dynamicDarkColorScheme(context)
    dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
    darkTheme -> DarkColorScheme   // your fallback
    else -> LightColorScheme       // your fallback
}
MaterialTheme(colorScheme = colorScheme) { ... }
```

### Color Usage Rules
- **DO:** Use semantic roles — never reference tonal palette tones directly in components
- **DO:** Pair "On X" color with "X" color (Primary + On Primary)
- **DO:** Maintain 4.5:1 contrast for text, 3:1 for non-text UI
- **DON'T:** Mix dark-theme colors into light theme
- **DON'T:** Hardcode hex values in components — always use theme color roles
- **DON'T:** Use semitransparent overlays for elevation — use surface container tones instead

### Custom Seed Color Generation
```
1. Pick seed color (brand hex)
2. Feed into Material Theme Builder: https://m3.material.io/theme-builder
3. Export → XML (Views) or Compose token file
4. Integrate into app theme
```

---

## 2. Themes

Source: https://developer.android.com/design/ui/mobile/guides/styles/themes

### Material Theme Structure
A complete MD3 theme = Color Scheme + Typography + Shapes

```kotlin
// Compose
MaterialTheme(
    colorScheme = appColorScheme,
    typography = appTypography,
    shapes = appShapes
) { content() }
```

```xml
<!-- Views — res/values/themes.xml -->
<style name="Theme.App" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/md_theme_light_primary</item>
    <!-- ... all color roles ... -->
    <item name="textAppearanceBodyLarge">@style/TextAppearance.App.BodyLarge</item>
    <item name="shapeAppearanceMediumComponent">@style/ShapeAppearance.App.Medium</item>
</style>
```

### Dark Theme
```kotlin
// Compose — detect system dark mode
val darkTheme = isSystemInDarkTheme()
MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors) { }
```
```xml
<!-- Views — res/values-night/themes.xml -->
<style name="Theme.App" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/md_theme_dark_primary</item>
    ...
</style>
```

### Shape Scale
| Token | Corner Radius | Common Uses |
|---|---|---|
| None | 0dp | Tiles that bleed to edge |
| Extra Small | 4dp | Chips, small buttons |
| Small | 8dp | Cards, menus |
| Medium | 12dp | FABs, dialogs |
| Large | 16dp | Bottom sheets, nav drawer |
| Extra Large | 28dp | Extended FABs |
| Full | 50% (circle) | Avatar, icon buttons |

```kotlin
// Compose shapes
MaterialTheme.shapes.medium   // 12dp corner radius
MaterialTheme.shapes.large    // 16dp corner radius
```

### Elevation in MD3
Elevation is expressed via **tonal color overlay** (surface container tones), not drop shadow.
- Level 0 → Surface (base)
- Level 1 → Surface Container Low
- Level 2 → Surface Container
- Level 3 → Surface Container High
- Level 4/5 → Surface Container Highest

Drop shadows remain for components that need physical separation (FAB, elevated cards).

---

## 3. Typography

### Material 3 Type Scale
| Role | Size | Weight | Line Height | Use |
|---|---|---|---|---|
| Display Large | 57sp | Regular (400) | 64sp | Hero text, large screen |
| Display Medium | 45sp | Regular | 52sp | |
| Display Small | 36sp | Regular | 44sp | |
| Headline Large | 32sp | Regular | 40sp | Article titles |
| Headline Medium | 28sp | Regular | 36sp | |
| Headline Small | 24sp | Regular | 32sp | |
| Title Large | 22sp | Regular | 28sp | App bar titles |
| Title Medium | 16sp | Medium (500) | 24sp | Section headings |
| Title Small | 14sp | Medium | 20sp | |
| Body Large | 16sp | Regular | 24sp | Primary readable text |
| Body Medium | 14sp | Regular | 20sp | Secondary readable text |
| Body Small | 12sp | Regular | 16sp | Captions |
| Label Large | 14sp | Medium | 20sp | Button labels |
| Label Medium | 12sp | Medium | 16sp | Badge, chip labels |
| Label Small | 11sp | Medium | 16sp | Tiny annotations |

```kotlin
// Compose — usage
Text(text = "Hello", style = MaterialTheme.typography.bodyLarge)
Text(text = "Title", style = MaterialTheme.typography.titleMedium)
```

### Typography Rules
- **Always use `sp` units** — never `dp` for text size (breaks font scaling accessibility)
- **Limit typefaces:** max 2 font families per app (brand + UI)
- **Avoid all-caps for body text** — use for labels/overlines only
- **Line length:** 40–60 characters for comfortable reading in body text
- **Line height:** MD3 scale already defines optimal values; don't override without reason

### Custom Fonts
```kotlin
// Compose — with downloadable font
val myFont = FontFamily(
    Font(R.font.my_font_regular),
    Font(R.font.my_font_bold, FontWeight.Bold)
)
val typography = Typography(
    bodyLarge = TextStyle(fontFamily = myFont, fontSize = 16.sp, lineHeight = 24.sp)
)
```

---

## 4. Motion & Animation

### Principles
- **Purpose:** Motion should communicate meaning, not decorate
- **Choreography:** Related elements move together (shared axis, container transform)
- **Speed:** Fast in, slow out for entrances (ease-in-out); immediate for responses to direct manipulation
- **Avoid:** Gratuitous animation that delays user tasks

### Material Motion Patterns

| Pattern | When to Use | Example |
|---|---|---|
| **Container Transform** | Expanding from a specific element | Card → Detail screen |
| **Shared Axis** | Related screens with spatial relationship | Onboarding steps, tabs |
| **Fade Through** | Unrelated content swaps | Bottom nav destination change |
| **Fade** | Elements enter/exit same area | Dialog appear/disappear |

### Duration Guidelines
| Interaction Type | Duration |
|---|---|
| Simple fade/appear | 100–150ms |
| Small component transitions | 200–250ms |
| Screen-level transitions | 300–500ms |
| Complex multi-step motion | 500–700ms |

### Compose Animation APIs
```kotlin
// Animated visibility
AnimatedVisibility(visible = show) {
    Text("Appears with fade")
}

// Animated content swap (Fade Through)
AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
        fadeIn(tween(220)) togetherWith fadeOut(tween(90))
    }
) { screen -> ScreenComposable(screen) }

// Spring physics (natural feel)
val offset by animateDpAsState(
    targetValue = if (expanded) 0.dp else 100.dp,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
)

// Shared element transition (Compose 1.7+)
SharedTransitionLayout {
    AnimatedContent(...) {
        // sharedElement modifier connects elements across compositions
    }
}
```

### Reduced Motion
```kotlin
val reduceMotion = LocalAccessibilityManager.current
    ?.isReduceMotionEnabled ?: false

val duration = if (reduceMotion) 0 else 300
```

### Predictive Back Animation
Android 13+ — back gesture shows preview; apps can provide custom animation:
```kotlin
// AndroidX Activity 1.8+
override fun onCreate(...) {
    onBackPressedDispatcher.addCallback(this) {
        // custom back animation handled here
    }
    // Or use predictiveBackProgress via OnBackPressedCallback API
}
```
