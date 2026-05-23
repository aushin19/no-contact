# Compose UI Samples — Reference

**GitHub**: [android/compose-samples](https://github.com/android/compose-samples)
**GitHub**: [android/nowinandroid](https://github.com/android/nowinandroid)
**Docs**: [developer.android.com/develop/ui/compose](https://developer.android.com/develop/ui/compose)

---

## Patterns Covered

1. Scaffold + TopBar + BottomBar
2. LazyColumn / LazyRow
3. State management + hoisting
4. Material 3 theming
5. Animations
6. Adaptive layouts (phone/tablet/foldable)
7. Side effects
8. Custom layouts

---

## 1. Scaffold + TopBar + BottomBar

```kotlin
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My App") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* settings */ }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { navController.navigate(item.route) },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* action */ },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Add Task") }
            )
        }
    ) { paddingValues ->
        // Content
        NavHost(navController, modifier = Modifier.padding(paddingValues)) { ... }
    }
}
```

---

## 2. LazyColumn / LazyRow

```kotlin
// Basic list
@Composable
fun TaskList(tasks: List<Task>, onDelete: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskItem(
                task = task,
                onDelete = { onDelete(task.id) },
                modifier = Modifier.animateItem() // animate add/remove
            )
        }
    }
}

// Sticky headers
LazyColumn {
    tasksByDate.forEach { (date, tasks) ->
        stickyHeader { DateHeader(date) }
        items(tasks, key = { it.id }) { TaskItem(it) }
    }
}

// Grid
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(items) { PhotoItem(it) }
}
```

---

## 3. State Management

```kotlin
// Local state
var expanded by remember { mutableStateOf(false) }

// Derived state (avoids unnecessary recompositions)
val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

// State hoisting pattern
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
    )
}

// Collecting StateFlow in Compose
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// When expression on sealed state
when (val state = uiState) {
    is TasksUiState.Loading -> CircularProgressIndicator()
    is TasksUiState.Success -> TaskList(state.tasks)
    is TasksUiState.Error -> ErrorMessage(state.message)
}
```

**Deps**:
```kotlin
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.x")
```

---

## 4. Material 3 Theming

```kotlin
// Theme.kt
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// Custom color scheme
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Accessing theme tokens
val primary = MaterialTheme.colorScheme.primary
val headlineSmall = MaterialTheme.typography.headlineSmall
val shape = MaterialTheme.shapes.medium
```

---

## 5. Animations

```kotlin
// Visibility animation
AnimatedVisibility(
    visible = isExpanded,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically()
) {
    ExpandedContent()
}

// Value animation
val alpha by animateFloatAsState(
    targetValue = if (isSelected) 1f else 0.4f,
    animationSpec = tween(300),
    label = "alpha"
)

// Cross-fade between composables
Crossfade(targetState = currentScreen, label = "screen") { screen ->
    when (screen) {
        Screen.Home -> HomeScreen()
        Screen.Profile -> ProfileScreen()
    }
}

// Infinite animation (loading shimmer, pulsing)
val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
val shimmerX by infiniteTransition.animateFloat(
    initialValue = -1000f, targetValue = 1000f,
    animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
    label = "shimmerX"
)
```

---

## 6. Adaptive Layouts

```kotlin
// WindowSizeClass-based layout
@Composable
fun AdaptiveLayout(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> SinglePaneLayout()
        WindowWidthSizeClass.Medium -> TwoPaneLayout(ratio = 0.4f)
        WindowWidthSizeClass.Expanded -> TwoPaneLayout(ratio = 0.3f)
    }
}

// ListDetailPaneScaffold (Compose adaptive)
ListDetailPaneScaffold(
    directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    value = navigator.scaffoldValue,
    listPane = { TaskListPane(onSelect = { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it) }) },
    detailPane = { TaskDetailPane(navigator.currentDestination?.content) }
)
```

**Deps**:
```kotlin
implementation("androidx.compose.material3.adaptive:adaptive:1.0.x")
implementation("androidx.compose.material3:material3-window-size-class:1.3.x")
```

---

## 7. Side Effects

```kotlin
// One-shot effect on composition
LaunchedEffect(key1 = userId) {
    viewModel.loadUser(userId) // runs once per userId change
}

// Show snackbar from ViewModel event
val snackbarHostState = remember { SnackbarHostState() }
LaunchedEffect(Unit) {
    viewModel.snackbarMessage.collect { msg ->
        snackbarHostState.showSnackbar(msg)
    }
}

// DisposableEffect for lifecycle
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

---

## 8. Custom Layouts

```kotlin
@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val colWidths = IntArray(columns) { constraints.maxWidth / columns }
        val colHeights = IntArray(columns) { 0 }
        val placeables = measurables.map { measurable ->
            val col = colHeights.indexOfMin()
            val placeable = measurable.measure(
                constraints.copy(maxWidth = colWidths[col])
            )
            Pair(col, placeable)
        }
        val totalHeight = colHeights.max()
        layout(constraints.maxWidth, totalHeight) {
            val colY = IntArray(columns) { 0 }
            placeables.forEachIndexed { i, (col, placeable) ->
                placeable.placeRelative(x = col * colWidths[col], y = colY[col])
                colY[col] += placeable.height
            }
        }
    }
}
```

---

## Key Notes

- Use `collectAsStateWithLifecycle()` not `collectAsState()` — respects lifecycle
- `key = { item.id }` in `items()` is critical for correct animations + diffing
- `animateItem()` (Compose 1.7+) replaces `animateItemPlacement()`
- `derivedStateOf {}` = memo for Compose state — use when deriving from other state
- Dynamic color (Material You) only available API 31+; always provide fallback
- `@Stable` / `@Immutable` annotations help Compose skip unnecessary recompositions
