---
name: android-mobile-design
description: >
  Comprehensive Android Mobile UI Design guide based on official Android developer documentation
  (developer.android.com/design/ui/mobile). Use this skill whenever a user asks about Android UI
  design, Material Design 3, mobile app layouts, Android components, design patterns, system bars,
  edge-to-edge, predictive back, theming, color, typography, accessibility, notifications, widgets,
  or any Android-specific design decision. Also trigger for: "how should I design X for Android",
  "what's the Android pattern for X", "Material 3 guidelines", "Android design review", "should I
  use bottom nav or nav drawer", "how to handle safe areas on Android", "Android design system",
  "Compose UI best practices", "adaptive layout Android", or any question about designing a
  beautiful, usable Android app. Always use this skill before answering Android design questions —
  it contains official Google guidance that supersedes general knowledge.
---

# Android Mobile UI Design Skill

Official source: https://developer.android.com/design/ui/mobile  
Design system: Material Design 3 → https://m3.material.io  
Figma kit: https://goo.gle/android-ui-kit  
Theme builder: https://m3.material.io/theme-builder#/dynamic

---

## How to Use This Skill

1. **Identify the domain** from the table below
2. **Load the relevant reference file** using `view`
3. **Apply guidance** with concrete recommendations, code patterns, and do/don't examples
4. **Cross-reference** adjacent domains when questions span multiple areas

| User Topic | Reference File |
|---|---|
| Accessibility, system bars, glossary, translation | `references/foundations.md` |
| Color, themes, typography, motion | `references/styles.md` |
| Layouts, grids, edge-to-edge, images, adaptive | `references/layout.md` |
| Buttons, cards, dialogs, FAB, nav components | `references/components.md` |
| Navigation, predictive back, sharing, settings | `references/patterns.md` |
| Notifications, app widgets, home screen | `references/home-screen.md` |

---

## Documentation Map

All guides live under: `https://developer.android.com/design/ui/mobile/guides/`

### Foundations
- Accessibility → `/foundations/accessibility`
- System Bars → `/foundations/system-bars`
- Glossary → `/foundations/glossary`
- Platform Translation → `/foundations/translate-designs`

### Styles
- Color → `/styles/color`
- Themes → `/styles/themes`

### Layout & Content
- Layout Basics → `/layout-and-content/layout-basics`
- Parts of an App → `/layout-and-content/app-anatomy`
- Grids & Units → `/layout-and-content/grids-and-units`
- Content Structure → `/layout-and-content/content-structure`
- Layout & Nav Patterns → `/layout-and-content/layout-and-nav-patterns`
- Canonical Layouts → `/layout-and-content/canonical-layouts`
- Custom Layouts → `/layout-and-content/custom-layouts`
- Adapt Your Layout → `/layout-and-content/adapt-layout`
- Immersive Content → `/layout-and-content/immersive-content`
- Edge-to-Edge Design → `/layout-and-content/edge-to-edge`
- Images & Graphics → `/layout-and-content/images-graphics`

### Components
- Material Components Overview → `/components/material-overview`

### Patterns
- Predictive Back → `/patterns/predictive-back`
- Navigation → `/patterns/navigation`
- Sharing → `/patterns/sharing`
- Settings → `/patterns/settings`

### Home Screen
- Notifications → `/home-screen/notifications`
- App Widgets → `/home-screen/app-widgets`

---

## Quick Decision Trees

### Navigation Pattern Selection
```
Does content have 3–5 top-level destinations?
  YES → Navigation Bar (bottom) — preferred for mobile
  NO  → Is it 1-2 destinations?
          YES → Tabs or simple top nav
          NO  → Is it 6+ with hierarchy?
                  YES → Navigation Drawer (modal or permanent)
                  NO  → Nested navigation with Back stack
```

### Layout Pattern Selection
```
Compact screen (< 600dp)?
  → Single-pane layout
Medium screen (600–840dp)?
  → Consider list-detail or supporting pane
Expanded screen (> 840dp)?
  → Two-pane / canonical layout (list-detail, feed+detail, etc.)
```

### Color System Selection
```
Does app need brand expression?
  YES → Seed color → Material Theme Builder → dynamic color roles
  NO  → Baseline Material 3 color scheme

Dark mode needed?
  → Always. Use tonal surface elevation, not shadow alone.
```

---

## Core Principles (Always Apply)

1. **Edge-to-edge first** — Apps target API 35+ must be edge-to-edge. Handle insets explicitly.
2. **Material Design 3** — Use MD3, not MD2. Dynamic color, updated components, expressive type.
3. **Adaptive by default** — Design for compact, medium, expanded window sizes from day one.
4. **Predictive back** — Support `OnBackPressedCallback` and custom animations for Android 13+.
5. **Accessibility** — Minimum 48×48dp touch targets, content descriptions, sufficient contrast (4.5:1 text).
6. **System bar integration** — Status bar and nav bar must use `WindowInsetsController` for proper theming.

---

## Key Tools & Resources

| Tool | URL | Purpose |
|---|---|---|
| Android UI Kit (Figma) | https://goo.gle/android-ui-kit | Design mockups |
| Material Theme Builder | https://m3.material.io/theme-builder | Color scheme generation |
| Android Design Gallery | https://developer.android.com/large-screens/gallery | Inspiration & templates |
| Android Figma Community | https://www.figma.com/@androiddesign | Latest kits & labs |
| Material 3 Website | https://m3.material.io | Full component specs |
| Window Size Classes | https://developer.android.com/develop/ui/compose/layouts/adaptive/window-size-classes | Adaptive breakpoints |

---

## When to Load Additional Reference Files

- Answering a specific component question → always load `references/components.md`
- Reviewing a layout design → load `references/layout.md`
- Discussing theming or brand → load `references/styles.md`
- Checking accessibility compliance → load `references/foundations.md`
- Navigation or back-stack question → load `references/patterns.md`
- Push notifications or widget → load `references/home-screen.md`
