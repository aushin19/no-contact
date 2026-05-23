# Jetpack Compose Samples Catalog

Source: https://github.com/android/compose-samples  
Featured: https://github.com/android/nowinandroid  
Docs: https://developer.android.com/develop/ui/compose/samples

---

## Official Samples (android/compose-samples)

### 1. JetNews
**Complexity:** Medium  
**GitHub:** https://github.com/android/compose-samples/tree/main/JetNews  
**What it demonstrates:**
- Compose with typical Material app + real-world architecture
- Varied UI (list, detail, drawer)
- Light & dark themes
- Resource loading
- UI Testing with Compose

**Key APIs:** `Scaffold`, `TopAppBar`, `LazyColumn`, `DrawerLayout`, `MaterialTheme`, `stringResource`

**Patterns:** Single-activity, ViewModel + StateFlow, adaptive layout (phone/tablet)

---

### 2. Jetchat
**Complexity:** Low  
**GitHub:** https://github.com/android/compose-samples/tree/main/Jetchat  
**What it demonstrates:**
- UI state patterns and text input
- Material Design 3 + Material You dynamic color
- Back button handling
- Fragment + Compose interop
- Navigation Component integration

**Key APIs:** `TextField`, `BasicTextField`, `NavController`, `AnimatedVisibility`, `rememberSaveable`

**Patterns:** Fragment-hosted Compose, ViewModel + LiveData, state hoisting

---

### 3. Jetsnack
**Complexity:** Medium-High  
**GitHub:** https://github.com/android/compose-samples/tree/main/Jetsnack  
**What it demonstrates:**
- Custom design system (not stock Material)
- Complex UI with animations
- Custom color system + theming
- Shared element transitions

**Key APIs:** `Animatable`, `AnimationSpec`, `Canvas`, `Modifier.graphicsLayer`, custom `@Composable` theme wrappers

**Patterns:** Custom design system, bottom nav, nested scroll, complex state

---

### 4. Jetcaster
**Complexity:** High  
**GitHub:** https://github.com/android/compose-samples/tree/main/Jetcaster  
**What it demonstrates:**
- Podcast/audio player UI
- Complex media state management
- Adaptive UI for phone + tablet
- Lazy grids and lists

**Key APIs:** `LazyVerticalGrid`, `HorizontalPager`, `rememberPagerState`, `Modifier.fillMaxSize`, `ConstraintLayout` in Compose

**Patterns:** Repository pattern, Hilt DI, Room, ExoPlayer integration, coroutines + Flow

---

### 5. Reply
**Complexity:** Medium  
**GitHub:** https://github.com/android/compose-samples/tree/main/Reply  
**What it demonstrates:**
- Adaptive layouts (phone/foldable/tablet)
- Material Design 3
- Navigation Rail, NavigationDrawer, BottomNavigation
- Window size classes

**Key APIs:** `NavigationRail`, `PermanentNavigationDrawer`, `WindowSizeClass`, `adaptive` layout APIs

**Patterns:** Adaptive/responsive UI, canonical layouts, multi-pane

---

### 6. JetLagged
**Complexity:** Advanced  
**GitHub:** https://github.com/android/compose-samples/tree/main/JetLagged  
**What it demonstrates:**
- Custom layouts and graphics
- Sleep tracker UI with custom drawing
- Graphs using `Canvas` and `Path`
- Complex custom composables

**Key APIs:** `Canvas`, `Path`, `DrawScope`, `Modifier.layout`, `SubcomposeLayout`, `measurePolicy`

**Patterns:** Custom layout, custom drawing, data visualization without charts library

---

## Featured Sample

### Now in Android (NiA)
**Complexity:** Expert / Production-grade  
**GitHub:** https://github.com/android/nowinandroid  
**What it demonstrates:**
- Full production app: Kotlin + Compose end-to-end
- Recommended Android Architecture Guidelines
- Modularization (feature, core, data, ui modules)
- Offline-first with sync
- Compose UI testing at scale

**Key APIs:** Full Compose Navigation, Hilt, Room, DataStore, WorkManager, Coil, Kotlin Serialization

**Patterns:** Multi-module, offline-first, unidirectional data flow (UDF), baseline profiles, build flavors

---

## Material Catalog (AOSP)
**Source:** Lives in AOSP — always current, matches API reference docs  
**Browse:** https://github.com/android/compose-samples (linked from docs)  
**What it demonstrates:**
- Every Material Design 3 component in Compose
- Theme picker (runtime Material Theming)
- Links to guidelines, docs, source, issue tracker

---

## Skill Level Mapping

| Sample | Beginner | Intermediate | Advanced | Expert |
|--------|----------|--------------|----------|--------|
| Jetchat | ✓ | | | |
| JetNews | | ✓ | | |
| Reply | | ✓ | | |
| Jetsnack | | ✓ | ✓ | |
| Jetcaster | | | ✓ | |
| JetLagged | | | ✓ | |
| Now in Android | | | | ✓ |

---

## Key Compose APIs by Category

### Layout
- `Column`, `Row`, `Box` — basic layouts
- `LazyColumn`, `LazyRow`, `LazyVerticalGrid` — scrolling lists/grids
- `Scaffold` — standard Material layout structure
- `SubcomposeLayout` — custom layouts needing subcomposition
- `Modifier.layout` — custom measure/place

### State
- `remember`, `rememberSaveable` — local state
- `collectAsState()` — Flow → State
- `produceState` — async to state
- `derivedStateOf` — computed state

### Animation
- `AnimatedVisibility` — show/hide with animation
- `Animatable` — fine-grained animation control
- `animateContentSize` — smooth size changes
- `updateTransition` — multi-value transitions
- `rememberInfiniteTransition` — looping animations

### Theming
- `MaterialTheme` — M3 tokens (color, typography, shapes)
- Custom theme wrappers — Jetsnack pattern
- `isSystemInDarkTheme()` — dark mode detection
- `dynamicColorScheme()` — Material You

### Navigation
- `NavHost`, `NavController`, `composable {}` — Compose Navigation
- `NavigationRail`, `NavigationDrawer` — adaptive nav
- `BottomNavigation` / `NavigationBar` — M3

### Testing
- `createComposeRule()` — Compose UI test rule
- `onNodeWithText()`, `performClick()` — finders/actions
- `assertIsDisplayed()` — assertions

---

## References
- Compose samples page: https://developer.android.com/develop/ui/compose/samples
- GitHub repo: https://github.com/android/compose-samples
- Compose docs: https://developer.android.com/develop/ui/compose/documentation
- Compose API reference: https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary
- Now in Android: https://github.com/android/nowinandroid
- Compose codelabs: https://developer.android.com/courses/pathways/compose
- Quick guides: https://developer.android.com/develop/ui/compose/quick-guides
