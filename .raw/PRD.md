# PRD — NoContact App · MVP v1
**Version:** 1.0  
**Status:** Draft  
**Platform:** Android (API 26+, targets API 35)  
**Architecture:** MVVM + Hilt · Jetpack Compose · RoomDB · Offline-only · No Auth  
**Design System:** Material Design 3 · Edge-to-Edge · Dynamic Color (Android 12+)

---

## Table of Contents
1. [Product Overview](#1-product-overview)
2. [Goals & Non-Goals](#2-goals--non-goals)
3. [User Personas](#3-user-personas)
4. [Core Loop](#4-core-loop)
5. [Information Architecture](#5-information-architecture)
6. [Screen Specifications](#6-screen-specifications)
7. [Navigation Flow](#7-navigation-flow)
8. [RoomDB Schema](#8-roomdb-schema)
9. [Local Notifications](#9-local-notifications)
10. [Home Screen Widget](#10-home-screen-widget)
11. [Design System Tokens](#11-design-system-tokens)
12. [Component Usage Map](#12-component-usage-map)
13. [Motion & Transitions](#13-motion--transitions)
14. [Milestone System](#14-milestone-system)
15. [Affirmations Corpus](#15-affirmations-corpus)
16. [Edge Cases & Error States](#16-edge-cases--error-states)
17. [Build Order](#17-build-order)
18. [Out of Scope — v2+](#18-out-of-scope--v2)

---

## 1. Product Overview

NoContact is an offline Android app that helps users survive and recover from a breakup through **no-contact discipline**, **streak accountability**, **emotional logging**, and **affirmations**. Zero network calls. All data local. No login required.

**Core value prop:** Open app → see streak → feel urge → SOS → resist → log mood → read affirmation → close. Repeat daily.

---

## 2. Goals & Non-Goals

### Goals — MVP v1
- Track no-contact streak to the second
- Intercept relapse urges via SOS mode before user acts
- Serve daily rotating affirmations — no API
- Allow emotional journaling with mood tagging
- Celebrate milestones (1/3/7/14/30/60/90 days) with badges
- Home screen widget showing live streak
- Local push notifications for daily affirmation + mood check-in

### Non-Goals (v2+)
- AI coach / LLM calls
- Community / social features
- Human coach marketplace
- Photo red-flag vault
- Conversation analyzer
- Backend / cloud sync
- Authentication

---

## 3. User Personas

### Primary — "The Spiraler"
- Age 18–32, smartphone-native
- Just broke up (0–14 days ago), high emotional volatility
- Checks ex's socials compulsively, fights urge to text
- Needs: structure, accountability, distraction in the moment

### Secondary — "The Relapser"
- Has tried no-contact before, keeps breaking it
- Needs: streak protection, shame-free reset, trigger awareness

---

## 4. Core Loop

```
[Wake up]
    │
    ▼
[Open App → Home]
    │
    ├─ Mood not checked in today?
    │       └─► [Mood Check-In sheet] → save → Home
    │
    ├─ Read today's affirmation card
    │
    │── [Feel urge to contact ex?]
    │       └─► [SOS Button] → Trigger select → Breathing → Affirmation → Outcome
    │                   ├─ "I Resisted" → streak intact → milestone check → Home
    │                   └─ "I Slipped"  → reset dialog → new streak starts → Home
    │
    └─ [Journal Tab] → new entry → mood + text → save
```

---

## 5. Information Architecture

```
App Root
├── Onboarding (first launch only, 3 steps)
│   ├── Step 1 — Breakup type selection
│   ├── Step 2 — Breakup date picker
│   └── Step 3 — NC start date + affirmation time
│
└── Main Shell (Bottom Nav — 4 tabs)
    ├── 🏠 Home
    │   ├── Streak counter (live)
    │   ├── Affirmation card (today's)
    │   ├── Mood check-in prompt (if pending)
    │   └── SOS button (always visible)
    │
    ├── 📔 Journal
    │   ├── Entry list (reverse chron)
    │   └── New entry FAB → entry composer
    │
    ├── 🏆 Milestones
    │   ├── Current streak progress bar
    │   └── Badge grid (locked/unlocked)
    │
    └── ⚙️ Settings
        ├── Edit NC start date
        ├── Affirmation notification time
        ├── Mood check-in notification time
        ├── Dark mode toggle
        └── Reset all data (destructive, confirm dialog)
```

---

## 6. Screen Specifications

---

### 6.1 Onboarding — Step 1: Breakup Type

**Purpose:** Personalize copy and affirmation tone across the app.  
**Route:** `OnboardingTypeScreen`

**Layout:**
- `LargeTopAppBar` — no back arrow (first step)
- Headline: `headlineMedium` — "What happened?"
- Subtext: `bodyMedium`, `onSurfaceVariant`
- 4 `ElevatedCard` options in vertical list, full-width, 16dp horizontal padding
  - Each card: icon + label + 1-line description
  - Selected state: `primaryContainer` bg + checkmark trailing icon
- Primary `Button` ("Continue") at bottom, disabled until selection made
- Step indicator: 3-dot `LinearProgressIndicator` at top, 1/3 filled

**Options:**
| Label | Icon | Description |
|---|---|---|
| Toxic / Abusive | `Warning` | Unhealthy patterns, manipulation |
| Mutual / Grew apart | `Handshake` | Both wanted out |
| Ghosted / One-sided | `QuestionMark` | No closure, no explanation |
| Divorce / Long-term | `FamilyRestroom` | Married or long relationship |

**DB write:** `breakup_profile.breakup_type`

---

### 6.2 Onboarding — Step 2: Breakup Date

**Purpose:** Anchor emotional timeline; shown in streak context.  
**Route:** `OnboardingDateScreen`

**Layout:**
- `TopAppBar` with back arrow
- Headline: "When did it end?"
- `DatePicker` (Material 3 inline calendar) — default today, max today, no future
- Caption: `bodySmall`, `onSurfaceVariant` — "Used only to personalize your healing journey"
- "Continue" `Button` — always enabled (defaults to today if untouched)
- Step indicator: 2/3

**DB write:** `breakup_profile.breakup_date`

---

### 6.3 Onboarding — Step 3: NC Start + Notification Time

**Purpose:** Set streak anchor and notification preferences.  
**Route:** `OnboardingPrefsScreen`

**Layout:**
- `TopAppBar` with back arrow
- Step indicator: 3/3
- Section 1 — "When did you start no contact?"
  - Toggle row: "Same as breakup date" (Switch, default ON)
  - If OFF: `DatePicker` appears (max today)
- Divider
- Section 2 — "Daily affirmation time"
  - `ListItem` with clock icon + `TimePicker` on tap (default 08:00)
- Section 3 — "Evening check-in reminder"
  - `ListItem` with moon icon + `TimePicker` on tap (default 20:00)
- "Start Healing" filled `Button` — writes to DB, navigates to Home

**DB write:** `breakup_profile.nc_start_date`, `notification_affirmation_time`, `notification_checkin_time`

---

### 6.4 Home Screen

**Purpose:** Daily anchor — streak visibility, affirmation, SOS access.  
**Route:** `HomeScreen`

**Layout (top to bottom, edge-to-edge):**

```
[Status bar — transparent, edge-to-edge]
[TopAppBar — "NoContact" logo + settings icon (navigates to Settings)]

[Streak Hero Card]
  ├─ Label: "No Contact Streak" — labelMedium, onSurfaceVariant
  ├─ Days count: displayMedium, primary color, bold
  ├─ Hours/Minutes: titleSmall, onSurfaceVariant
  └─ Subtext: "Every hour counts. You're doing this." — bodySmall

[Today's Affirmation Card — ElevatedCard]
  ├─ Quote icon decorative
  ├─ Affirmation text: bodyLarge, center-aligned
  └─ "Tap to refresh" — labelSmall (rotates through today's pool)

[Mood Check-In Row — visible only if not done today]
  ├─ "How are you feeling?" — titleSmall
  └─ 5 emoji chips (horizontal scroll): 😔 💔 😐 🙂 💪
      └─ Tap any → saves mood + dismisses row

[SOS Button — Extended FAB, full-width ish, bottom center]
  ├─ Icon: Warning / Shield
  └─ Label: "I feel the urge to reach out"
  Position: above bottom nav, always visible

[Bottom Navigation Bar — 4 tabs]
  Home | Journal | Milestones | Settings
```

**Streak calculation logic:**
```
streakDays = (now - nc_start_date_of_latest_streak) / 1 day
streakHours = remainder hours
Update: Live via ViewModel + StateFlow, refreshed every minute via coroutine
```

**States:**
- **Day 0 (same day):** "You started today. That took courage." 
- **Relapse state (came from reset):** Gentle copy — "Day 1 again. You came back. That matters."
- **Milestone hit:** Banner appears with badge earned

---

### 6.5 SOS / Temptation Screen

**Purpose:** Intercept the urge before it becomes action. Most critical screen.  
**Route:** `SOSScreen` (full-screen, not in bottom nav)  
**Entry:** SOS FAB on Home  
**Design note:** Dark/deep surface bg to signal "safe space" — use `inverseSurface` tint overlay

**Layout (stepped flow, same screen):**

**Step 1 — Trigger Select**
```
Headline: "It's okay. You're here." — headlineSmall, center
Body: "What's pulling you toward them?" — bodyMedium

[FilterChip grid — 2 columns, wrapping]
  - 🌙 It's late and I'm alone
  - 🎵 A song reminded me of them
  - 📱 I saw their profile / story
  - 🍺 I've been drinking
  - 😢 I'm just really sad
  - 💭 I keep replaying memories
  - 😡 I want closure / answers
  - ✏️ Other

[Continue button — enabled after 1+ chip selected]
```

**Step 2 — Breathing Exercise**
```
Headline: "Breathe with me" — headlineSmall
Animated circle: expands/contracts using animateDpAsState
  4s inhale → 7s hold → 8s exhale (4-7-8 technique)
Phase label: "Inhale... / Hold... / Exhale..." — titleMedium

[Skip] TextButton — bottom right
Counter: "3 rounds" — auto-advances after 3 cycles
```

**Step 3 — Affirmation + Reason Anchor**
```
Affirmation card: ElevatedCard, prominent
"Remember why you started:" — labelMedium, onSurfaceVariant
Breakup type summary: "You left a toxic situation. That was brave." — bodyMedium

Streak reminder: "You've been NC for X days. Don't erase that." — titleSmall, primary
```

**Step 4 — Outcome**
```
Two buttons, full-width, stacked:

[I Resisted 💪] — Filled Button, primaryContainer
[I Slipped 😔]  — OutlinedButton, outline color

"I Resisted" flow:
  → Snackbar: "One more resist logged. Proud of you."
  → save SOS session (outcome = RESISTED)
  → navigate back to Home
  → milestone check runs

"I Slipped" flow:
  → AlertDialog: 
    Title: "No judgment here."
    Body: "Reaching out doesn't erase your progress. Every day 1 is a choice."
    [Reset Streak] FilledButton
    [Keep Current Streak] TextButton
  → If Reset: nc_start_date = now, insert streak_log row
  → navigate back to Home
```

---

### 6.6 Journal Screen

**Purpose:** Emotional release, reflection, pattern building.  
**Route:** `JournalScreen`

**Layout:**
- `LargeTopAppBar` — "Journal", collapses on scroll
- Entry list: `LazyColumn`, reverse chronological
  - Each item: `ElevatedCard`
    - Date + time: `labelSmall`, `onSurfaceVariant`  
    - Mood emoji + label: `labelMedium`
    - Entry preview: first 2 lines, `bodyMedium`, clipped
  - Tap → `JournalEntryDetailScreen` (read-only + delete option)
- Empty state: illustration + "Your feelings deserve space. Write your first entry." + FAB
- `ExtendedFAB` bottom-right: "New Entry" — expands on idle, collapses on scroll

**New Entry Composer (modal bottom sheet, full-height):**
```
TopBar: "New Entry" title + X close icon + "Save" TextButton
  
Mood selector row:
  5 emoji buttons horizontal, one selectable at a time
  😔 Devastated | 💔 Sad | 😐 Numb | 🙂 Okay | 💪 Strong

OutlinedTextField:
  - Placeholder: "What's on your mind?"
  - minLines = 6, scrollable
  - No char limit
  - Keyboard: multiline, sentence caps

[Save] — disabled until ≥ 10 chars
```

**DB:** `journal_entries` table — id, content, mood_tag, created_at

---

### 6.7 Milestones Screen

**Purpose:** Gamification — visualize progress, reward discipline.  
**Route:** `MilestonesScreen`

**Layout:**
- `TopAppBar` — "Your Progress"
- Current streak hero: streak days large number + progress bar to next milestone
  - `LinearProgressIndicator` — determinate, shows % to next badge
  - Label: "X days to [next milestone]"
- Badge grid: `LazyVerticalGrid`, 3 columns
  - Each badge: `OutlinedCard` (locked) / `ElevatedCard` (unlocked)
  - Badge icon (custom vector or emoji)
  - Day label: "7 Days"
  - Milestone name: "One Week Strong"
  - Locked state: greyscale + lock icon overlay
  - Unlocked state: full color + achieved date

**Milestone definitions:**
| Days | Badge Name | Icon |
|---|---|---|
| 1 | First Step | 🌱 |
| 3 | Three Days | 🔥 |
| 7 | One Week Strong | ⭐ |
| 14 | Two Weeks Clear | 🌙 |
| 30 | One Month Free | 💎 |
| 60 | Two Months Healed | 🦋 |
| 90 | 90 Days Warrior | 🏆 |

**Unlock event:** When streak crosses milestone threshold, `SharedPreferences` flag triggers Home screen banner on next open. Banner: `Snackbar` with action "View Badge".

---

### 6.8 Settings Screen

**Purpose:** User control, customization, data management.  
**Route:** `SettingsScreen`

**Layout:**
- `LargeTopAppBar` — "Settings"
- `LazyColumn` of grouped `ListItem` rows

**Sections:**

**Your Journey**
- NC Start Date — shows current date, tap → `DatePickerDialog`
- Breakup Type — shows current, tap → bottom sheet with 4 options

**Notifications**
- Daily Affirmation — time display, tap → `TimePickerDialog`
- Evening Check-in — time display, tap → `TimePickerDialog`
- Toggle switches for each notification type

**Appearance**
- Dark Mode — `Switch`, overrides system default

**Data**
- Export Journal (v2 — shown as disabled row with "Coming soon" label)
- Reset All Data — `TextButton` in error color
  - `AlertDialog`: "This will delete your streak, journal, and all data. This cannot be undone."
  - Confirm: "Delete Everything" filled error button
  - Cancel: "Keep My Data" text button
  - On confirm: wipe DB, clear prefs, navigate to Onboarding

---

## 7. Navigation Flow

```
[App Launch]
    │
    ├─ First launch (no breakup_profile) ──► [Onboarding Step 1]
    │                                              │
    │                                         Step 2 → Step 3
    │                                              │
    │                                         [Home Screen]
    │
    └─ Returning user ──────────────────────► [Home Screen]
                                                    │
                    ┌───────────────────────────────┤
                    │                               │
              [SOS Screen]                   [Bottom Nav]
              (full-screen)                      │
                    │                    ┌────────┴────────┐
              [back / auto]          [Journal]        [Milestones]    [Settings]
                                         │
                                  [Entry Detail]
                                  (back-stack)
```

**Back stack rules:**
- Onboarding: linear, no back from Step 1
- SOS: `popBackStack` returns to Home, not re-entrant
- Bottom nav: `launchSingleTop = true`, `restoreState = true`
- Predictive back: enabled via `OnBackPressedCallback` (Android 13+)
- No back arrow on Home (root destination)

---

## 8. RoomDB Schema

### Database Name: `breakfree_db` · Version: `1`

---

```sql
-- Singleton row. Always has exactly 1 row after onboarding.
CREATE TABLE breakup_profile (
  id               INTEGER PRIMARY KEY DEFAULT 1,
  breakup_type     TEXT NOT NULL,          -- TOXIC | MUTUAL | GHOSTED | DIVORCE
  breakup_date     INTEGER NOT NULL,       -- epoch millis
  nc_start_date    INTEGER NOT NULL,       -- epoch millis (current active streak start)
  notif_affirmation_time  TEXT NOT NULL,   -- "HH:mm" e.g. "08:00"
  notif_checkin_time      TEXT NOT NULL,   -- "HH:mm" e.g. "20:00"
  notif_affirmation_on    INTEGER NOT NULL DEFAULT 1,  -- boolean 0/1
  notif_checkin_on        INTEGER NOT NULL DEFAULT 1,
  dark_mode_override      INTEGER DEFAULT NULL,         -- NULL=system, 0=light, 1=dark
  created_at       INTEGER NOT NULL        -- epoch millis
);

-- One row per relapse/reset event. Streak history.
CREATE TABLE streak_log (
  id               INTEGER PRIMARY KEY AUTOINCREMENT,
  streak_start     INTEGER NOT NULL,       -- nc_start_date at time of reset
  streak_end       INTEGER NOT NULL,       -- epoch millis when reset occurred
  streak_days      INTEGER NOT NULL,       -- computed days at reset
  reason_tag       TEXT,                   -- TOXIC | DRUNK | LONELY | SAW_PROFILE | OTHER
  note             TEXT,                   -- optional user note on relapse
  created_at       INTEGER NOT NULL
);

-- One row per SOS session opened.
CREATE TABLE sos_sessions (
  id               INTEGER PRIMARY KEY AUTOINCREMENT,
  trigger_tags     TEXT NOT NULL,          -- comma-separated: "LATE_NIGHT,SONG"
  outcome          TEXT NOT NULL,          -- RESISTED | RELAPSED
  duration_seconds INTEGER NOT NULL,       -- how long SOS screen was open
  created_at       INTEGER NOT NULL
);

-- User journal entries.
CREATE TABLE journal_entries (
  id               INTEGER PRIMARY KEY AUTOINCREMENT,
  content          TEXT NOT NULL,
  mood_tag         TEXT NOT NULL,          -- DEVASTATED | SAD | NUMB | OKAY | STRONG
  created_at       INTEGER NOT NULL
);

-- Daily mood check-in. One per calendar day enforced in DAO.
CREATE TABLE mood_checkins (
  id               INTEGER PRIMARY KEY AUTOINCREMENT,
  mood_tag         TEXT NOT NULL,          -- same enum as journal
  note             TEXT,                   -- optional 1-liner
  date             TEXT NOT NULL UNIQUE,   -- "YYYY-MM-DD" — UNIQUE enforces 1/day
  created_at       INTEGER NOT NULL
);
```

### Key DAOs

```kotlin
// BreakupProfileDao
@Query("SELECT * FROM breakup_profile WHERE id = 1")
fun getProfile(): Flow<BreakupProfile?>

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsertProfile(profile: BreakupProfile)

// MoodCheckinDao — enforce 1 per day
@Query("SELECT * FROM mood_checkins WHERE date = :date LIMIT 1")
suspend fun getCheckinForDate(date: String): MoodCheckin?

// StreakLogDao
@Query("SELECT COUNT(*) FROM streak_log")
suspend fun getTotalRelapseCount(): Int

// SOSSessionDao
@Query("SELECT COUNT(*) FROM sos_sessions WHERE outcome = 'RESISTED' AND created_at > :since")
suspend fun getResistCountSince(since: Long): Int
```

---

## 9. Local Notifications

**Library:** `WorkManager` (periodic) + `NotificationManager` (exact alarm for specific times)  
**No network. All local.**

### Notification Channels

| Channel ID | Name | Importance | Use |
|---|---|---|---|
| `daily_affirmation` | Daily Affirmation | HIGH | Morning affirmation push |
| `mood_checkin` | Evening Check-in | DEFAULT | Evening mood reminder |
| `milestone` | Milestone Unlocked | HIGH | Badge unlock alert |

### Notification Specs

**Daily Affirmation (08:00 default)**
- Title: "Your affirmation for today"
- Body: random affirmation from local corpus (selected at schedule time)
- Icon: app icon
- Tap action: deep link → Home screen
- Schedule: `AlarmManager.setRepeating` or `WorkManager` daily periodic

**Evening Check-in (20:00 default)**
- Title: "How are you feeling tonight?"
- Body: "Take 10 seconds to check in with yourself."
- Tap action: deep link → Home with mood sheet open
- Only fires if today's `mood_checkins` row doesn't exist (check before notify)

**Milestone Unlocked**
- Title: "You earned a badge! 🏆"
- Body: "[Badge Name] — X days no contact"
- Tap action: deep link → Milestones screen
- Triggered in-app via ViewModel post-streak recalculation

---

## 10. Home Screen Widget

**Type:** `AppWidget` (Glance API — Jetpack Compose-based)  
**Sizes:** 2×1 (small), 4×1 (medium)

**Small (2×1):**
```
[🔥 icon]  [42 days]
           [No Contact]
```

**Medium (4×1):**
```
[🔥]  [42 days, 6 hours]       [You've got this]
      [No Contact Streak]
```

**Update frequency:** Every 15 minutes via `WorkManager` periodic task  
**Tap action:** Opens app to Home screen  
**Data source:** Reads directly from RoomDB via Repository

**Glance implementation note:**
- Use `GlanceAppWidget` + `GlanceAppWidgetReceiver`
- Colors: use `GlanceTheme` with MD3 token mapping
- Background: `android.R.attr.colorWidgetBackground` for system adaptive bg

---

## 11. Design System Tokens

### Color Palette — Seed Color
**Primary seed:** `#7B4CCA` (soft purple — healing, calm, introspection)  
Generate full scheme via [Material Theme Builder](https://m3.material.io/theme-builder)

### Key Color Roles in App

| Role | Light Hex (approx) | Use |
|---|---|---|
| `primary` | `#6750A4` | Streak number, active chips, buttons |
| `primaryContainer` | `#EADDFF` | Affirmation card bg, resisted button bg |
| `onPrimaryContainer` | `#21005D` | Text on affirmation card |
| `secondary` | `#625B71` | Secondary labels |
| `secondaryContainer` | `#E8DEF8` | Mood chips selected state |
| `surface` | `#FFFBFE` | Screen backgrounds |
| `surfaceContainer` | `#F3EDF7` | Cards default bg |
| `error` | `#B3261E` | Reset data button |
| `inverseSurface` | `#313033` | SOS screen overlay tint |

### Typography Usage

| Style Token | Used For |
|---|---|
| `displayMedium` | Streak day count hero |
| `headlineMedium` | Onboarding headlines |
| `headlineSmall` | SOS screen headline |
| `titleMedium` | Section headings, card titles |
| `bodyLarge` | Affirmation text, journal body |
| `bodyMedium` | Supporting text, descriptions |
| `labelMedium` | Mood chips, badge labels |
| `labelSmall` | Timestamps, captions |

### Shape Scale Usage

| Token | Radius | Used For |
|---|---|---|
| `small` | 8dp | Mood chips, small badges |
| `medium` | 12dp | All cards, dialog |
| `large` | 16dp | Bottom sheets, SOS overlay |
| `extraLarge` | 28dp | Extended FAB |
| `full` | 50% | Breathing circle animation |

---

## 12. Component Usage Map

| Screen | Key Components |
|---|---|
| Onboarding Type | `ElevatedCard` (selectable), `LinearProgressIndicator`, `Button` |
| Onboarding Date | `DatePicker`, `TopAppBar` with back, `Button` |
| Onboarding Prefs | `Switch`, `ListItem`, `TimePicker`, `FilledButton` |
| Home | `LargeTopAppBar` (collapsed), streak hero card, `ElevatedCard`, `FilterChip` row, `ExtendedFAB` |
| SOS | `FilterChip` grid, animated `Box` (breathing), `ElevatedCard`, `Button` + `OutlinedButton`, `AlertDialog` |
| Journal List | `LargeTopAppBar`, `LazyColumn` of `ElevatedCard`, `ExtendedFAB` |
| Journal Composer | `ModalBottomSheet`, `OutlinedTextField`, emoji `IconButton` row |
| Milestones | `TopAppBar`, `LinearProgressIndicator`, `LazyVerticalGrid` of cards |
| Settings | `LargeTopAppBar`, `LazyColumn`, `ListItem`, `Switch`, `AlertDialog` |

---

## 13. Motion & Transitions

| Transition | Pattern | Duration |
|---|---|---|
| Bottom nav tab switch | **Fade Through** | 300ms |
| Home → SOS | **Shared Axis Z** (scale up) | 400ms |
| SOS back → Home | **Shared Axis Z** (scale down) | 300ms |
| SOS step 1→2→3→4 | **Shared Axis X** (slide right) | 250ms |
| Onboarding steps | **Shared Axis X** | 300ms |
| Journal entry open | **Container Transform** (card expands) | 350ms |
| Milestone unlock banner | **Slide in from top** + spring | 400ms |
| Breathing circle | `animateDpAsState` spring, dampingRatio = Low | 4000ms / phase |
| Mood check-in dismiss | **Fade out** | 200ms |

**Reduced motion:** Check `LocalAccessibilityManager.isReduceMotionEnabled`, collapse all durations to 0 if true.

---

## 14. Milestone System

**Calculation:** Pure function — `currentStreakDays = daysBetween(nc_start_date, now)`  
**Persistence:** `SharedPreferences` — set of unlocked milestone IDs (not DB — simpler)

```kotlin
val MILESTONES = listOf(1, 3, 7, 14, 30, 60, 90) // days

fun checkMilestones(currentDays: Int, previousDays: Int): List<Int> {
    return MILESTONES.filter { it in (previousDays + 1)..currentDays }
}
// Called in HomeViewModel on each streak tick
// If result non-empty → fire milestone notification + update SharedPrefs
```

**Streak reset behavior:**
- On relapse: `nc_start_date` updated to now in `breakup_profile`
- `streak_log` row inserted with old start, end, days, reason
- All milestone SharedPrefs cleared (start fresh)
- UI copy: "Day 1. Every comeback starts here."

**Streak shield (v2):** Not in MVP. Note for later.

---

## 15. Affirmations Corpus

**Storage:** Hardcoded `strings.xml` or `affirmations.json` in `assets/`  
**Count:** Minimum 90 entries (one per day for 90-day journey, can cycle)  
**Selection:** `Random.nextInt(size)` seeded with `date.toEpochDay()` → same affirmation all day  
**Personalization by type:** Tag each affirmation — `GENERAL | TOXIC | GHOSTED | MUTUAL | DIVORCE`  
**Selection logic:** 60% type-matched + 40% general, random within pool

**Sample entries:**
```
"Protecting your peace is not selfish. It is survival."
"You cannot heal in the same environment that hurt you."
"Every day without contact is a day you chose yourself."
"The urge to reach out will pass. Your self-respect will remain."
"You are not waiting for them. You are returning to yourself."
"No contact is not punishment. It is medicine."
```

---

## 16. Edge Cases & Error States

| Scenario | Handling |
|---|---|
| NC start date in future | Disable "Start" button, show error: "Date must be today or earlier" |
| Mood check-in already done today | Row hidden on Home, no notification fires |
| Journal entry < 10 chars | "Save" button stays disabled, subtle shake animation |
| Streak = 0 hours (just reset) | Show "You started today" copy, no negative numbers |
| DB insert failure | Log to Logcat (no-op in MVP, no analytics) |
| Widget data stale | WorkManager retry — show last known data with "—" if null |
| Notification permission denied (Android 13+) | In-app banner on first Home open: "Enable notifications for daily support" + Settings deeplink |
| Very long journal entry (10k+ chars) | `OutlinedTextField` scrollable, no truncation, RoomDB TEXT handles it |
| User resets data mid-onboarding | Impossible — reset only in Settings, onboarding only shown if no profile |

---

## 17. Build Order

```
Phase 1 — Foundation (Week 1)
  ├── Project setup: Compose, Hilt, Room, Navigation
  ├── RoomDB entities + DAOs + Database class
  ├── Repository layer
  └── Theme + color tokens + typography

Phase 2 — Onboarding (Week 1-2)
  ├── OnboardingTypeScreen
  ├── OnboardingDateScreen
  ├── OnboardingPrefsScreen
  └── Profile write + nav to Home

Phase 3 — Core Streak (Week 2)
  ├── HomeViewModel (streak calc, StateFlow)
  ├── HomeScreen UI
  └── Streak persistence + live update

Phase 4 — SOS Flow (Week 2-3)
  ├── SOSScreen (all 4 steps)
  ├── Breathing animation
  ├── Outcome handling (resisted / relapsed)
  └── streak_log insert

Phase 5 — Journal (Week 3)
  ├── JournalScreen list
  ├── New entry bottom sheet
  ├── JournalEntryDetail
  └── CRUD + mood tag

Phase 6 — Milestones (Week 3)
  ├── Badge grid screen
  ├── Milestone unlock logic
  └── Progress bar

Phase 7 — Affirmations (Week 3-4)
  ├── JSON corpus in assets
  └── Day-seeded selection logic

Phase 8 — Notifications (Week 4)
  ├── Channel setup
  ├── WorkManager scheduling
  └── Permission handling (Android 13+)

Phase 9 — Widget (Week 4)
  ├── Glance AppWidget
  ├── Small + medium layouts
  └── WorkManager update job

Phase 10 — Settings + Polish (Week 4-5)
  ├── SettingsScreen all rows
  ├── Dark mode toggle
  ├── Reset flow
  ├── Transitions + motion
  └── Edge case hardening
```

---

## 18. Out of Scope — v2+

| Feature | Notes |
|---|---|
| AI coaching chat | Requires Anthropic/OpenAI API, network |
| Community / forum | Backend required |
| Human coach booking | Marketplace + payments |
| Red flag photo vault | Media storage, encryption |
| Conversation analyzer | LLM calls |
| Cloud backup / sync | Auth + backend |
| Attachment style quiz | Informs AI coaching — needs v2 AI first |
| Unsent letter "burn" ceremony | Delightful, low-effort — candidate for v1.5 |
| Streak shield / protection | Gamification expansion — v1.5 |
| Journal export to PDF | v2 |
| Trigger analytics / heatmap | Need data volume first — v2 |
| iOS port | Post-Android validation |

---

*PRD authored for NoContact MVP v1 · Offline · No Auth · RoomDB · Jetpack Compose · MD3*
