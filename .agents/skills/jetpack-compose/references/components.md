# Compose — Components (Material 3)

Ref: https://developer.android.com/develop/ui/compose/components
     https://m3.material.io/components

All components require: `implementation("androidx.compose.material3:material3")`
Wrap app in `MaterialTheme { ... }` for theming.

---

## Scaffold (App Shell)

```kotlin
Scaffold(
    topBar = { TopAppBar(title = { Text("Title") }) },
    bottomBar = { NavigationBar { ... } },
    floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(...) } },
    floatingActionButtonPosition = FabPosition.End,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = MaterialTheme.colorScheme.background
) { paddingValues ->
    // Content MUST apply paddingValues to avoid system bar overlap
    LazyColumn(modifier = Modifier.padding(paddingValues)) { ... }
}
```

---

## App Bars

```kotlin
// Top app bar variants
TopAppBar(
    title = { Text("Screen") },
    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }},
    actions = {
        IconButton(onClick = {}) { Icon(Icons.Default.Search, null) }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null) }
    },
    scrollBehavior = scrollBehavior  // links to LazyColumn scroll
)

// Collapsing top bar
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
LargeTopAppBar(title = { Text("Large Title") }, scrollBehavior = scrollBehavior)
// Connect to scrollable content:
Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)

// Variants: CenterAlignedTopAppBar, MediumTopAppBar, LargeTopAppBar
```

---

## Buttons

```kotlin
Button(onClick = {}) { Text("Filled") }                   // primary action
FilledTonalButton(onClick = {}) { Text("Tonal") }         // secondary
OutlinedButton(onClick = {}) { Text("Outlined") }         // medium emphasis
TextButton(onClick = {}) { Text("Text") }                 // low emphasis
ElevatedButton(onClick = {}) { Text("Elevated") }

// With icon
Button(onClick = {}) {
    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
    Text("Add")
}

// Disabled
Button(onClick = {}, enabled = false) { Text("Disabled") }

// FAB variants
FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, null) }
SmallFloatingActionButton(onClick = {}) { ... }
LargeFloatingActionButton(onClick = {}) { ... }
ExtendedFloatingActionButton(
    text = { Text("Add Item") },
    icon = { Icon(Icons.Default.Add, null) },
    onClick = {},
    expanded = isExpanded  // collapses to icon-only when false
)

// Icon buttons
IconButton(onClick = {}) { Icon(Icons.Default.Favorite, null) }
FilledIconButton(onClick = {}) { ... }
FilledTonalIconButton(onClick = {}) { ... }
OutlinedIconButton(onClick = {}) { ... }

// Segmented button
MultiChoiceSegmentedButtonRow {
    options.forEachIndexed { index, option ->
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index, options.size),
            checked = option in selected,
            onCheckedChange = { toggleSelection(option) }
        ) { Text(option) }
    }
}
```

---

## Selection Controls

```kotlin
// Checkbox
var checked by remember { mutableStateOf(false) }
Checkbox(checked = checked, onCheckedChange = { checked = it })
TriStateCheckbox(state = ToggleableState.Indeterminate, onClick = {})

// Radio button
var selected by remember { mutableStateOf(0) }
options.forEachIndexed { index, label ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected == index, onClick = { selected = index })
        Text(label)
    }
}

// Switch
var on by remember { mutableStateOf(false) }
Switch(
    checked = on,
    onCheckedChange = { on = it },
    thumbContent = if (on) { { Icon(Icons.Filled.Check, null, Modifier.size(SwitchDefaults.IconSize)) } } else null
)

// Slider
var value by remember { mutableStateOf(0f) }
Slider(value = value, onValueChange = { value = it }, valueRange = 0f..100f, steps = 9)
RangeSlider(value = 20f..80f, onValueChange = {}, valueRange = 0f..100f)
```

---

## Text Fields

```kotlin
// Filled (default)
var text by remember { mutableStateOf("") }
TextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Email") },
    placeholder = { Text("user@example.com") },
    leadingIcon = { Icon(Icons.Default.Email, null) },
    trailingIcon = { IconButton(onClick = { text = "" }) { Icon(Icons.Default.Clear, null) } },
    isError = text.isNotEmpty() && !text.contains("@"),
    supportingText = { if (text.isNotEmpty() && !text.contains("@")) Text("Invalid email") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
    singleLine = true,
    maxLines = 1
)

// Outlined
OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Name") })

// Password
var passwordVisible by remember { mutableStateOf(false) }
TextField(
    value = password,
    onValueChange = { password = it },
    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
    trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
        }
    }
)
```

---

## Cards

```kotlin
Card(
    onClick = { },
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
) {
    Column(Modifier.padding(16.dp)) {
        Text("Title", style = MaterialTheme.typography.titleMedium)
        Text("Subtitle", style = MaterialTheme.typography.bodyMedium)
    }
}

ElevatedCard(modifier = Modifier.fillMaxWidth()) { ... }
OutlinedCard(modifier = Modifier.fillMaxWidth()) { ... }
```

---

## Dialogs

```kotlin
// AlertDialog
if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Delete item?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = { delete(); showDialog = false }) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) { Text("Cancel") }
        }
    )
}

// Custom dialog
Dialog(onDismissRequest = { showDialog = false }) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(24.dp)) {
            Text("Custom Dialog", style = MaterialTheme.typography.headlineSmall)
            // any content
        }
    }
}

// Full screen dialog
Dialog(
    onDismissRequest = {},
    properties = DialogProperties(usePlatformDefaultWidth = false)
) { /* full screen content */ }
```

---

## Bottom Sheets

```kotlin
// Modal bottom sheet
val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
var showSheet by remember { mutableStateOf(false) }

if (showSheet) {
    ModalBottomSheet(
        onDismissRequest = { showSheet = false },
        sheetState = sheetState
    ) {
        // content — add Modifier.navigationBarsPadding() to last item
        Column(Modifier.padding(16.dp).navigationBarsPadding()) {
            Text("Sheet content")
            Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
            }) { Text("Close") }
        }
    }
}

// Partial bottom sheet (fixed height)
PartiallyExpandedBottomSheet(...)

// Scaffold bottom sheet (persistent)
val scaffoldState = rememberBottomSheetScaffoldState()
BottomSheetScaffold(
    scaffoldState = scaffoldState,
    sheetContent = { SheetContent() },
    sheetPeekHeight = 128.dp
) { padding -> MainContent(padding) }
```

---

## Navigation Components

```kotlin
// Navigation Bar (bottom, phones)
NavigationBar {
    items.forEach { item ->
        NavigationBarItem(
            selected = currentRoute == item.route,
            onClick = { navController.navigate(item.route) },
            icon = { Icon(item.icon, null) },
            label = { Text(item.title) },
            badge = { Badge { Text("3") } }  // optional badge
        )
    }
}

// Navigation Rail (tablets, landscape)
NavigationRail {
    items.forEach { item ->
        NavigationRailItem(selected = ..., onClick = {}, icon = { ... }, label = { ... })
    }
}

// Navigation Drawer (permanent/modal)
val drawerState = rememberDrawerState(DrawerValue.Closed)
ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
        ModalDrawerSheet {
            NavigationDrawerItem(
                label = { Text("Inbox") },
                selected = currentRoute == "inbox",
                onClick = { /* navigate */ scope.launch { drawerState.close() } },
                icon = { Icon(Icons.Default.Inbox, null) },
                badge = { Text("24") }
            )
        }
    }
) { Scaffold(...) { ... } }
```

---

## Chips

```kotlin
// Types
AssistChip(onClick = {}, label = { Text("Assist") }, leadingIcon = { Icon(...) })
FilterChip(selected = isSelected, onClick = { toggle() }, label = { Text("Filter") })
InputChip(selected = true, onClick = {}, label = { Text("Tag") }, trailingIcon = {
    Icon(Icons.Default.Close, null, Modifier.clickable { remove() })
})
SuggestionChip(onClick = {}, label = { Text("Suggestion") })
```

---

## Progress Indicators

```kotlin
CircularProgressIndicator()                          // indeterminate
CircularProgressIndicator(progress = { 0.7f })      // determinate
LinearProgressIndicator()                            // indeterminate
LinearProgressIndicator(progress = { 0.5f })        // determinate
```

---

## Menus & Dropdowns

```kotlin
var expanded by remember { mutableStateOf(false) }
Box {
    IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, null) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("Edit") }, onClick = { edit(); expanded = false })
        DropdownMenuItem(text = { Text("Delete") }, onClick = { delete(); expanded = false },
            leadingIcon = { Icon(Icons.Default.Delete, null) })
        HorizontalDivider()
        DropdownMenuItem(text = { Text("Share") }, onClick = {})
    }
}

// Exposed dropdown (combo box)
ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
    TextField(
        value = selectedOption,
        onValueChange = {},
        readOnly = true,
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        modifier = Modifier.menuAnchor()
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
            DropdownMenuItem(text = { Text(option) }, onClick = { select(option); expanded = false })
        }
    }
}
```

---

## Snackbar

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
    Button(onClick = {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Item deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) { undo() }
        }
    }) { Text("Delete") }
}
```

---

## Date & Time Pickers

```kotlin
// Date picker
val datePickerState = rememberDatePickerState()
DatePicker(state = datePickerState)

// In dialog
DatePickerDialog(
    onDismissRequest = {},
    confirmButton = { TextButton(onClick = { confirmDate(datePickerState.selectedDateMillis) }) { Text("OK") } }
) { DatePicker(state = datePickerState) }

// Time picker
val timePickerState = rememberTimePickerState(initialHour = 10, initialMinute = 0)
TimePicker(state = timePickerState)
// state.hour, state.minute for selected values
```

---

## Other Components

```kotlin
// Badge
BadgedBox(badge = { Badge { Text("99+") } }) { Icon(Icons.Default.Email, null) }

// Tooltip
TooltipBox(
    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
    tooltip = { PlainTooltip { Text("Tooltip text") } },
    state = rememberTooltipState()
) { Icon(...) }

// Divider
HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
VerticalDivider()

// Pull to refresh
val pullState = rememberPullToRefreshState()
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = { viewModel.refresh() },
    state = pullState
) { LazyColumn { ... } }

// Search bar
SearchBar(
    inputField = { SearchBarDefaults.InputField(query = query, onQueryChange = { query = it },
        onSearch = { search(it) }, expanded = active, onExpandedChange = { active = it }) },
    expanded = active,
    onExpandedChange = { active = it }
) { SearchResults(query) }

// Carousel
HorizontalMultiBrowseCarousel(state = rememberCarouselState { items.count() }) { index ->
    CarouselItem(items[index])
}

// Tabs
val selectedTab = remember { mutableIntStateOf(0) }
TabRow(selectedTabIndex = selectedTab.intValue) {
    tabs.forEachIndexed { index, tab ->
        Tab(selected = selectedTab.intValue == index,
            onClick = { selectedTab.intValue = index },
            text = { Text(tab) })
    }
}
ScrollableTabRow(selectedTabIndex = selectedTab.intValue) { /* many tabs */ }
```
