# Android Mobile Design System

## Overview

NoContact uses a Pinterest-inspired Android design system translated from `.raw/DESIGN.md` and shaped by the product requirements in `.raw/PRD.md`. The visual language is warm, quiet, and direct: cream surfaces carry the interface, Pinterest red is reserved for primary commitment moments, and obsidian dark surfaces create a focused safe-space tone for SOS and dark mode.

The app is offline, private, and emotionally sensitive. UI should feel steady and premium rather than playful or clinical. The core loop is simple: open the app, see the no-contact streak, handle an urge through SOS, log mood or journal, and leave with a clear next step.

Dynamic color is disabled by default. Brand consistency matters more than wallpaper adaptation for this product, because the red/cream/obsidian palette is part of the emotional contract and relapse-intervention affordance.

## Colors

Source tokens come from `.raw/DESIGN.md`; PRD screen context comes from `.raw/PRD.md`.

| Source token | Hex | Android role | Use |
|---|---:|---|---|
| Pinterest Red | `#E60023` | `primary`, `error` when destructive | Primary CTA, active navigation, SOS emphasis, reset/destructive action |
| Red Pressed | `#CC001F` | pressed primary, dark inverse primary | Pressed CTA, stronger urgent state |
| Soft Red | `#FFE0E5` | `primaryContainer`, `errorContainer` | Selected chips, gentle alert surfaces, SOS support |
| Canvas | `#FFFFFF` | `surfaceContainerLowest` | Sheets, dialogs, high-clarity content panels |
| Soft Surface | `#FBFBF9` | `background`, `surface`, `surfaceContainerLow` | App background and large screen surfaces |
| Surface Card | `#F6F6F3` | `surfaceVariant`, `surfaceContainer` | Cards, chips, input fills, bottom nav |
| Secondary BG | `#E5E5E0` | `surfaceContainerHigh` | Pressed neutral states, section separation |
| Hairline | `#DADAD3` | `outlineVariant` | Dividers, subtle card borders |
| Ink Soft | `#211922` | `onSurface`, `onBackground` | Primary text and icons |
| Body | `#33332E` | strong body text | Paragraphs, journal copy |
| Mute | `#62625B` | `onSurfaceVariant` | Metadata, helper copy, inactive navigation |
| Ash | `#91918C` | `outline`, disabled text | Field outlines, disabled controls |
| Obsidian | `#262622` | `inverseSurface`, dark surface | SOS deep surface, snackbar, dark cards |

### Material 3 Role Policy

- `primary`: `#E60023`; use only for highest-emphasis actions and active destination indicators.
- `primaryContainer`: `#FFE0E5`; use for selected states that need warmth without alarm.
- `secondary`: warm charcoal `#33332E`; use for medium-emphasis controls, never cool-hued accents.
- `secondaryContainer`: `#E5E5E0`; use for neutral filled controls.
- `surface` and `background`: `#FBFBF9`; the app should read as warm cream, not white dashboard chrome.
- `surfaceContainer*`: step through `#FFFFFF`, `#FBFBF9`, `#F6F6F3`, `#E5E5E0`, and `#DADAD3`.
- `outline` and `outlineVariant`: `#91918C` and `#DADAD3`; prefer hairline separation over shadows.
- `error`: use Pinterest red, paired with explicit copy so destructive states are not color-only.
- `inverseSurface`: obsidian `#262622`; use for SOS, snackbars, and high-focus dark surfaces.

### Dark Mode

Dark mode is not pure black. Use warm obsidian tones:

| Role | Hex | Use |
|---|---:|---|
| `background` | `#191814` | App root |
| `surfaceContainerLowest` | `#14130F` | Lowest dark layer |
| `surfaceContainerLow` | `#1E1D18` | Main dark panels |
| `surfaceContainer` | `#262622` | Cards and bottom nav |
| `surfaceContainerHigh` | `#302F2A` | Raised controls |
| `surfaceContainerHighest` | `#3A3933` | Dialogs and active neutral surfaces |
| `onSurface` | `#F4F0EA` | Primary dark text |
| `onSurfaceVariant` | `#C9C3B8` | Secondary dark text |

## Typography

Use the app's Material 3 type scale with a Pin Sans-inspired posture. If Pin Sans is unavailable, use the existing Android font stack or Inter/Roboto-like metrics.

- Streak count: `displayMedium`, bold weight, red primary.
- Onboarding headings: `headlineMedium`, concise and direct.
- SOS headings: `headlineSmall`, centered only for breathing and affirmation steps.
- Card titles and section labels: `titleMedium` or `titleSmall`.
- Journal and affirmation body: `bodyLarge` with generous line height.
- Metadata, timestamps, and helper copy: `labelSmall` or `bodySmall`.

Do not use negative letter spacing in Android Compose text. Preserve accessibility font scaling with `sp` and allow text to wrap before shrinking.

## Layout

Use an 8dp grid with 4dp sub-grid adjustments for compact surfaces.

- Compact width: 16dp screen padding, single column content, bottom navigation.
- Medium width: 20dp to 24dp padding, optional navigation rail when the app shell supports it.
- Expanded width: 24dp outer padding, constrain content width where single-column emotional content would become too wide.
- Major section rhythm: 16dp to 24dp inside screens, not large marketing-page spacing.
- Related controls: 8dp spacing.
- Dense inline elements: 4dp to 6dp spacing.

All screens must be edge-to-edge. Do not hardcode status bar, navigation bar, or IME heights. Use Scaffold padding and window inset APIs. Bottom sheets and SOS controls must clear gesture and three-button navigation modes.

## Elevation & Depth

The system is mostly flat. Depth comes from tonal surfaces, borders, and content hierarchy rather than shadows.

- Default cards: `surfaceContainer`, 1dp `outlineVariant`, no shadow.
- Important cards: `surfaceContainerLow` in light mode or `surfaceContainerHigh` in dark mode.
- Dialogs and sheets: tonal elevation plus scrim.
- FAB and urgent SOS actions may use Material elevation, but avoid stacked shadow-heavy cards.

## Shapes

Keep surfaces sleek and consistent:

- Small controls and chips: 8dp.
- Cards and inputs: 12dp.
- Large hero cards and sheets: 16dp.
- Extended FAB: 24dp to 28dp.
- Avatars, breathing circle, icon buttons: full circle.

Do not introduce sharp-cornered interactive elements. Do not use oversized 32dp marketing radii for routine app cards.

## Components

### App Shell

Use a 4-tab bottom NavigationBar for compact screens: Home, Journal, Milestones, Settings. Active state uses red primary sparingly; inactive icons use warm muted text. On medium and expanded screens, navigation rail is the preferred future adaptation for the same destinations.

### Onboarding

Onboarding should feel calm and private. Use filled or outlined selectable cards on cream surfaces. Selected breakup type uses `primaryContainer` with a red check or accent. Step progress uses red primary over a hairline track.

### Home

Home is the daily anchor. The streak hero should use warm cream surfaces, red only for the day count or a single primary action, and compact premium spacing. The affirmation card should feel quiet: neutral surface, ink text, optional red quote/icon accent only if it does not compete with the streak.

### SOS Flow

SOS is the critical relapse-intervention screen. Use obsidian or inverse surface for the full-screen safe-space mode, with cream text and red for the single most important action. Trigger chips can be neutral until selected. Breathing animation should be slow, meaningful, and disabled when reduced motion is enabled.

### Journal

Journal screens should emphasize readability. Use neutral filled cards with subtle borders, timestamps in muted text, and mood tags as compact chips. The composer bottom sheet should use `surfaceContainerLowest`, 16dp top radius, IME padding, and a clear Save action.

### Milestones

Milestones use a badge grid, but avoid decorative color overload. Locked badges are neutral and muted. Unlocked badges may use red accents, soft red containers, or warm success green. Progress bars use red primary on cream track.

### Settings

Settings use grouped ListItems with clear section labels. Destructive reset actions use `error`, explicit copy, and an AlertDialog with a red confirm button plus neutral cancel action.

### Notifications And Widget

Notification visuals should use the app icon and red brand accent. Widget surfaces should use system widget background when required, but internal text and accent mapping should remain red/cream/obsidian.

## Motion & Behavior

- Bottom nav changes: fade through, 200ms to 300ms.
- Onboarding steps: shared axis horizontal, 250ms to 300ms.
- Home to SOS: shared axis Z or fade/scale, 300ms to 400ms.
- Journal card to detail: container transform when available.
- Breathing circle: 4-7-8 timing, with clear phase labels.
- Reduced motion: remove nonessential transitions and keep state changes immediate.

Support predictive back for SOS, journal detail, onboarding back steps, sheets, and dialogs.

## Accessibility & System Integration

- Minimum touch target: 48x48dp, even when visual icons are 18dp to 24dp.
- Maintain at least 8dp separation between adjacent touch targets.
- Text contrast: 4.5:1 for body text, 3:1 for non-text UI.
- Do not communicate state with color alone; selected, error, locked, and milestone states need icons, labels, or shape changes.
- Provide content descriptions for meaningful icons; decorative icons use `contentDescription = null`.
- Preserve TalkBack focus order in visual reading order.
- Respect font scaling without clipping button labels or card text.
- Use explicit IME and navigation bar insets for journal composer, date/time pickers, and bottom sheets.

## Do's and Don'ts

### Do

- Use Pinterest red only for primary actions, active state, and urgent/destructive clarity.
- Let cream surfaces and warm ink carry most of the UI.
- Prefer tonal separation and hairline borders over shadows.
- Keep dimensions compact and premium while preserving touch targets.
- Use obsidian surfaces for SOS and dark mode focus.
- Keep app copy direct, shame-free, and supportive.

### Don't

- Do not use cool-hued accent roles from the default Material palette.
- Do not enable dynamic color by default.
- Do not make cards oversized or heavily shadowed.
- Do not use marketing-page spacing inside routine app screens.
- Do not hardcode system bar sizes.
- Do not rely on emoji alone for mood, milestone, or action semantics.
