# Android Mobile Design — Layout & Content Reference

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/

---

## Table of Contents
1. [Layout Basics](#1-layout-basics)
2. [Parts of an App (Anatomy)](#2-parts-of-an-app-anatomy)
3. [Grids & Units](#3-grids--units)
4. [Content Structure](#4-content-structure)
5. [Layout & Navigation Patterns](#5-layout--navigation-patterns)
6. [Canonical Layouts](#6-canonical-layouts)
7. [Custom Layouts](#7-custom-layouts)
8. [Adapt Your Layout](#8-adapt-your-layout)
9. [Immersive Content](#9-immersive-content)
10. [Edge-to-Edge Design](#10-edge-to-edge-design)
11. [Images & Graphics](#11-images--graphics)

---

## 1. Layout Basics

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-basics

### Core Layout Principles
- **Top-down hierarchy:** Most important content first; scannable structure
- **Consistent margins:** Use 16dp horizontal margins on compact, 24dp on medium+
- **Breathing room:** Content areas need whitespace — avoid dense packing
- **Alignment:** Align elements to an 8dp grid; text baselines to 4dp sub-grid

### Layout Slots in Scaffold
```
┌─────────────────────────────────┐
│          Top App Bar            │ ← topBar slot
├─────────────────────────────────┤
│                                 │
│           Content               │ ← content slot (has padding from scaffold)
│                                 │
├─────────────────────────────────┤
│       Bottom Navigation         │ ← bottomBar slot
└─────────────────────────────────┘
    FAB floats over bottom-right   ← floatingActionButton slot
```

```kotlin
Scaffold(
    topBar = { TopAppBar(title = { Text("App") }) },
    bottomBar = { NavigationBar { /* destinations */ } },
    floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(...) } }
) { paddingValues ->
    ContentScreen(Modifier.padding(paddingValues))
}
```

### Padding & Margin System
| Context | Value |
|---|---|
| Screen edge margin (compact) | 16dp |
| Screen edge margin (medium/expanded) | 24dp |
| Component internal padding | 12–16dp |
| Between related items | 8dp |
| Between sections | 16–24dp |
| List item height (standard) | 56–72dp |

---

## 2. Parts of an App (Anatomy)

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/app-anatomy

### Chrome vs Content
- **Chrome** = persistent UI structure: app bars, nav bars, status bar, FAB
- **Content** = the unique value of each screen

### Anatomy Regions
```
┌──────────────────────────────────────────┐
│  Status bar (system — handled via insets)│
├──────────────────────────────────────────┤
│  Top App Bar                             │ ← Screen identity + key actions
│  (optional: subtitle, tabs below)        │
├──────────────────────────────────────────┤
│                                          │
│  Body / Content Area                     │ ← Primary content
│  - scrollable                            │
│  - respects horizontal margins           │
│  - cards, lists, forms, media            │
│                                          │
├──────────────────────────────────────────┤
│  Navigation Bar (app)                    │ ← Destination switching
│  (or: bottom app bar + FAB)              │
├──────────────────────────────────────────┤
│  System Navigation Bar (system insets)   │
└──────────────────────────────────────────┘
```

### Top App Bar Variants
| Variant | Height | When to Use |
|---|---|---|
| Small | 64dp | Default — most screens |
| Center-aligned | 64dp | Single-level destinations, symmetrical layouts |
| Medium | 112dp | Rich context needed (e.g. article detail) |
| Large | 152dp | Emphasis on screen title; hero-like treatment |

All top app bars collapse to Small on scroll (using `TopAppBarScrollBehavior`).

```kotlin
val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
Scaffold(
    topBar = {
        LargeTopAppBar(
            title = { Text("Title") },
            scrollBehavior = scrollBehavior
        )
    },
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
) { ... }
```

---

## 3. Grids & Units

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/grids-and-units

### Base Grid: 8dp
All spacing, sizing, and layout values should be multiples of 8dp.
Sub-values of 4dp acceptable for fine adjustments (icon padding, text baseline).

### Column Grid
| Window Size | Columns | Margin | Gutter |
|---|---|---|---|
| Compact (<600dp) | 4 | 16dp | 16dp |
| Medium (600–840dp) | 8 | 24dp | 24dp |
| Expanded (>840dp) | 12 | 24dp | 24dp |

### Breakpoints (Window Size Classes)
| Class | Width Range | Typical Device |
|---|---|---|
| Compact | 0–599dp | Phone portrait |
| Medium | 600–839dp | Tablet portrait, large phone landscape |
| Expanded | 840dp+ | Tablet landscape, foldable unfolded |

```kotlin
// Compose — get current window size class
val windowSizeClass = calculateWindowSizeClass(activity)
val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
```

### Keylines
- **Content keyline:** 16dp from edge (compact) / 24dp (medium+) — where content text starts
- **Icon keyline:** 16dp from edge — where leading icons align
- Text after icons: typically starts at 72dp (16dp margin + 24dp icon + 16dp gap + 16dp = 72dp)

### Touch Target Minimum
All interactive elements: minimum **48×48dp** touch target regardless of visual size.

---

## 4. Content Structure

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/content-structure

### Information Hierarchy
1. **Primary content** — what user came for; most visual weight
2. **Secondary content** — supports primary; less prominent
3. **Metadata** — supplementary info (dates, counts, tags)
4. **Actions** — directly relevant to content

### List Structures
| Type | Item Height | Components |
|---|---|---|
| One-line | 48dp | Text only |
| Two-line | 64dp | Title + supporting text |
| Three-line | 88dp | Title + 2-line supporting + optional media |
| List with avatar | 56–72dp | Leading avatar/icon + text |

### Card Structures
Cards are surface-level containers with:
- **Media** (optional, top) — aspect ratio 16:9 or square
- **Header** — title + subtitle + avatar
- **Body** — supporting text
- **Actions** — text buttons or icon buttons at bottom

MD3 card variants:
- **Elevated card** — drop shadow, surface bg
- **Filled card** — surface container bg, no shadow  
- **Outlined card** — border, surface bg

### Grouping & Dividers
- Dividers: 1dp, `OutlineVariant` color — use sparingly
- Section headers: `LabelLarge` or `TitleSmall`, `OnSurfaceVariant` color
- Inset dividers: start at icon keyline (72dp) to avoid cutting through leading icons

---

## 5. Layout & Navigation Patterns

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns

### Navigation Component Selection by Screen Size

| Component | When | Width |
|---|---|---|
| **Navigation Bar** (bottom) | 3–5 destinations, compact | Compact |
| **Navigation Rail** | 3–5 destinations | Medium / narrow expanded |
| **Navigation Drawer** (permanent) | 5+ destinations | Expanded |
| **Navigation Drawer** (modal) | 5+ destinations, compact | Any |

### Navigation Bar (Bottom)
- **3–5 destinations** — do not use for < 3 or > 5
- Each destination: icon + optional label
- Active destination: filled icon + label always visible
- Inactive: outlined icon, label optional
- Never use for actions (that's Bottom App Bar territory)
- Height: 80dp (includes system nav bar padding automatically in Compose)

```kotlin
NavigationBar {
    items.forEach { item ->
        NavigationBarItem(
            icon = { Icon(item.icon, contentDescription = item.label) },
            label = { Text(item.label) },
            selected = currentRoute == item.route,
            onClick = { navController.navigate(item.route) }
        )
    }
}
```

### Navigation Rail
- Same destinations as Navigation Bar but vertical, on side
- Switch from NavigationBar to NavigationRail at medium width
- Width: 80dp (compact rail) or 256dp (extended with labels)

### Navigation Drawer
- **Modal:** Overlays content; use on compact when > 5 destinations
- **Permanent:** Always visible; use on expanded screens
- Header: app/account identity
- Items: icon + label, grouped by section

### Tabs
- Use within a single destination for content filtering/sub-navigation
- Not for top-level destinations (use nav bar instead)
- Scrollable tabs: when > 5 tabs
- Fixed tabs: when ≤ 5 tabs, all fit without scrolling

---

## 6. Canonical Layouts

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/canonical-layouts

Three canonical layout patterns for adaptive design:

### 1. List-Detail
- Two panes: list (left/top) + detail (right/bottom)
- **Compact:** Single pane; navigate to detail screen
- **Medium/Expanded:** Both panes visible simultaneously
- Use for: email, contacts, files, settings

```
Compact:          Medium/Expanded:
┌──────────┐      ┌──────────┬──────────────┐
│  List    │      │  List    │   Detail     │
│  Item 1  │  →   │  Item 1  │   Content    │
│  Item 2  │      │  Item 2  │   ...        │
│  Item 3  │      │  Item 3  │              │
└──────────┘      └──────────┴──────────────┘
```

### 2. Supporting Pane
- Primary pane (main content) + supporting pane (contextual info/actions)
- Supporting pane opens on demand or is always visible on larger screens
- **Compact:** Sheet or nav to supporting content
- **Medium/Expanded:** Side-by-side

### 3. Feed
- Grid or list of cards; adapts column count by width
- **Compact:** 1 column
- **Medium:** 2 columns  
- **Expanded:** 3+ columns
- Use for: news, social, media galleries

```kotlin
// Compose — adaptive grid
val columns = when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.Compact -> 1
    WindowWidthSizeClass.Medium -> 2
    else -> 3
}
LazyVerticalGrid(columns = GridCells.Fixed(columns)) { ... }
```

### Implementation: `TwoPane` (Jetpack)
```kotlin
// androidx.window:window + adaptive layouts
ListDetailPaneScaffold(
    directive = calculatePaneScaffoldDirective(windowAdaptiveInfo),
    value = navigator.scaffoldValue,
    listPane = { ListScreen() },
    detailPane = { DetailScreen() }
)
```

---

## 7. Custom Layouts

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/custom-layouts

When canonical layouts don't fit, build custom adaptive layouts:

### Decision: When to Go Custom
- Use canonical layouts first — they handle most cases
- Go custom for unique brand moments, immersive experiences, game UIs
- Custom layouts must still handle all window sizes

### Compose Custom Layout
```kotlin
// Custom layout with Layout composable
Layout(
    content = content,
    modifier = modifier
) { measurables, constraints ->
    // Measure children
    val placeables = measurables.map { measurable ->
        measurable.measure(constraints)
    }
    // Calculate layout size
    layout(constraints.maxWidth, totalHeight) {
        // Place children
        placeables.forEach { placeable ->
            placeable.placeRelative(x, y)
        }
    }
}
```

### Adaptive Custom Layout Pattern
```kotlin
@Composable
fun AdaptiveContent(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactLayout()
        WindowWidthSizeClass.Medium -> MediumLayout()
        WindowWidthSizeClass.Expanded -> ExpandedLayout()
    }
}
```

---

## 8. Adapt Your Layout

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/adapt-layout

### Adaptive Strategy
1. **Design compact first** — single pane, vertical scroll
2. **Promote to medium** — add rail nav, 2-column grids, side panels
3. **Expand to large** — permanent drawer, multi-pane, 3+ columns

### Foldables
- **Half-opened (tabletop):** Content above fold, controls below
- **Book mode (side-by-side):** Two equal panes
- Detect fold state via `WindowInfoTracker`

```kotlin
val windowInfoTracker = WindowInfoTracker.getOrCreate(context)
lifecycleScope.launch {
    windowInfoTracker.windowLayoutInfo(activity).collect { layoutInfo ->
        val foldingFeature = layoutInfo.displayFeatures
            .filterIsInstance<FoldingFeature>().firstOrNull()
        // React to fold state
    }
}
```

### Window Size Class Integration
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            AppContent(windowSizeClass)
        }
    }
}
```

---

## 9. Immersive Content

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/immersive-content

### When to Use Immersive Mode
- Video/media playback (full-screen)
- Games
- Reading apps (optional)
- Art/photo viewing
- **NOT for:** general content apps, productivity apps

### Modes
| Mode | System Bar Behavior | Best For |
|---|---|---|
| Lean Back | Auto-hides; tap to show | Passive viewing (video) |
| Immersive | Hidden; swipe edge to show temporarily | Games, active engagement |
| Sticky Immersive | Hidden; swipe shows semi-transparent; auto-hides | Maximum immersion |

```kotlin
// Hide system bars
WindowCompat.getInsetsController(window, view).apply {
    hide(WindowInsetsCompat.Type.systemBars())
    systemBarsBehavior = WindowInsetsControllerCompat
        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

// Show system bars
WindowCompat.getInsetsController(window, view).apply {
    show(WindowInsetsCompat.Type.systemBars())
}
```

### Design Considerations
- Provide clear entry/exit mechanism for immersive mode
- Show controls overlay briefly on entry; fade out after 3 seconds
- Ensure back gesture still works (system gestures always available)
- Don't use immersive for primary navigation — users get stranded

---

## 10. Edge-to-Edge Design

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/edge-to-edge

### Why Edge-to-Edge
- **Required** for Android 15 (targetSdk 35) — enforced by platform
- Enables full-bleed content, immersive imagery
- System bars become transparent/translucent overlays

### Implementation Steps
```kotlin
// Step 1: Enable edge-to-edge (required for API < 35)
enableEdgeToEdge()  // In Activity.onCreate()

// Step 2: Handle insets in Scaffold (Compose)
Scaffold(
    // Scaffold handles top/bottom bars automatically
) { paddingValues ->
    LazyColumn(contentPadding = paddingValues) { ... }
}

// Step 3: For custom full-bleed content, consume insets manually
Box(
    Modifier
        .fillMaxSize()
        .background(color)
        // Content goes behind bars — no inset padding
) {
    // Full-bleed image/gradient

    // Interactive content — add inset padding
    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(16.dp)
    ) { /* bottom controls */ }
}
```

### Common Inset Patterns

**Bottom sheet with edge-to-edge:**
```kotlin
ModalBottomSheet(
    onDismissRequest = onDismiss,
    // Sheet automatically handles navigation bar insets
) { content() }
```

**Scrollable list with FAB:**
```kotlin
Scaffold(
    floatingActionButton = {
        FloatingActionButton(onClick = {}) { Icon(...) }
        // FAB auto-positioned above navigation bar
    }
) { padding ->
    LazyColumn(contentPadding = padding) { items(...) }
    // Last item not hidden behind FAB or nav bar
}
```

### Design Guidance
- **Background color** of content that draws behind status bar should work with both icon tints (light bg → dark icons, dark bg → light icons)
- Use **gradients** to ensure readability when content scrolls behind bars
- **Never hardcode** system bar heights — they vary by device and nav mode
- Test on both gesture nav (thin strip) and button nav (tall bar)

---

## 11. Images & Graphics

Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/images-graphics

### Image Aspect Ratios
| Ratio | Use Case |
|---|---|
| 16:9 | Media cards, video thumbnails, wide images |
| 3:2 | Photography, editorial images |
| 4:3 | Legacy / camera default |
| 1:1 | Avatars, album art, product thumbnails |
| 3:4 | Portrait photography, poster art |

### Loading & Placeholders
- Always show placeholder while image loads — prevents layout jump
- Use shimmer/skeleton for content-aware placeholders
- Handle error state with fallback image or icon

```kotlin
// Coil (recommended library)
AsyncImage(
    model = imageUrl,
    contentDescription = "Product image",
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error_image),
    contentScale = ContentScale.Crop,
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .clip(MaterialTheme.shapes.medium)
)
```

### Content Scale
| Mode | Behavior | Use |
|---|---|---|
| Crop | Fill bounds, crop excess | Thumbnails, avatars |
| Fit | Show full image, letterbox | Product images (no crop OK) |
| FillBounds | Stretch to fill (distorts) | Rarely appropriate |
| Inside | Fit but never enlarge | Icons |

### Vector Drawables
- Use vector drawables (SVG-based) for icons — scale perfectly
- `VectorPainter` in Compose; `VectorDrawable` in Views
- Max practical complexity: ~200 paths before performance degrades
- Prefer Material Icons library for standard icons

### Adaptive Icons (Launcher)
Android 8.0+ adaptive icon format:
```xml
<adaptive-icon xmlns:android="...">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <!-- Optional monochrome for themed icons (Android 13+) -->
    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
</adaptive-icon>
```
- Background: 108×108dp (safe zone: inner 72×72dp)
- Foreground: key content in safe zone
- System clips to circle, squircle, or device-specific shape

### Photography Guidelines
- Avoid stock photo clichés — use authentic, diverse imagery
- Ensure images work in both light and dark themes (or apply a scrim)
- Alt text (content description) required for all meaningful images
- Decorative images: `contentDescription = null`
