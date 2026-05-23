---
name: jetpack-compose-samples
description: >
  Guide for working with official Jetpack Compose UI samples. Use this skill whenever a user asks
  about Compose UI implementation, wants to understand how a specific Compose pattern works, asks
  "how do I build X in Compose", references any official sample app (JetNews, Jetchat, Jetsnack,
  Jetcaster, Reply, JetLagged, Now in Android), wants working code for Compose layouts/animations/
  theming/navigation/state/testing, or asks to clone/adapt any Compose sample. Also trigger for:
  "show me a Compose example of X", "what sample demonstrates Y", "how does Jetsnack do Z",
  "Compose beginner sample", "production Compose architecture", "Material 3 in Compose",
  "adaptive layout Compose", "custom drawing Compose", "Compose with ViewModel/Hilt/Room".
  Always use this skill — even partial queries like "compose list example" or "compose animation".
---

# Jetpack Compose Samples Skill

Working samples that demonstrate how to create UI using Compose.  
All samples: Kotlin + Jetpack Compose. Clone via Android Studio.

**Full catalog → read:** `references/samples-catalog.md`

---

## Quick Sample Picker

| Need | Use Sample |
|------|-----------|
| First Compose app, simple patterns | **Jetchat** (low complexity) |
| Material app + architecture | **JetNews** (medium) |
| Adaptive phone/tablet/foldable UI | **Reply** (medium) |
| Custom design system + animations | **Jetsnack** (medium-high) |
| Media player + LazyGrid + Hilt | **Jetcaster** (high) |
| Custom Canvas drawing + layouts | **JetLagged** (advanced) |
| Production multi-module architecture | **Now in Android** (expert) |
| Every M3 component catalogued | **Material Catalog** (AOSP) |

---

## How to Use a Sample

```bash
# Clone all official samples
git clone https://github.com/android/compose-samples

# Or clone Now in Android separately
git clone https://github.com/android/nowinandroid
```

Open in Android Studio → File → Open → select sample folder.  
Min Android Studio: Hedgehog (2023.1.1) or later.  
Min SDK: 21. Target SDK: 35+.

---

## Core Patterns with Code

### 1. Basic Composable + State (Jetchat-style)

```kotlin
@Composable
fun MessageInput(onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message") }
        )
        IconButton(onClick = {
            onSend(text)
            text = ""
        }) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}
```

**Key:** State hoisted via lambda. `rememberSaveable` survives rotation.

---

### 2. LazyColumn List (JetNews-style)

```kotlin
@Composable
fun PostList(posts: List<Post>, onPostClick: (Post) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            PostCard(post = post, onClick = { onPostClick(post) })
        }
    }
}

@Composable
fun PostCard(post: Post, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(post.subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
```

**Key:** Always provide `key` to `items()` — prevents recomposition bugs.

---

### 3. Scaffold + Navigation (JetNews-style)

```kotlin
@Composable
fun JetNewsApp() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("JetNews") })
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("article/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")
                ArticleScreen(id)
            }
        }
    }
}
```

---

### 4. ViewModel + StateFlow (standard pattern)

```kotlin
class HomeViewModel @Inject constructor(
    private val repo: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getPosts().collect { posts ->
                _uiState.update { it.copy(posts = posts) }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PostList(posts = uiState.posts, onPostClick = { /* nav */ })
}
```

---

### 5. Adaptive Layout (Reply-style)

```kotlin
@Composable
fun ReplyApp(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            ReplyWithBottomNavBar()
        }
        WindowWidthSizeClass.Medium -> {
            ReplyWithNavigationRail()
        }
        WindowWidthSizeClass.Expanded -> {
            ReplyWithPermanentDrawer()
        }
    }
}
```

**Dependency:**
```kotlin
implementation("androidx.compose.material3:material3-window-size-class:$m3Version")
```

---

### 6. Custom Canvas Drawing (JetLagged-style)

```kotlin
@Composable
fun SleepBar(sleepData: List<SleepPeriod>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        sleepData.forEach { period ->
            val startX = period.startFraction * size.width
            val endX = period.endFraction * size.width
            val top = period.type.yFraction * size.height

            drawRoundRect(
                color = period.type.color,
                topLeft = Offset(startX, top),
                size = Size(endX - startX, size.height * 0.2f),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
        }
    }
}
```

---

### 7. Animation (Jetsnack-style)

```kotlin
@Composable
fun AddToCartButton(expanded: Boolean, onClick: () -> Unit) {
    val width by animateDpAsState(
        targetValue = if (expanded) 200.dp else 48.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_width"
    )

    Button(
        onClick = onClick,
        modifier = Modifier.width(width)
    ) {
        AnimatedVisibility(visible = expanded) {
            Text("Add to Cart", modifier = Modifier.padding(end = 8.dp))
        }
        Icon(Icons.Default.ShoppingCart, contentDescription = null)
    }
}
```

---

### 8. Custom Theme System (Jetsnack-style)

```kotlin
// Custom color system — not relying on MaterialTheme
object JetsnackTheme {
    val colors: JetsnackColors
        @Composable get() = LocalJetsnackColors.current
}

val LocalJetsnackColors = staticCompositionLocalOf { LightJetsnackColors }

@Composable
fun JetsnackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkJetsnackColors else LightJetsnackColors
    CompositionLocalProvider(LocalJetsnackColors provides colors) {
        MaterialTheme(content = content)
    }
}
```

---

### 9. UI Testing (JetNews / Jetchat pattern)

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_showsPosts() {
        composeTestRule.setContent {
            JetNewsTheme {
                HomeScreen(posts = fakePosts)
            }
        }

        composeTestRule
            .onNodeWithText("Jetpack Compose is stable")
            .assertIsDisplayed()
    }

    @Test
    fun postCard_click_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            PostCard(post = fakePost, onClick = { clicked = true })
        }

        composeTestRule.onNodeWithText(fakePost.title).performClick()
        assert(clicked)
    }
}
```

---

## Common Gotchas

| Problem | Fix |
|---------|-----|
| Recomposition loop | Check `remember` — missing it recreates state every frame |
| List flickers on update | Add `key = { item.id }` to `items()` |
| State lost on rotation | Use `rememberSaveable` or hoist to ViewModel |
| Custom layout broken | Implement `MeasurePolicy` — don't use `fillMaxSize` inside custom layout |
| Slow scrolling | Use `LazyColumn`, not `Column` + `verticalScroll` for long lists |
| Preview not working | Ensure `@Preview` composable has no required params or uses `PreviewParameter` |
| Dark theme ignored | Check custom theme wraps `MaterialTheme` correctly |

---

## Dependency Reference

```kotlin
// Core Compose
implementation(platform("androidx.compose:compose-bom:2024.12.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.5")

// Adaptive / Window Size
implementation("androidx.compose.material3:material3-window-size-class")
implementation("androidx.compose.material3.adaptive:adaptive:1.0.0")

// ViewModel + Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

// Hilt
implementation("com.google.dagger:hilt-android:2.52")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// Testing
testImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-tooling")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

---

## References

- **Samples page:** https://developer.android.com/develop/ui/compose/samples
- **GitHub (all samples):** https://github.com/android/compose-samples
- **Now in Android:** https://github.com/android/nowinandroid
- **Compose docs:** https://developer.android.com/develop/ui/compose/documentation
- **Quick guides:** https://developer.android.com/develop/ui/compose/quick-guides
- **Codelabs:** https://developer.android.com/courses/pathways/compose
- **API reference:** https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary
- **Material 3:** https://m3.material.io/develop/android/jetpack-compose
- **Compose BOM versions:** https://developer.android.com/develop/ui/compose/bom/bom-mapping

> Full sample breakdown with features, APIs, and skill-level mapping → `references/samples-catalog.md`
