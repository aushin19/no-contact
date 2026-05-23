# Android Mobile Design — Behaviors & Patterns Reference

Source: https://developer.android.com/design/ui/mobile/guides/patterns/

---

## Table of Contents
1. [Predictive Back](#1-predictive-back)
2. [Navigation Patterns](#2-navigation-patterns)
3. [Sharing](#3-sharing)
4. [Settings](#4-settings)
5. [Search Patterns](#5-search-patterns)
6. [Error & Empty States](#6-error--empty-states)
7. [Loading Patterns](#7-loading-patterns)

---

## 1. Predictive Back

Source: https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back

### What It Is
Android 13+ feature: user can peek at the destination before completing the back gesture. Swipe partially from edge → preview appears. Complete swipe → navigate back. Release → cancel.

### System Predictive Back (Free, No Code)
For standard Android navigation (NavController, Activity back stack), predictive back works automatically when `android:enableOnBackInvokedCallback="true"` in manifest:

```xml
<!-- AndroidManifest.xml -->
<application
    android:enableOnBackInvokedCallback="true"
    ...>
```

This enables the system cross-activity animation for free.

### Custom Predictive Back Animations
For custom in-app transitions, use the AndroidX Back APIs:

```kotlin
// AndroidX Activity 1.8+ approach
val onBackPressedCallback = object : OnBackPressedCallback(enabled = true) {
    override fun handleOnBackPressed() {
        // Perform back navigation
    }

    override fun handleOnBackProgressed(backEvent: BackEventCompat) {
        // backEvent.progress: 0.0 (start) → 1.0 (committed)
        // backEvent.touchX, touchY: finger position
        // Use to animate your custom back preview
        animateWithProgress(backEvent.progress)
    }

    override fun handleOnBackCancelled() {
        // User released without completing — reset animations
        resetAnimations()
    }
}
onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
```

### Compose — Predictive Back
```kotlin
// In Compose with AnimatedContent
val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

PredictiveBackHandler { progress ->
    // progress is a Flow<BackEventCompat>
    try {
        progress.collect { backEvent ->
            // Animate based on backEvent.progress
        }
        // Back committed — perform navigation
        onBack()
    } catch (e: CancellationException) {
        // Back cancelled — reset
    }
}
```

### Design Requirements
- **All apps targeting Android 13+ should support** predictive back
- Provide meaningful visual preview of destination
- Animations must be smooth (60fps minimum)
- Don't block or prevent the back gesture for primary navigation
- Dialog/sheet dismiss: supported automatically by MD3 components

### Patterns That Must Support Predictive Back
- Screen-to-screen navigation
- Bottom sheet dismiss
- Dialog dismiss
- Side drawer close
- Fullscreen media dismiss

---

## 2. Navigation Patterns

Source: https://developer.android.com/design/ui/mobile/guides/patterns/navigation  
Related: https://developer.android.com/guide/navigation/navigation-principles

### Navigation Principles
1. **Fixed start destination:** Every app has a fixed home destination (back from it → exits app)
2. **Back stack is LIFO:** Back always goes to previous destination
3. **Deep links bypass back stack:** Handle gracefully (back should go to logical parent)
4. **Up ≠ Back:** "Up" (←) navigates app hierarchy; "Back" navigates history (usually same, but not always)

### NavController (Jetpack Navigation)
```kotlin
// Setup
val navController = rememberNavController()

NavHost(navController = navController, startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("detail/{id}") { backStackEntry ->
        DetailScreen(
            id = backStackEntry.arguments?.getString("id"),
            navController = navController
        )
    }
}

// Navigate
navController.navigate("detail/123")

// Navigate up
navController.navigateUp()

// Navigate with popUpTo (clear back stack)
navController.navigate("home") {
    popUpTo("home") { inclusive = false }
    launchSingleTop = true
}
```

### Navigation Patterns by Destination Relationship

**Sequential Flow (Wizard/Onboarding):**
- Each step adds to back stack
- Back goes to previous step
- Cancel/skip pops to start

**Parallel Navigation (Bottom Nav):**
- Each tab maintains its own back stack
- Switching tabs does not add to main back stack
- Back from non-home tab → goes to home tab (not previous tab)

```kotlin
// Bottom nav with proper back stack per tab
NavigationBar {
    items.forEach { screen ->
        NavigationBarItem(
            selected = currentRoute == screen.route,
            onClick = {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            ...
        )
    }
}
```

**Hierarchical Navigation:**
- Top app bar shows ← back button (not ≡ menu)
- Represents drilling into detail

### Safe Args / Argument Passing
```kotlin
// Typed navigation arguments
composable(
    "detail/{itemId}",
    arguments = listOf(navArgument("itemId") { type = NavType.IntType })
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
    DetailScreen(itemId)
}

// Navigate with argument
navController.navigate("detail/42")
```

### Deep Links
```kotlin
// Declare deep link in NavHost
composable(
    "article/{id}",
    deepLinks = listOf(
        navDeepLink { uriPattern = "https://example.com/article/{id}" }
    )
) { ... }
```
```xml
<!-- AndroidManifest.xml -->
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:scheme="https" android:host="example.com"/>
    </intent-filter>
</activity>
```

---

## 3. Sharing

Source: https://developer.android.com/design/ui/mobile/guides/patterns/sharing

### Android Sharesheet (System Share)
Always use the system share sheet — do NOT build a custom one.

```kotlin
// Share text
val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, "Sharing this content: https://example.com")
    putExtra(Intent.EXTRA_TITLE, "Check this out")  // preview title
    type = "text/plain"
}
val shareIntent = Intent.createChooser(sendIntent, null)
startActivity(shareIntent)

// Share image/file
val shareIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_STREAM, contentUri)  // FileProvider URI
    type = "image/jpeg"
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
startActivity(Intent.createChooser(shareIntent, null))
```

### Share Entry Points
- **Share icon** in top app bar actions — use `Icons.Default.Share`
- **Long-press context menu** on shareable items
- **Share action** in bottom app bar for selected items
- **Never** add share to FAB

### Receiving Shared Content
```xml
<!-- Manifest — accept shared text -->
<activity android:name=".ReceiveActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEND"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <data android:mimeType="text/plain"/>
    </intent-filter>
</activity>
```
```kotlin
// Handle in activity
if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    // process sharedText
}
```

### Sharing Rich Preview
Android 10+: system share sheet shows a preview (thumbnail + title):
```kotlin
val thumbnail: Bitmap = /* your image */
val clipData = ClipData.newUri(contentResolver, "Image", contentUri)
val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_STREAM, contentUri)
    putExtra(Intent.EXTRA_TITLE, "Share title")
    type = "image/*"
    this.clipData = clipData
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
```

---

## 4. Settings

Source: https://developer.android.com/design/ui/mobile/guides/patterns/settings

### Settings Principles
- Settings = user preferences, NOT content or actions
- Separate settings from content discovery
- Never use settings for onboarding
- Settings entry point: overflow menu (⋮) → "Settings"

### Settings Architecture
```
Settings (root)
├── Account
│   ├── Profile
│   └── Privacy
├── Notifications
│   ├── Push notifications
│   └── Email digest
├── Appearance
│   ├── Theme (System/Light/Dark)
│   └── Font size
└── About
    ├── Version
    ├── Terms of Service
    └── Privacy Policy
```

### Preference Types (Jetpack Preference Library)

| Preference | Use |
|---|---|
| `SwitchPreferenceCompat` | Boolean toggle |
| `ListPreference` | Single selection from list |
| `MultiSelectListPreference` | Multiple selection from list |
| `EditTextPreference` | Free text input |
| `SeekBarPreference` | Numeric range |
| `PreferenceCategory` | Group header |
| `Preference` (no value) | Navigation link, info display |

```kotlin
// Compose settings with DataStore
val context = LocalContext.current
val dataStore = context.dataStore

val isDarkMode by dataStore.data
    .map { prefs -> prefs[DARK_MODE_KEY] ?: false }
    .collectAsState(initial = false)

// Setting row pattern
ListItem(
    headlineContent = { Text("Dark mode") },
    trailingContent = {
        Switch(checked = isDarkMode, onCheckedChange = { setDarkMode(it) })
    }
)
```

### Settings Grouping
- Group by feature area, not by technical category
- Max 7 items per group before splitting into sub-screen
- Sub-screens for groups with 4+ settings
- Use `PreferenceCategory` / section headers for visual grouping

### Settings DO / DON'T
| DO | DON'T |
|---|---|
| Show current value below setting name | Hide current state |
| Immediately apply changes (no Save button) | Require explicit save |
| Provide clear labels in plain language | Use technical jargon |
| Group related settings | Mix unrelated settings in one group |
| Link to system settings where appropriate | Duplicate system settings |
| Show confirmation dialog for destructive settings | Silently reset/clear data |

---

## 5. Search Patterns

### Search Entry Points
- **Persistent search bar** — top of screen; always visible; best for search-primary apps
- **Search icon in top bar** — expands to full search UI on tap; good for secondary search
- **In-content search field** — within a list/form; for filtering visible content

### Search Behavior
```kotlin
// Search bar with suggestions
var query by remember { mutableStateOf("") }
var isActive by remember { mutableStateOf(false) }

SearchBar(
    query = query,
    onQueryChange = {
        query = it
        loadSuggestions(it)
    },
    onSearch = { performSearch(query) },
    active = isActive,
    onActiveChange = { isActive = it },
    placeholder = { Text("Search") },
    leadingIcon = {
        if (isActive) {
            IconButton(onClick = { isActive = false; query = "" }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
            }
        } else {
            Icon(Icons.Default.Search, contentDescription = null)
        }
    },
    trailingIcon = {
        if (query.isNotEmpty()) {
            IconButton(onClick = { query = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
        }
    }
) {
    // Suggestions content
    suggestions.forEach { suggestion ->
        ListItem(
            headlineContent = { Text(suggestion) },
            leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
            modifier = Modifier.clickable { query = suggestion; performSearch(suggestion) }
        )
    }
}
```

### Search Results
- Show skeleton/loading state immediately
- Empty state: explain why no results + suggest corrections
- Error state: allow retry
- Apply filters inline (FilterChip row above results)

---

## 6. Error & Empty States

### Empty States
Always explain WHY empty + provide a path forward:
```
[Illustration]
"No messages yet"
You'll see your conversations here once you start chatting.
[Start a conversation]  ← Primary action button
```

Types:
- **No data yet** — first-use, onboarding action
- **No search results** — suggest alternatives, clear search
- **Filtered to zero** — offer to clear filters
- **Error loading** — retry action

### Error States
| Error Type | Pattern |
|---|---|
| Network error | Inline error with Retry button |
| Form validation | Inline error below field |
| Critical error | Full-screen error + Retry |
| Partial failure | Snackbar with action |
| Permissions denied | Explanation + Settings link |

```kotlin
// Inline error state
if (loadState is LoadState.Error) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null,
             modifier = Modifier.size(64.dp))
        Text("Couldn't load content", style = MaterialTheme.typography.titleMedium)
        Text("Check your connection and try again",
             style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { retry() }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Retry")
        }
    }
}
```

---

## 7. Loading Patterns

### Loading State Hierarchy
1. **Skeleton screens** (preferred) — layout placeholder matching content shape
2. **Progress indicator** — when skeleton can't represent content
3. **Shimmer** — animated gradient over skeleton (use sparingly)
4. **Pull-to-refresh** — for user-initiated refresh of lists

```kotlin
// Skeleton placeholder with Compose
if (isLoading) {
    // Repeat card shape with gray boxes
    repeat(5) {
        Card(Modifier.fillMaxWidth().padding(8.dp)) {
            Column(Modifier.padding(16.dp)) {
                Box(Modifier.fillMaxWidth(0.7f).height(20.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.small))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.small))
            }
        }
    }
} else {
    ContentList(items)
}

// Pull to refresh
val pullToRefreshState = rememberPullToRefreshState()
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = { refresh() },
    state = pullToRefreshState
) {
    LazyColumn { items(data) { ItemRow(it) } }
}
```

### Loading Performance Guidelines
- Show skeleton within 100ms of navigation
- Show content within 2 seconds (P50) / 5 seconds (P95) for network data
- Cache aggressively — show stale data while refreshing
- Never show spinner without progress when operation takes >10 seconds
