---
name: jetpack-compose
description: >
  Comprehensive Jetpack Compose reference skill covering all official Android documentation sections.
  Use this skill whenever the user asks about Jetpack Compose, Compose UI, @Composable functions,
  state management, recomposition, Compose layouts, Compose components, Material3, Compose animation,
  Compose navigation, Compose testing, Compose performance, Compose accessibility, touch/gesture input,
  Compose tooling, window insets, Glance widgets, or migrating from XML Views to Compose.
  Also trigger for questions about: LazyColumn, LazyRow, Modifier, remember, mutableStateOf,
  CompositionLocal, Side effects (LaunchedEffect, DisposableEffect, SideEffect), ViewModel in Compose,
  shared element transitions, predictive back, edge-to-edge, stylus/Ink API, or any Compose API.
  Always use this skill — even for partial Compose questions like "how do I handle gestures in Compose"
  or "what's the difference between remember and rememberSaveable".
---

# Jetpack Compose — Master Skill

Authoritative reference for all Jetpack Compose topics, mapped to official Android documentation.
Read this file first, then load the relevant reference file(s) for deep detail.

---

## Reference File Index

| Domain | File | Covers |
|--------|------|--------|
| UI Architecture & State | `references/architecture-state.md` | Lifecycle, recomposition, phases, side-effects, state, hoisting, CompositionLocal, Navigation |
| Layout & Modifiers | `references/layout-modifiers.md` | Row/Column/Box, Modifier chain, lists, grids, pager, flow, custom layouts, adaptive, foldables |
| Components | `references/components.md` | All 30+ M3 components: buttons, sheets, dialogs, nav, scaffold, pickers, chips, cards, etc. |
| Theming, Text & Graphics | `references/theming-text-graphics.md` | Material3 theme, typography, custom theme, text styling, images, brush, shapes, animations |
| Touch, Input & Accessibility | `references/touch-input-accessibility.md` | Gestures, scroll, keyboard, focus, stylus, Ink API, drag-drop, a11y semantics |
| Performance, Testing & Tools | `references/perf-testing-tools.md` | Stability, baseline profiles, UI testing APIs, previews, lint, tracing, Glance widgets, system capabilities |

**Load rule:** Read the relevant reference file(s) before answering any non-trivial Compose question.
For questions spanning multiple domains, load multiple files.

---

## Mental Model — Thinking in Compose

Compose is **declarative**: describe UI as a function of state, not imperative mutations.

```
State → Composable(state) → UI
User event → update state → recomposition → new UI
```

**Three phases per frame:**
1. **Composition** — runs `@Composable` functions, builds UI tree
2. **Layout** — measures and places nodes
3. **Draw** — renders to canvas

Smart recomposition skips composables whose inputs haven't changed. Design composables to be **stable** and **side-effect-free**.

---

## Setup & BOM

```kotlin
// build.gradle.kts (app)
val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
implementation(composeBom)
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")
debugImplementation("androidx.compose.ui:ui-tooling")

// Compiler plugin (separate from BOM)
// build.gradle.kts (app) — with AGP 8.x
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.15"
}
```

BOM → library mapping: https://developer.android.com/develop/ui/compose/bom/bom-mapping

---

## Core Patterns Quick Reference

### Minimal composable
```kotlin
@Composable
fun Greeting(name: String) {
    Text(text = "Hello, $name!")
}
```

### State
```kotlin
var count by remember { mutableStateOf(0) }
val savedCount by rememberSaveable { mutableStateOf(0) } // survives rotation
```

### State hoisting pattern
```kotlin
// Stateless (testable, reusable)
@Composable
fun Counter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) { Text("$count") }
}
// Stateful caller owns state
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }
    Counter(count, onIncrement = { count++ })
}
```

### Side effects
```kotlin
LaunchedEffect(key)      // coroutine, cancels on key change
DisposableEffect(key)    // cleanup on leave/key change
SideEffect { }           // runs after every successful recomposition
rememberCoroutineScope() // scope tied to composition lifetime
```

### ViewModel integration
```kotlin
@Composable
fun MyScreen(vm: MyViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
}
```

---

## Modifier Quick Reference

Order matters — modifiers apply outside-in for layout, inside-out for drawing.

```kotlin
Modifier
    .fillMaxSize()           // size
    .padding(16.dp)          // spacing
    .background(Color.Blue)  // draw behind
    .clip(RoundedCornerShape(8.dp))
    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
    .clickable { }
    .semantics { contentDescription = "..." }  // a11y
```

---

## Layout Quick Reference

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { ... }
Row(horizontalArrangement = Arrangement.SpaceBetween) { ... }
Box(contentAlignment = Alignment.Center) { ... }

// Lazy
LazyColumn { items(list) { item -> ItemRow(item) } }
LazyRow   { itemsIndexed(list) { index, item -> ... } }
LazyVerticalGrid(columns = GridCells.Adaptive(120.dp)) { ... }

// Pager
HorizontalPager(count = pages.size) { page -> PageContent(page) }
```

---

## Navigation Quick Reference

```kotlin
// Setup
val navController = rememberNavController()
NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("detail/{id}") { backStack ->
        val id = backStack.arguments?.getString("id")
        DetailScreen(id)
    }
}
// Navigate
navController.navigate("detail/42")
navController.popBackStack()
```

---

## Entry Points to Official Docs

| Topic | URL |
|-------|-----|
| Compose landing | https://developer.android.com/develop/ui/compose/documentation |
| BOM versions | https://developer.android.com/develop/ui/compose/bom/bom-mapping |
| API reference | https://developer.android.com/reference/kotlin/androidx/compose/ui/package-summary |
| Compose samples | https://developer.android.com/develop/ui/compose/samples |
| Quick guides | https://developer.android.com/develop/ui/compose/quick-guides |
| Compose releases | https://developer.android.com/jetpack/androidx/releases/compose |
| API guidelines | https://developer.android.com/develop/ui/compose/api-guidelines |

---

Load the appropriate reference file(s) now based on the user's question.
