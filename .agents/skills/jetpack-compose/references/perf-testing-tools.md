# Compose — Performance, Testing, Tools, System & Glance

Ref: https://developer.android.com/develop/ui/compose/performance
     https://developer.android.com/develop/ui/compose/testing
     https://developer.android.com/develop/ui/compose/tooling
     https://developer.android.com/develop/ui/compose/system
     https://developer.android.com/develop/ui/compose/glance

---

## Performance

### Understand the 3 phases
Read `references/architecture-state.md` § Phases. Performance means avoiding unnecessary work in each phase.

### Stability — key to smart recomposition
```kotlin
// Unstable (forces recompose every time parent recomposes)
data class User(val name: String, val list: List<Item>)  // List is unstable

// Stable options:
@Immutable
data class User(val name: String, val items: ImmutableList<Item>)  // kotlinx.collections.immutable

// Or annotate
@Stable
class MyState { var value by mutableStateOf(0) }

// Diagnose: build with Compose Compiler Metrics
// Add to build.gradle.kts:
kotlinOptions {
    freeCompilerArgs += listOf(
        "-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir.absolutePath}/compose_metrics",
        "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.buildDir.absolutePath}/compose_metrics"
    )
}
// Output: *_composables.txt, *_classes.txt — shows stability per class
```

### Strong skipping mode (Compose 1.7+)
```kotlin
// Allows skipping unstable composables when reference equality holds
// Enable in gradle.properties:
// composeCompiler.enableStrongSkippingMode=true
// WARNING: verify with tests; changes recomposition behavior
```

### Defer state reads
```kotlin
// Read in draw/layout modifier lambdas, not in composable body
// BAD — reads in composition, causes recompose
val offset = scrollState.value
Box(Modifier.offset(y = offset.dp))

// GOOD — reads in layout phase, skips composition phase
Box(Modifier.offset { IntOffset(0, scrollState.value) })

// GOOD — graphicsLayer reads in draw phase
Box(Modifier.graphicsLayer { translationY = scrollState.value.toFloat() })
```

### Avoid lambda allocations
```kotlin
// BAD — new lambda on every recompose
LazyColumn { items(list) { item -> ItemRow(item, onClick = { handleClick(item) }) } }

// GOOD — stable lambda
val onClick: (Item) -> Unit = remember { { item -> handleClick(item) } }
LazyColumn { items(list) { item -> ItemRow(item, onClick) } }
```

### derivedStateOf — prevent over-recomposition
```kotlin
// BAD — recomposes on every scroll pixel
val showFab = scrollState.value > 0

// GOOD — recomposes only when threshold crosses
val showFab by remember { derivedStateOf { scrollState.value > 100 } }
```

### LazyList keys & contentType
```kotlin
LazyColumn {
    items(
        items = list,
        key = { item -> item.id },                // stable identity → better diffing
        contentType = { item -> item.type }       // type hint → better ViewHolder recycling
    ) { item -> ItemRow(item) }
}
```

### Baseline Profiles
```kotlin
// Generate profile (reduces JIT startup time by ~30%)
implementation("androidx.profileinstaller:profileinstaller:1.3.x")

// In app/src/androidTest:
class AppBaselineProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test fun generateBaselineProfile() = rule.collect("com.example.app") {
        pressHome()
        startActivityAndWait()
        // user journey steps
    }
}
// Run: ./gradlew generateBaselineProfile
```

### Perf tooling
- Android Studio Profiler → System Trace → look for `Choreographer#doFrame`, `Recomposer`
- Compose-specific trace: `Modifier.traceRecompose()` (custom)
- Enable recomposition highlighting: Layout Inspector → Enable composition count overlay
- `Modifier.testTag("x")` for profiler labeling

---

## UI Testing

Ref: https://developer.android.com/develop/ui/compose/testing

### Setup
```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

### Basic test structure
```kotlin
@RunWith(AndroidJUnit4::class)
class MyComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    // or createAndroidComposeRule<MainActivity>() for real activity

    @Test
    fun myTest() {
        composeTestRule.setContent {
            MaterialTheme { MyComposable() }
        }

        // Find
        composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithTag("submit_button").assertIsEnabled()
        composeTestRule.onAllNodesWithText("Item").assertCountEquals(3)

        // Find by semantics
        composeTestRule.onNode(
            hasText("Hello") and hasClickAction()
        ).assertIsDisplayed()

        // Interactions
        composeTestRule.onNodeWithTag("text_field").performTextInput("Hello")
        composeTestRule.onNodeWithTag("text_field").performTextClearance()
        composeTestRule.onNodeWithTag("list").performScrollToIndex(10)
        composeTestRule.onNodeWithTag("swipeable").performTouchInput { swipeLeft() }

        // Assertions
        composeTestRule.onNodeWithText("Label").assertTextEquals("Label")
        composeTestRule.onNodeWithTag("checkbox").assertIsToggleable()
        composeTestRule.onNodeWithTag("checkbox").assertIsOn()
        composeTestRule.onNodeWithTag("item").assertDoesNotExist()

        // Capture for debugging
        composeTestRule.onRoot().printToLog("MY_TAG")
    }
}
```

### Semantics matchers
```kotlin
// Finders
onNodeWithText(text, substring = false, ignoreCase = false)
onNodeWithContentDescription(label)
onNodeWithTag(testTag)
onNode(matcher)
onAllNodes(matcher)
onFirst() / onLast()
onChildren()
onChild()
onParent()
onSibling()

// Matchers (combine with and, or, not)
hasText("Hello")
hasContentDescription("Back")
hasTestTag("my_tag")
hasClickAction()
isDisplayed()
isEnabled() / isNotEnabled()
isFocused()
isSelected()
isOn() / isOff()             // toggleable
hasScrollAction()
isRoot()
hasAnyChild(matcher)
hasAnyAncestor(matcher)
```

### Synchronization
```kotlin
// Compose test auto-idles — waits for recomposition, animations, coroutines
// Manual wait for condition:
composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule.onAllNodesWithText("Loaded").fetchSemanticsNodes().isNotEmpty()
}

// Disable animations for stable tests
@get:Rule val composeTestRule = createComposeRule().apply {
    mainClock.autoAdvance = false   // manual clock
}
composeTestRule.mainClock.advanceTimeBy(1000L)
```

### Testing with ViewModel / Coroutines
```kotlin
@get:Rule val coroutineRule = TestCoroutineRule()

@Test fun test() {
    val fakeRepo = FakeRepository()
    val vm = MyViewModel(fakeRepo)
    composeTestRule.setContent { MyScreen(vm) }
    // ...
}
```

### Screenshot testing
```kotlin
// Paparazzi (offline, fast)
// implementation("app.cash.paparazzi:paparazzi:1.x")
@get:Rule val paparazzi = Paparazzi()
@Test fun snapshot() { paparazzi.snapshot { MyComposable() } }
```

---

## Tooling

Ref: https://developer.android.com/develop/ui/compose/tooling

### Previews
```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", fontScale = 1.5f)
@Preview(name = "Tablet", device = Devices.TABLET)
@Preview(name = "Phone", device = Devices.PHONE)
@Preview(name = "Landscape", device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape")
@Composable
fun MyComposablePreview() {
    AppTheme { MyComposable(data = previewData()) }
}

// Multi-preview annotation (reusable)
@Preview(name = "1. Phone",   device = Devices.PHONE)
@Preview(name = "2. Foldable", device = Devices.FOLDABLE)
@Preview(name = "3. Tablet",  device = Devices.TABLET)
annotation class DevicePreviews

@DevicePreviews @Composable fun Prev() { AppTheme { MyComposable() } }

// PreviewParameterProvider (multiple data states)
class UserPreviewProvider : PreviewParameterProvider<User> {
    override val values = sequenceOf(User("Alice"), User("Bob"))
}
@Preview @Composable
fun Preview(@PreviewParameter(UserPreviewProvider::class) user: User) {
    UserCard(user)
}
```

### Interactive preview & animation preview
- Click **▶** in preview gutter → Interactive mode (clickable, scrollable)
- Click **🎬** → Animation Inspector (timeline, value graphs for `animate*AsState`, `updateTransition`)

### Iterative development
- **Live Edit** — code changes reflect in preview/emulator without rebuild (simple changes)
- **Apply Changes** — hot-swap code changes (no restart)
- **Apply Code Changes** — swap code only (fastest, limited)

### Editor actions
- Alt+Enter on `@Composable` → "Wrap with widget", "Extract composable", "Add preview"
- Surround with `Column`, `Row`, `Box` from context menu
- Import Material icons inline

### Lint rules
```kotlin
// Built-in Compose lint checks:
// - MissingModifierDefaultValue: exported composable should have Modifier param with default
// - ModifierParameter: Modifier should be first optional param named "modifier"
// - ComposableLambdaParameterPosition: last param should be trailing lambda
// - RememberInComposition: remember missing for expensive objects
// Run: ./gradlew lint
```

### Tracing
```kotlin
// Add trace sections for profiler
Modifier.composed {
    trace("MyComposable") { this }
}

// Or use inline:
trace("heavy_operation") { doHeavyWork() }

// Compose tracing library (shows composable names in System Trace)
implementation("androidx.compose.runtime:runtime-tracing:1.0.0-beta01")
```

### Stack traces
Compose stack traces can be noisy. Tips:
- Filter to your package in Logcat
- Use "Hide Compose internals" in Android Studio stack trace view
- Use `trace {}` to annotate frames in Profiler

---

## System Capabilities

### Edge-to-edge & Window Insets
```kotlin
// In Activity.onCreate()
enableEdgeToEdge()   // androidx.activity:activity:1.8+

// Apply insets to avoid content behind system bars
Modifier.systemBarsPadding()           // status + nav bars
Modifier.statusBarsPadding()
Modifier.navigationBarsPadding()
Modifier.imePadding()                  // keyboard IME
Modifier.safeDrawingPadding()          // safe area for drawing
Modifier.safeContentPadding()

// Manual insets
val insets = WindowInsets.systemBars
val topPadding = with(LocalDensity.current) { insets.getTop(this).toDp() }

// WindowInsets in Scaffold (auto-applied)
Scaffold(
    modifier = Modifier.fillMaxSize()   // no extra padding needed with scaffold
) { padding ->
    LazyColumn(Modifier.padding(padding)) { ... }   // padding includes insets
}

// Keyboard animations (IME follows keyboard)
Modifier.imeNestedScroll()   // content scrolls when keyboard appears
```

### WindowInsetsRulers (API 35+)
```kotlin
// Rulers define layout boundaries relative to window
val rulers = LocalWindowInsetsRulers.current
// More stable than insets for custom views
```

### Display cutouts
```kotlin
// Allow content to draw into cutout area
WindowCompat.getInsetsController(window, view).apply {
    isAppearanceLightStatusBars = false
}
// In Manifest:
// android:windowLayoutInDisplayCutoutMode="shortEdges"

// Query cutout bounds
val cutouts = WindowInsets.displayCutout
val cutoutPadding = Modifier.displayCutoutPadding()
```

### Predictive Back
```kotlin
// Auto-handled by NavController for back navigation
// Custom back handler:
BackHandler(enabled = hasUnsavedChanges) {
    showDiscardDialog = true
}

// Predictive back progress (animate during back gesture)
val backCallback = remember {
    object : OnBackPressedCallback(true) {
        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            progress = backEvent.progress          // 0.0–1.0
            swipeEdge = backEvent.swipeEdge        // EDGE_LEFT or EDGE_RIGHT
        }
        override fun handleOnBackPressed() { navController.popBackStack() }
        override fun handleOnBackCancelled() { progress = 0f }
    }
}
LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    ?.addCallback(lifecycleOwner, backCallback)
```

### Picture-in-Picture
```kotlin
// Enter PiP
val pipParams = PictureInPictureParams.Builder()
    .setAspectRatio(Rational(16, 9))
    .setSourceRectHint(rect)              // transition hint
    .setAutoEnterEnabled(true)            // API 31+
    .build()
activity.enterPictureInPictureMode(pipParams)

// Detect PiP state in Compose
val pipMode by activity.pipMode.collectAsStateWithLifecycle()
if (pipMode) PipContent() else FullContent()

// React to PiP lifecycle
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_USER_LEAVE_HINT) {
            activity.enterPictureInPictureMode(pipParams)
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

---

## Migrate to Compose

### Strategy
1. Screen-by-screen migration (not component-by-component)
2. Use `ComposeView` to embed Compose in existing XML layouts
3. Use `AndroidView` / `AndroidViewBinding` to embed XML Views in Compose
4. Migrate state management to ViewModel first

### ComposeView (Compose in XML)
```kotlin
// In XML layout:
// <androidx.compose.ui.platform.ComposeView android:id="@+id/compose_view" ... />

// In Fragment/Activity:
binding.composeView.setContent {
    MaterialTheme { MyComposable() }
}
```

### AndroidView (XML in Compose)
```kotlin
AndroidView(
    factory = { context ->
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map -> configureMap(map) }
        }
    },
    update = { mapView -> updateMap(mapView, currentLocation) },
    modifier = Modifier.fillMaxSize()
)

// ViewBinding
AndroidViewBinding(ItemBinding::inflate) { root, _, _ ->
    root.title.text = "Hello"
}
```

### Common migration scenarios
```kotlin
// RecyclerView → LazyColumn (direct 1:1 conceptual mapping)
// CoordinatorLayout → Scaffold + scrollBehavior
// Fragment → composable destination in NavHost
// ConstraintLayout → Column/Row/Box or ConstraintLayout compose artifact
//   implementation("androidx.constraintlayout:constraintlayout-compose:1.1.x")

ConstraintLayout {
    val (image, title, body) = createRefs()
    Image(modifier = Modifier.constrainAs(image) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
    })
    Text(modifier = Modifier.constrainAs(title) {
        top.linkTo(image.bottom, margin = 8.dp)
    })
}
```

---

## Glance — App Widgets

Ref: https://developer.android.com/develop/ui/compose/glance

```kotlin
implementation("androidx.glance:glance-appwidget:1.1.x")
implementation("androidx.glance:glance-material3:1.1.x")

// Widget class
class MyWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { MyWidgetContent() }
    }
}

// Widget content
@Composable
fun MyWidgetContent() {
    val context = LocalContext.current
    GlanceTheme {
        Column(
            modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hello Widget", style = TextStyle(color = GlanceTheme.colors.onBackground))
            Button("Refresh", onClick = actionRunCallback<RefreshAction>())
            Image(provider = ImageProvider(R.drawable.icon), contentDescription = null)
        }
    }
}

// Action callback
class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        MyWidget().update(context, glanceId)
    }
}

// Widget receiver
class MyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MyWidget()
}

// Manifest:
// <receiver android:name=".MyWidgetReceiver" android:exported="true">
//     <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE"/></intent-filter>
//     <meta-data android:name="android.appwidget.provider" android:resource="@xml/my_widget_info"/>
// </receiver>

// Glance-specific differences from Compose:
// - GlanceModifier (not Modifier) — limited subset
// - No remember, no LazyColumn (use Column/Row with fixed items)
// - No animations
// - Layout updated via RemoteViews bridge
// - Use LocalGlanceId, LocalContext, LocalSize

// Pin widget in-app (API 26+)
val widgetManager = AppWidgetManager.getInstance(context)
widgetManager.requestPinAppWidget(ComponentName(context, MyWidgetReceiver::class.java), null, null)

// Testing Glance
GlanceAppWidgetRule().apply {
    runTest {
        rule.onGlance(R.id.text_view) { assertTextEquals("Hello Widget") }
    }
}
```

---

## Style Guidelines

### API Guidelines Summary
```kotlin
// 1. Modifier as first optional parameter, default = Modifier
@Composable
fun MyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,            // ← correct position & default
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit  // ← trailing lambda last
)

// 2. Slot API pattern for flexible content
@Composable
fun Card(
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,   // optional slot
    content: @Composable () -> Unit             // required slot
)

// 3. State hoisting — don't own state you don't need to
// 4. Emit single root node from composable
// 5. Use @Immutable/@Stable for parameters
// 6. Name composables as nouns (Button, not ShowButton)
// 7. Name composable returning values as normal functions (rememberXxx for remembered values)
```

### Kotlin for Compose
```kotlin
// Default arguments over multiple overloads
// Trailing lambdas for content slots
// Extension functions for composable utilities
// Value classes for semantic wrappers (e.g., ContentScale)
// Inline functions for high-performance composable helpers
// Use object for singletons of composable defaults (e.g., ButtonDefaults)
```
