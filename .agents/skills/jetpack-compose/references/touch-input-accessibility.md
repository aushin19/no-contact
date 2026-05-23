# Compose — Touch, Input & Accessibility

Ref: https://developer.android.com/develop/ui/compose/touch-input
     https://developer.android.com/develop/ui/compose/touch-input/pointer-input
     https://developer.android.com/develop/ui/compose/touch-input/focus
     https://developer.android.com/develop/ui/compose/touch-input/stylus-input
     https://developer.android.com/develop/ui/compose/accessibility

---

## Pointer Input — Gestures

All gesture APIs live inside `Modifier.pointerInput(key) { ... }`.

### Tap, press, long press
```kotlin
Modifier.pointerInput(Unit) {
    detectTapGestures(
        onTap       = { offset -> /* single tap */ },
        onDoubleTap = { offset -> /* double tap */ },
        onLongPress = { offset -> /* long press */ },
        onPress     = { offset ->
            // called on down; tryAwaitRelease() returns false if cancelled
            val released = tryAwaitRelease()
        }
    )
}

// Simpler: Modifier.clickable + combinedClickable
Modifier.clickable { onClick() }
Modifier.combinedClickable(
    onClick      = { },
    onLongClick  = { },
    onDoubleClick = { }
)
```

### Drag
```kotlin
// Simple drag (no velocity)
Modifier.pointerInput(Unit) {
    detectDragGestures(
        onDragStart  = { offset -> },
        onDrag       = { change, dragAmount ->
            change.consume()
            offset += dragAmount
        },
        onDragEnd    = { },
        onDragCancel = { }
    )
}

// Draggable modifier (single axis)
var offsetX by remember { mutableStateOf(0f) }
Modifier.draggable(
    state = rememberDraggableState { delta -> offsetX += delta },
    orientation = Orientation.Horizontal
)

// Swipeable (anchored draggable — e.g., swipe-to-dismiss)
val state = rememberAnchoredDraggableState(
    initialValue = DragValue.Center,
    anchors = DraggableAnchors {
        DragValue.Start at -300f
        DragValue.Center at 0f
        DragValue.End at 300f
    },
    positionalThreshold = { totalDistance -> totalDistance * 0.5f },
    velocityThreshold = { 125.dp.toPx() },
    animationSpec = spring()
)
Modifier.anchoredDraggable(state, Orientation.Horizontal)
val xOffset = state.requireOffset()
```

### Fling / scroll with velocity
```kotlin
Modifier.pointerInput(Unit) {
    detectDragGesturesAfterLongPress(
        onDragEnd = { /* velocity tracked internally */ }
    )
}

// Or use scrollable + FlingBehavior
val scrollState = rememberScrollState()
Modifier.verticalScroll(scrollState)
Modifier.horizontalScroll(scrollState)
```

### Multi-touch (pinch, rotate)
```kotlin
Modifier.pointerInput(Unit) {
    detectTransformGestures(
        panZoomLock = false,
        onGesture = { centroid, pan, zoom, rotation ->
            scale  *= zoom
            angle  += rotation
            offset += pan
        }
    )
}
```

### Raw pointer events
```kotlin
Modifier.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()    // PointerEvent
            val changes = event.changes        // List<PointerInputChange>
            changes.forEach { change ->
                val position = change.position
                val pressure = change.pressure
                val id       = change.id
                if (change.pressed) change.consume()
            }
        }
    }
}

// Pass only if not consumed
Modifier.pointerInput(Unit) {
    awaitPointerEventScope {
        awaitFirstDown().also { if (!it.isConsumed) handleDown(it) }
    }
}
```

---

## Scroll

### Scroll modifiers
```kotlin
// Simple scroll (non-lazy content)
val scrollState = rememberScrollState()
Column(Modifier.verticalScroll(scrollState)) { /* tall content */ }
Row(Modifier.horizontalScroll(scrollState)) { /* wide content */ }

// Programmatic
LaunchedEffect(Unit) { scrollState.animateScrollTo(scrollState.maxValue) }
val isAtBottom = scrollState.value == scrollState.maxValue
```

### Nested scroll
```kotlin
// Nested scroll with connection
val nestedScrollConnection = remember {
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y
            // consume some, return what was consumed
            val consumed = if (delta < 0) delta else 0f
            return Offset(0f, consumed)
        }
    }
}
Modifier.nestedScroll(nestedScrollConnection)

// CollapsingToolbar pattern
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
```

### Two-dimensional scrolling
```kotlin
// Compose doesn't have built-in 2D scroll; implement via:
Modifier
    .horizontalScroll(horizontalScrollState)
    .verticalScroll(verticalScrollState)
// Note: works on Box/Column, not LazyList (lazy only supports one axis)
```

---

## Keyboard Input

```kotlin
// Handle key events on a focusable composable
Modifier.onKeyEvent { keyEvent ->
    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown) {
        submit(); true   // true = consumed
    } else false
}

Modifier.onPreviewKeyEvent { keyEvent -> /* intercept before children */ false }

// IME actions
TextField(
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(onSearch = { performSearch(text) })
)

// Keyboard shortcuts helper
LocalSoftwareKeyboardController.current?.show()
LocalSoftwareKeyboardController.current?.hide()
```

---

## Focus

```kotlin
// Request focus
val focusRequester = remember { FocusRequester() }
Modifier.focusRequester(focusRequester)
LaunchedEffect(Unit) { focusRequester.requestFocus() }

// Make composable focusable
Modifier.focusable()
Modifier.focusTarget()   // lower-level

// Focus traversal order
Modifier.focusGroup()    // groups children for traversal
Modifier.focusProperties {
    canFocus = true
    next = otherFocusRequester
    previous = prevFocusRequester
    up = upFocusRequester
    down = downFocusRequester
}

// Move focus programmatically
val focusManager = LocalFocusManager.current
focusManager.moveFocus(FocusDirection.Next)
focusManager.moveFocus(FocusDirection.Down)
focusManager.clearFocus()

// React to focus
Modifier.onFocusChanged { state ->
    isFocused = state.isFocused
}
Modifier.onFocusEvent { event -> }
```

---

## User Interaction — Indication & Ripple

```kotlin
// Standard ripple
Modifier.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(bounded = true, radius = 24.dp, color = Color.Blue)
) { }

// No ripple (custom indication)
Modifier.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null
) { }

// Observe interaction state (hover, pressed, focused)
val interactionSource = remember { MutableInteractionSource() }
val isPressed by interactionSource.collectIsPressedAsState()
val isHovered by interactionSource.collectIsHoveredAsState()
val isFocused by interactionSource.collectIsFocusedAsState()
Box(Modifier.clickable(interactionSource, indication = ripple()) { },
    color = if (isPressed) Color.DarkGray else Color.Gray)
```

---

## Drag and Drop

```kotlin
// Source (drag FROM)
Modifier.dragAndDropSource {
    detectTapGestures(onLongPress = { offset ->
        startTransfer(
            DragAndDropTransferData(
                clipData = ClipData.newPlainText("text", "Dragged text"),
                flags = DragAndDropTransferData.DRAG_FLAG_GLOBAL
            )
        )
    })
}

// Target (drop TO)
Modifier.dragAndDropTarget(
    shouldStartDragAndDrop = { event -> "text/plain" in event.mimeTypes() },
    target = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val text = event.toAndroidDragEvent().clipData?.getItemAt(0)?.text
                handleDrop(text)
                return true
            }
        }
    }
)
```

---

## Swipe to Dismiss / Update

```kotlin
// SwipeToDismissBox
val dismissState = rememberSwipeToDismissBoxState(
    confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart }
)
SwipeToDismissBox(
    state = dismissState,
    backgroundContent = {
        Box(Modifier.fillMaxSize().background(Color.Red)) {
            Icon(Icons.Default.Delete, null, Modifier.align(Alignment.CenterEnd).padding(16.dp))
        }
    }
) {
    ItemCard(item)
}
if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
    LaunchedEffect(Unit) { removeItem() }
}
```

---

## Stylus Input

```kotlin
// Motion events include stylus data when available
Modifier.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            event.changes.forEach { change ->
                val pressure  = change.pressure          // 0.0–1.0
                val tiltX     = change.tilt.x            // radians
                val tiltY     = change.tilt.y
                val toolType  = change.type              // Stylus, Eraser, Finger, Mouse
                val isStylusEraser = change.type == PointerType.Eraser
                val historicalChanges = change.historical // for high-rate stylus data
            }
        }
    }
}

// Stylus in text fields
TextField(
    value = text,
    onValueChange = { text = it },
    // StylusHandwritingModifier enables handwriting recognition
    modifier = Modifier.stylusHandwritingHandler()
)
```

### Ink API (androidx.ink)
```kotlin
// Dependencies
implementation("androidx.ink:ink-authoring:1.0.0-alpha01")
implementation("androidx.ink:ink-rendering:1.0.0-alpha01")
implementation("androidx.ink:ink-geometry:1.0.0-alpha01")

// Define brush
val brush = Brush(
    family = StockBrushes.markerLatest,
    size = 10f,
    epsilon = 0.1f,
    stockColor = StockBrush.Color(Color.Black)
)

// Render strokes
InProgressStrokesView(
    modifier = Modifier.fillMaxSize(),
    inProgressStrokesState = inProgressStrokesState,
    motionEventToViewTransform = motionEventToViewTransform
)

// Geometry APIs for selection, erasing, hit testing
val meshCreator = MeshCreator()
val triangle = ImmutableTriangle(pointA, pointB, pointC)
val isInside = triangle.contains(touchPoint)
```

---

## Copy & Paste

```kotlin
val clipboardManager = LocalClipboardManager.current

// Copy
clipboardManager.setText(AnnotatedString("Copied text"))

// Paste
val pastedText = clipboardManager.getText()?.text ?: ""

// React to clipboard changes (API 33+)
// Use ClipboardManager.addPrimaryClipChangedListener via Android API
```

---

## Accessibility

Ref: https://developer.android.com/develop/ui/compose/accessibility

### Semantics
```kotlin
// Default: Compose infers most semantics automatically
// Custom:
Modifier.semantics {
    contentDescription = "Profile photo of Alice"
    role = Role.Image
    heading()                          // marks as section heading
    disabled()                         // marks as disabled
    focused = true
    selected = true
    onClick(label = "Open profile") { true }
    onLongClick(label = "Options") { true }
    dismiss(label = "Close") { true }
    scrollBy(label = "Scroll down") { _, _ -> true }
    stateDescription = "Selected"
    testTag = "profile_photo"
    liveRegion = LiveRegionMode.Polite  // announces changes
    invisible()                         // hides from a11y tree
    paneTitle = "Dialog"
    error("Invalid input")
    progressBarRangeInfo = ProgressBarRangeInfo(0.7f, 0f..1f)
    setSelection(0, 5, false) { true }  // for text
}
```

### Merging & clearing
```kotlin
// Merge children's semantics into this node (default for clickable containers)
Modifier.semantics(mergeDescendants = true) { }

// Clear children's semantics (for custom drawing)
Modifier.clearAndSetSemantics { contentDescription = "Custom description" }
```

### Traversal order
```kotlin
// Control TalkBack reading order
Modifier.semantics { traversalIndex = -1f }  // lower = earlier
// Default = 0f; use negative to read before siblings, positive to read after
```

### Scalable content (font scale)
```kotlin
// Use sp for text (scales with system font size)
// Use dp for layout (does NOT scale)
// For min tap targets: Modifier.minimumInteractiveComponentSize() (48x48dp minimum)

val fontScale = LocalDensity.current.fontScale
// Adjust layouts for very large font scales
if (fontScale > 1.5f) VerticalLayout() else HorizontalLayout()
```

### Testing accessibility
```kotlin
// In test:
composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
composeTestRule.onNodeWithTag("list").performScrollToIndex(10)

// Debug with Layout Inspector → Accessibility in Android Studio
// Run Accessibility Scanner from Play Store
```

---

## Input Compatibility on Large Screens

```kotlin
// Hover (mouse/trackpad)
Modifier.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            if (event.type == PointerEventType.Enter) showTooltip = true
            if (event.type == PointerEventType.Exit)  showTooltip = false
        }
    }
}

// Mouse scroll
Modifier.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val scrollDelta = event.changes.firstOrNull()?.scrollDelta ?: Offset.Zero
            if (scrollDelta != Offset.Zero) handleScroll(scrollDelta)
        }
    }
}

// Right-click / context menu
Modifier.combinedClickable(
    onLongClick = { showContextMenu = true },    // touch context menu
    onClick = { }
)
// For mouse right-click, use ContextMenuArea from Compose Desktop
```
