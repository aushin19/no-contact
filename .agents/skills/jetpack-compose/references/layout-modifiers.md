# Compose — Layout & Modifiers

Ref: https://developer.android.com/develop/ui/compose/layouts
     https://developer.android.com/develop/ui/compose/modifiers
     https://developer.android.com/develop/ui/compose/lists
     https://developer.android.com/develop/ui/compose/layouts/adaptive
     https://developer.android.com/develop/ui/compose/layouts/custom

---

## Layout Basics

### Standard containers
```kotlin
Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) { /* children stacked vertically */ }

Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) { /* children side by side */ }

Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.BottomEnd
) {
    Image(...)
    FloatingActionButton(...) // overlaid, bottom-end
}
```

### Arrangement options
```kotlin
Arrangement.Top / Bottom / Start / End / Center
Arrangement.SpaceBetween   // space between children, none at edges
Arrangement.SpaceAround    // equal space around each child
Arrangement.SpaceEvenly    // equal space between AND at edges
Arrangement.spacedBy(8.dp) // fixed gap between children
```

### Weight in Row/Column
```kotlin
Row {
    Text("Label", modifier = Modifier.weight(1f))   // takes remaining space
    Icon(...)  // fixed size
}
```

---

## Modifiers

Modifiers are **ordered** — sequence affects behavior.

```kotlin
// Padding BEFORE background → padding has background color
Modifier.background(Color.Blue).padding(16.dp)

// Padding AFTER background → padding is outside the colored area
Modifier.padding(16.dp).background(Color.Blue)
```

### Size modifiers
```kotlin
Modifier.fillMaxSize()         // match parent
Modifier.fillMaxWidth()
Modifier.fillMaxHeight()
Modifier.fillMaxWidth(0.5f)    // fraction of parent
Modifier.size(48.dp)
Modifier.width(200.dp)
Modifier.height(48.dp)
Modifier.widthIn(min = 48.dp, max = 200.dp)
Modifier.wrapContentSize()     // size to content
Modifier.defaultMinSize(minWidth = 48.dp)
Modifier.requiredSize(100.dp)  // ignores parent constraints
```

### Spacing & positioning
```kotlin
Modifier.padding(16.dp)
Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
Modifier.offset(x = 8.dp, y = 4.dp)
Modifier.offset { IntOffset(scrollState.value, 0) } // deferred — layout phase
Modifier.absoluteOffset(x = 8.dp)
```

### Drawing
```kotlin
Modifier.background(Color.Blue)
Modifier.background(brush = Brush.horizontalGradient(listOf(Color.Red, Color.Blue)))
Modifier.clip(RoundedCornerShape(8.dp))
Modifier.clip(CircleShape)
Modifier.clipToBounds()
Modifier.border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
Modifier.alpha(0.5f)
Modifier.shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
Modifier.graphicsLayer { rotationZ = angle }  // hardware-accelerated transforms
Modifier.drawBehind { drawRect(color, ...) }
Modifier.drawWithContent { drawContent(); drawRect(...) }
```

### Interaction
```kotlin
Modifier.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = ripple()
) { onClick() }
Modifier.combinedClickable(onClick = {}, onLongClick = {}, onDoubleClick = {})
Modifier.selectable(selected, onClick = {})
Modifier.toggleable(checked, onCheckedChange = {})
Modifier.pointerInput(Unit) { detectTapGestures { ... } }
```

### Semantics (Accessibility)
```kotlin
Modifier.semantics {
    contentDescription = "Add item"
    role = Role.Button
    stateDescription = if (checked) "Checked" else "Unchecked"
    onClick(label = "Add") { true }
}
Modifier.clearAndSetSemantics { contentDescription = "Custom" }
```

### Custom modifiers
```kotlin
fun Modifier.redBorder() = this.border(2.dp, Color.Red)

// Composed modifier (can use remember, etc.)
fun Modifier.shimmer(): Modifier = composed {
    val anim = rememberInfiniteTransition()
    val alpha by anim.animateFloat(0.2f, 0.9f, infiniteRepeatable(...))
    this.alpha(alpha)
}

// Layout modifier
fun Modifier.customPadding(padding: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.offset(-padding.roundToPx() * 2))
    layout(placeable.width + padding.roundToPx() * 2, placeable.height) {
        placeable.placeRelative(padding.roundToPx(), 0)
    }
}
```

---

## Lists & Grids

### LazyColumn / LazyRow
```kotlin
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    state = rememberLazyListState()
) {
    item { Header() }
    items(items = myList, key = { it.id }) { item ->
        ItemRow(item)
    }
    itemsIndexed(myList) { index, item -> IndexedRow(index, item) }
    items(count = 20) { index -> NumberRow(index) }
}

// Scroll state
val listState = rememberLazyListState()
// Scroll programmatically
LaunchedEffect(target) {
    listState.animateScrollToItem(index = target)
    listState.scrollToItem(index = 0) // instant
}
// Observe scroll
val firstVisible by remember { derivedStateOf { listState.firstVisibleItemIndex } }
```

### LazyVerticalGrid / LazyHorizontalGrid
```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 128.dp),   // auto-fit
    // columns = GridCells.Fixed(3),                  // fixed count
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
) {
    items(photos, key = { it.id }) { photo -> PhotoCard(photo) }
    // Span multiple columns
    item(span = { GridItemSpan(maxLineSpan) }) { FullWidthHeader() }
}
```

### LazyStaggeredGrid
```kotlin
LazyVerticalStaggeredGrid(
    columns = StaggeredGridCells.Adaptive(200.dp)
) {
    items(items) { item -> StaggeredCard(item) }
}
```

### Performance tips
```kotlin
// Always provide stable keys
items(list, key = { it.id }) { ... }

// Avoid creating lambdas in items — hoist them
val onClick: (Item) -> Unit = remember { { item -> onItemClick(item) } }

// Use contentType for heterogeneous lists (better recycling)
items(list, contentType = { it::class }) { ... }

// Avoid nested scrollable in same direction
// Use nestedScroll API if absolutely required
```

---

## Pager

```kotlin
implementation("androidx.compose.foundation:foundation")

val pagerState = rememberPagerState(pageCount = { pages.size })

HorizontalPager(state = pagerState) { page ->
    PageContent(pages[page])
}

VerticalPager(state = pagerState) { page ->
    PageContent(pages[page])
}

// Page indicator
HorizontalPagerIndicator(pagerState = pagerState)

// Programmatic scroll
LaunchedEffect(Unit) { pagerState.animateScrollToPage(2) }
val currentPage = pagerState.currentPage
```

---

## Flow Layouts

```kotlin
implementation("androidx.compose.foundation:foundation")

FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    maxItemsInEachRow = 4
) {
    tags.forEach { tag -> Chip(label = { Text(tag) }, onClick = {}) }
}

FlowColumn { ... }
```

---

## Custom Layouts

```kotlin
@Composable
fun MyCustomLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val width = constraints.maxWidth
        val height = placeables.sumOf { it.height }
        layout(width, height) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x = 0, y = y)
                y += placeable.height
            }
        }
    }
}
```

### SubcomposeLayout (measure content based on other content)
```kotlin
SubcomposeLayout { constraints ->
    val mainPlaceables = subcompose("main") { MainContent() }
        .map { it.measure(constraints) }
    val overlayPlaceables = subcompose("overlay") {
        OverlayContent(mainHeight = mainPlaceables.first().height)
    }.map { it.measure(constraints) }
    layout(constraints.maxWidth, constraints.maxHeight) {
        mainPlaceables.forEach { it.placeRelative(0, 0) }
        overlayPlaceables.forEach { it.placeRelative(0, 0) }
    }
}
```

### Alignment lines
```kotlin
// Custom alignment line
val TextBaseline = HorizontalAlignmentLine(merger = { old, new -> min(old, new) })

// Use in layout
layout(width, height, alignmentLines = mapOf(TextBaseline to baselineY)) { ... }

// Align by line in Row
Row {
    Text("Label", modifier = Modifier.alignBy(FirstBaseline))
    Text("Value", modifier = Modifier.alignBy(FirstBaseline))
}
```

---

## Adaptive Layouts

### Window size classes
```kotlin
implementation("androidx.compose.material3.adaptive:adaptive")

val windowSizeClass = calculateWindowSizeClass(activity)
// .widthSizeClass: Compact / Medium / Expanded
// .heightSizeClass: Compact / Medium / Expanded

when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.Compact  -> PhoneLayout()
    WindowWidthSizeClass.Medium   -> TabletLayout()
    WindowWidthSizeClass.Expanded -> DesktopLayout()
}
```

### Canonical layouts
```kotlin
// List-Detail (NavigableListDetailPaneScaffold)
NavigableListDetailPaneScaffold(
    navigator = navigator,
    listPane = { ListPane(onSelect = { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }) },
    detailPane = { DetailPane(item = selectedItem) }
)

// Supporting pane (SupportingPaneScaffold)
SupportingPaneScaffold(
    directive = calculatePaneScaffoldDirective(windowAdaptiveInfo),
    value = scaffoldState,
    mainPane = { MainContent() },
    supportingPane = { SupportingContent() }
)
```

### Foldables
```kotlin
implementation("androidx.window:window")

val foldingFeatures = WindowInfoTracker.getOrCreate(context)
    .windowLayoutInfo(activity)
    .map { it.displayFeatures.filterIsInstance<FoldingFeature>() }

// In Compose:
val windowInfo = currentWindowAdaptiveInfo()
val isSeparating = windowInfo.windowPosture.isTabletop
```

### MediaQuery equivalent
```kotlin
val configuration = LocalConfiguration.current
val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
val screenWidthDp = configuration.screenWidthDp.dp
```

---

## Intrinsic Measurements

Force min/max intrinsic size when children need to size themselves relative to siblings:

```kotlin
// Makes Row height = tallest child's intrinsic height
Row(modifier = Modifier.height(IntrinsicSize.Min)) {
    Text("Hello", modifier = Modifier.fillMaxHeight())
    Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
    Text("World", modifier = Modifier.fillMaxHeight())
}
// Use sparingly — O(n) extra measurement passes
```

---

## Visibility & Scroll Tracking

```kotlin
// Track item visibility in LazyList
val listState = rememberLazyListState()
val visibleItems = listState.layoutInfo.visibleItemsInfo

// Modifier for visibility
Modifier.onGloballyPositioned { coords ->
    val bounds = coords.boundsInWindow()
    val isVisible = bounds.overlaps(windowBounds)
}
```
