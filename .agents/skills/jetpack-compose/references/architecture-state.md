# Compose — UI Architecture & State

Ref: https://developer.android.com/develop/ui/compose/lifecycle
     https://developer.android.com/develop/ui/compose/state
     https://developer.android.com/develop/ui/compose/side-effects
     https://developer.android.com/develop/ui/compose/phases
     https://developer.android.com/develop/ui/compose/architecture
     https://developer.android.com/develop/ui/compose/navigation

---

## Composable Lifecycle

A composable enters the **Composition**, recomposes zero or more times, then leaves.

```
Enter Composition
    ↓
Recompose (when inputs/state change)
    ↓ (n times)
Leave Composition
```

- Identity tracked by **call site** position in the composition tree
- `key(id) { ... }` — force stable identity across reorders
- Each composable instance has its own `remember` storage

**When does recomposition happen?**
- A `State<T>` read inside the composable changes
- Only the **reading scope** recomposes, not the whole tree

**Stability rules (skippable recomposition):**
- Primitive types: always stable
- `@Stable` / `@Immutable` annotated classes: stable
- Mutable classes (e.g. `ArrayList`, non-val data classes): unstable → forces recomposition
- Fix: use `@Stable`, `ImmutableList` (kotlinx.collections.immutable), or `@Immutable` data classes

---

## Phases

| Phase | What happens | Can read |
|-------|-------------|---------|
| Composition | Runs composables, builds node tree | State |
| Layout | Measures, places nodes | Layout info |
| Draw | Renders pixels | — |

**Deferring reads to later phases improves perf:**
```kotlin
// BAD — reads state in composition phase, causes full recompose
val offset = scrollState.value
Box(Modifier.offset(y = offset.dp))

// GOOD — reads in layout phase, skips composition
Box(Modifier.offset { IntOffset(0, scrollState.value) })
```

---

## State APIs

```kotlin
// Core
val s = remember { mutableStateOf(0) }   // s.value
var s by remember { mutableStateOf(0) }  // delegate syntax (import getValue/setValue)
val s = rememberSaveable { mutableStateOf(0) } // survives process death

// Collections
val list = remember { mutableStateListOf<Item>() }
val map  = remember { mutableStateMapOf<K, V>() }

// From Flow / LiveData
val value by flow.collectAsStateWithLifecycle()   // lifecycle-aware (recommended)
val value by flow.collectAsState()                // no lifecycle awareness
val value by liveData.observeAsState()

// Derived
val derived = remember(a, b) { expensiveCompute(a, b) }
val derived by remember { derivedStateOf { list.filter { it.active } } }
// derivedStateOf: recomputes only when result changes, not on every read
```

**State hoisting rules:**
- Hoist to lowest common ancestor of all composables that read/write state
- Stateless composables are easier to test, preview, reuse
- Pass state down, events up (lambda callbacks)

**Where to hoist:**
| Scope | Use |
|-------|-----|
| Single composable | `remember` inside |
| Shared across siblings | Hoist to parent composable |
| Survive nav/config change | `ViewModel` |
| Survive process death | `rememberSaveable` or persisted store |

**Save/restore state:**
```kotlin
// Custom saver
val saver = listSaver<MyState, Any>(
    save = { listOf(it.x, it.y) },
    restore = { MyState(it[0] as Int, it[1] as Int) }
)
val state = rememberSaveable(stateSaver = saver) { MyState() }

// Parcelize
@Parcelize data class MyState(...) : Parcelable
val state = rememberSaveable { MyState() }
```

---

## Side Effects

Side effects = work that escapes composition (network, DB, analytics, etc.)

### LaunchedEffect
```kotlin
// Runs coroutine when key changes; cancels previous coroutine on key change
LaunchedEffect(userId) {
    val data = repo.fetch(userId)
    // update state here
}
// key = Unit → runs once on entry
LaunchedEffect(Unit) { viewModel.loadInitial() }
```

### rememberCoroutineScope
```kotlin
// Use when you need coroutine in callbacks (onClick, etc.)
val scope = rememberCoroutineScope()
Button(onClick = { scope.launch { doWork() } }) { ... }
```

### DisposableEffect
```kotlin
// For effects needing cleanup (listeners, observers)
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event -> ... }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

### SideEffect
```kotlin
// Runs after every SUCCESSFUL recomposition; for syncing Compose state to non-Compose code
SideEffect {
    analytics.setScreen(currentScreen)
}
```

### produceState
```kotlin
// Converts non-Compose state source into State<T>
val image by produceState<Bitmap?>(null, url) {
    value = loadImage(url)
}
```

### snapshotFlow
```kotlin
// Converts Compose State into a Flow
snapshotFlow { scrollState.value }
    .distinctUntilChanged()
    .collect { offset -> ... }
```

---

## Architecture Patterns

**Recommended UDF (Unidirectional Data Flow):**
```
ViewModel (business logic, state)
    ↓ StateFlow<UiState>
UI Layer (composables observe state)
    ↓ events/intents
ViewModel
```

```kotlin
// ViewModel
class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

    fun onAction(action: MyAction) {
        viewModelScope.launch { /* update _uiState */ }
    }
}

// Composable
@Composable
fun MyScreen(vm: MyViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    MyContent(state = state, onAction = vm::onAction)
}
```

**Architectural layering:**
- `compose.ui` — low-level (layout, drawing, input)
- `compose.foundation` — building blocks (LazyColumn, gestures)
- `compose.material3` — Material Design components
- Custom components built on foundation, not material (for full control)

---

## CompositionLocal

Implicitly pass data down the composition tree without explicit params.

```kotlin
// Define
val LocalUserTheme = compositionLocalOf { UserTheme.Default }
// or staticCompositionLocalOf for rarely-changed values (more efficient)
val LocalAppConfig = staticCompositionLocalOf { AppConfig() }

// Provide (wrap subtree)
CompositionLocalProvider(LocalUserTheme provides darkTheme) {
    ChildComposable()
}

// Consume (anywhere in subtree)
val theme = LocalUserTheme.current

// Built-in locals: LocalContext, LocalDensity, LocalConfiguration,
// LocalFocusManager, LocalSoftwareKeyboardController, LocalHapticFeedback
```

---

## Navigation (Navigation Compose)

```kotlin
// Dependency
implementation("androidx.navigation:navigation-compose:2.8.x")

// Setup
val navController = rememberNavController()

NavHost(
    navController = navController,
    startDestination = "home"
) {
    composable("home") { HomeScreen(navController) }

    // Typed args
    composable(
        "detail/{id}",
        arguments = listOf(navArgument("id") { type = NavType.IntType })
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        DetailScreen(id, navController)
    }

    // Deep link
    composable(
        "profile/{uid}",
        deepLinks = listOf(navDeepLink { uriPattern = "myapp://profile/{uid}" })
    ) { ... }
}

// Actions
navController.navigate("detail/42")
navController.navigate("detail/42") {
    popUpTo("home") { inclusive = false }
    launchSingleTop = true
}
navController.popBackStack()
navController.navigateUp()

// Pass complex objects: use SavedStateHandle or serialize to JSON string
// Type-safe navigation (Nav 2.8+): use @Serializable route objects
```

**Back stack & shared ViewModel:**
```kotlin
// ViewModel scoped to nav graph (shared between destinations)
val vm: SharedViewModel = viewModel(
    remember { navController.getBackStackEntry("graph_route") }
)
```

**Bottom nav integration:**
```kotlin
val currentDest by navController.currentBackStackEntryAsState()
NavigationBar {
    items.forEach { item ->
        NavigationBarItem(
            selected = currentDest?.destination?.route == item.route,
            onClick = { navController.navigate(item.route) { ... } },
            icon = { Icon(item.icon, null) },
            label = { Text(item.label) }
        )
    }
}
```

---

## State Lifespans

| Storage | Survives recompose | Survives config change | Survives process death |
|---------|-------------------|----------------------|----------------------|
| Local var | ✗ | ✗ | ✗ |
| `remember` | ✓ | ✗ | ✗ |
| `rememberSaveable` | ✓ | ✓ | ✓ (Parcelable/Saver) |
| ViewModel | ✓ | ✓ | ✗ |
| Persisted (DataStore, DB) | ✓ | ✓ | ✓ |

---

## State Callbacks

```kotlin
// React to state transitions
LaunchedEffect(step) {
    when (step) {
        Step.DONE -> navigator.navigate("success")
    }
}

// Avoid reading transient state in callbacks; use snapshotFlow for streams
val events = remember { Channel<Event>(Channel.CONFLATED) }
LaunchedEffect(events) {
    for (event in events) handleEvent(event)
}
```
