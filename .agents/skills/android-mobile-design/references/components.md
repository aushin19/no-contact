# Android Mobile Design — Components Reference

Source: https://developer.android.com/design/ui/mobile/guides/components/material-overview  
Full MD3 catalog: https://m3.material.io/components

---

## Table of Contents
1. [Component Overview](#1-component-overview)
2. [Action Components](#2-action-components)
3. [Communication Components](#3-communication-components)
4. [Containment Components](#4-containment-components)
5. [Navigation Components](#5-navigation-components)
6. [Selection Components](#6-selection-components)
7. [Text Input Components](#7-text-input-components)
8. [Component Selection Guide](#8-component-selection-guide)

---

## 1. Component Overview

Material Design 3 components = reusable, themed, accessible building blocks.

**Compose dependency:**
```kotlin
implementation("androidx.compose.material3:material3:1.x.x")
```

**Views dependency:**
```kotlin
implementation("com.google.android.material:material:1.x.x")
```

All MD3 components:
- Auto-apply theme colors, shapes, and typography
- Built-in accessibility (semantics, touch targets, content descriptions)
- Support dark mode out of the box
- Follow Material Design 3 spec

---

## 2. Action Components

### Buttons
Five variants — choose by emphasis hierarchy:

| Variant | Visual | Use | Compose |
|---|---|---|---|
| **Filled** | Solid primary bg | Highest emphasis — primary action | `Button` |
| **Filled Tonal** | Secondary container bg | Medium-high emphasis — secondary action | `FilledTonalButton` |
| **Elevated** | Shadow, surface bg | Medium emphasis — needs visual separation | `ElevatedButton` |
| **Outlined** | Border, no bg | Medium emphasis — secondary alongside filled | `OutlinedButton` |
| **Text** | No bg/border | Low emphasis — in-line tertiary action | `TextButton` |

**Rules:**
- Max 2 buttons per dialog/section (primary + secondary)
- Don't mix Filled + Filled — creates ambiguity
- Button height: 40dp; padding: 24dp horizontal

```kotlin
// Primary action
Button(onClick = { }) { Text("Save") }

// Secondary action
OutlinedButton(onClick = { }) { Text("Cancel") }

// Lowest emphasis
TextButton(onClick = { }) { Text("Learn more") }
```

### Icon Buttons
| Variant | Use |
|---|---|
| Standard | Low-emphasis icon action (toolbar) |
| Filled | High-emphasis icon action |
| Filled Tonal | Medium-emphasis icon action |
| Outlined | Medium-emphasis, needs border |

```kotlin
IconButton(onClick = { }) {
    Icon(Icons.Default.Favorite, contentDescription = "Add to favorites")
}
FilledIconButton(onClick = { }) {
    Icon(Icons.Default.Add, contentDescription = "Add")
}
```

### Floating Action Button (FAB)
| Variant | Size | Use |
|---|---|---|
| FAB | 56dp | Primary action — 1 per screen max |
| Small FAB | 40dp | Compact layout auxiliary action |
| Large FAB | 96dp | Very prominent single action |
| Extended FAB | 56dp tall, variable width | When icon alone is unclear — add label |

```kotlin
FloatingActionButton(
    onClick = { },
    containerColor = MaterialTheme.colorScheme.primaryContainer
) {
    Icon(Icons.Default.Edit, contentDescription = "Compose")
}

ExtendedFloatingActionButton(
    onClick = { },
    icon = { Icon(Icons.Default.Add, contentDescription = null) },
    text = { Text("New message") },
    expanded = isExpanded  // collapses to icon on scroll
)
```

**FAB Rules:**
- One FAB per screen — represents the single most important action
- Position: bottom-right, above navigation bar
- Hide/collapse FAB when scrolling down content; reveal on scroll up
- Never use FAB for destructive actions

### Chips
| Variant | Use |
|---|---|
| **Assist chip** | Suggest dynamic actions (e.g. "Add to calendar") |
| **Filter chip** | Toggle filter on/off in a set |
| **Input chip** | Represent user input (e.g. tags, email recipients) |
| **Suggestion chip** | Suggest content/completions |

```kotlin
FilterChip(
    selected = isSelected,
    onClick = { isSelected = !isSelected },
    label = { Text("Electronics") },
    leadingIcon = if (isSelected) {
        { Icon(Icons.Default.Done, contentDescription = null) }
    } else null
)
```

---

## 3. Communication Components

### Snackbar
- Brief feedback message (1–2 lines)
- Auto-dismisses after 4 seconds
- Optional single action (short label, e.g. "Undo")
- Never use for critical information — user might miss it

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { ... }

// Show snackbar
scope.launch {
    snackbarHostState.showSnackbar(
        message = "Email sent",
        actionLabel = "Undo",
        duration = SnackbarDuration.Short
    )
}
```

### Dialog / AlertDialog
- **Alert dialog:** Confirm destructive action, convey critical info
- **Full-screen dialog:** Complex forms, content editing
- Max 2–3 actions; always include dismiss (Cancel/No)

```kotlin
AlertDialog(
    onDismissRequest = { showDialog = false },
    title = { Text("Delete item?") },
    text = { Text("This action cannot be undone.") },
    confirmButton = {
        TextButton(onClick = { onConfirm(); showDialog = false }) {
            Text("Delete")
        }
    },
    dismissButton = {
        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
    }
)
```

### Progress Indicators
| Type | Use |
|---|---|
| Linear (determinate) | Known progress: file download, form step |
| Linear (indeterminate) | Unknown duration: loading data |
| Circular (determinate) | Known progress in compact space |
| Circular (indeterminate) | Loading spinner |

```kotlin
// Indeterminate — "loading"
CircularProgressIndicator()
LinearProgressIndicator()

// Determinate — known progress
CircularProgressIndicator(progress = { 0.7f })
LinearProgressIndicator(progress = { downloadProgress })
```

### Badges
- Notification count on nav icons or avatars
- `Badge` (dot) for unread state; `BadgedBox` for count

```kotlin
BadgedBox(badge = { Badge { Text("3") } }) {
    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
}
```

### Tooltips
- **Plain tooltip:** Brief label for icon buttons; show on long press
- **Rich tooltip:** More info, optional action; show on long press

```kotlin
TooltipBox(
    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
    tooltip = { PlainTooltip { Text("Add to favorites") } },
    state = rememberTooltipState()
) {
    IconButton(onClick = { }) {
        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
    }
}
```

---

## 4. Containment Components

### Cards
```kotlin
// Elevated card
Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
) { CardContent() }

// Filled card (surface container bg)
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
) { CardContent() }

// Outlined card
OutlinedCard { CardContent() }
```

### Bottom Sheets
| Type | Behavior | Use |
|---|---|---|
| **Modal** | Dims background; must dismiss to continue | Additional options, confirmation |
| **Standard** | Coexists with content; partially visible | Persistent context, map details |

```kotlin
// Modal bottom sheet
ModalBottomSheet(
    onDismissRequest = { showSheet = false },
    sheetState = rememberModalBottomSheetState()
) {
    Column(Modifier.padding(16.dp)) {
        Text("Sheet content")
        // Bottom padding for nav bar handled automatically
        Spacer(Modifier.navigationBarsPadding())
    }
}
```

### Menus
| Type | Trigger | Use |
|---|---|---|
| **Dropdown menu** | Button/icon tap | Overflow actions, options |
| **Context menu** | Long press | Item-specific actions |
| **Exposed dropdown** | Form field tap | Selection in forms |

```kotlin
var expanded by remember { mutableStateOf(false) }
Box {
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit() })
        DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete() })
    }
}
```

### Lists (ListItem)
```kotlin
ListItem(
    headlineContent = { Text("Item title") },
    supportingContent = { Text("Supporting text") },
    leadingContent = {
        Icon(Icons.Default.Person, contentDescription = null)
    },
    trailingContent = {
        Text("Meta", style = MaterialTheme.typography.labelSmall)
    }
)
```

---

## 5. Navigation Components

### Top App Bar
```kotlin
// Small (default)
TopAppBar(
    title = { Text("Screen Title") },
    navigationIcon = {
        IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    },
    actions = {
        IconButton(onClick = { }) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    }
)

// Large (collapses on scroll)
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
LargeTopAppBar(
    title = { Text("Large Title") },
    scrollBehavior = scrollBehavior
)
```

### Navigation Bar (Bottom)
```kotlin
NavigationBar {
    NavItem.values().forEach { item ->
        NavigationBarItem(
            selected = selectedItem == item,
            onClick = { selectedItem = item },
            icon = {
                Icon(
                    if (selectedItem == item) item.selectedIcon else item.icon,
                    contentDescription = item.label
                )
            },
            label = { Text(item.label) }
        )
    }
}
```

### Navigation Rail
```kotlin
NavigationRail {
    items.forEach { item ->
        NavigationRailItem(
            selected = selected == item,
            onClick = { selected = item },
            icon = { Icon(item.icon, contentDescription = item.label) },
            label = { Text(item.label) }
        )
    }
}
```

### Navigation Drawer
```kotlin
// Modal drawer
ModalNavigationDrawer(
    drawerContent = {
        ModalDrawerSheet {
            Text("App Name", modifier = Modifier.padding(16.dp))
            Divider()
            items.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.label) },
                    icon = { Icon(item.icon, contentDescription = null) },
                    selected = selected == item,
                    onClick = { selected = item; closeDrawer() }
                )
            }
        }
    }
) { content() }
```

### Tabs
```kotlin
TabRow(selectedTabIndex = selectedTab) {
    tabs.forEachIndexed { index, tab ->
        Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = { Text(tab) }
        )
    }
}

// Scrollable tabs (for 5+ tabs)
ScrollableTabRow(selectedTabIndex = selectedTab) { ... }
```

---

## 6. Selection Components

### Checkbox
```kotlin
Checkbox(
    checked = isChecked,
    onCheckedChange = { isChecked = it }
)
// Tri-state
TriStateCheckbox(state = toggleState, onClick = { })
```

### Radio Button
```kotlin
options.forEach { option ->
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selectedOption == option, onClick = { selectedOption = option })
            .padding(16.dp)
    ) {
        RadioButton(selected = selectedOption == option, onClick = null)
        Text(option, Modifier.padding(start = 16.dp))
    }
}
```

### Switch
```kotlin
Switch(
    checked = isSwitchOn,
    onCheckedChange = { isSwitchOn = it },
    thumbContent = if (isSwitchOn) {
        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(SwitchDefaults.IconSize)) }
    } else null
)
```

### Slider
```kotlin
// Continuous
Slider(value = sliderValue, onValueChange = { sliderValue = it })

// Stepped / discrete
Slider(
    value = sliderValue,
    onValueChange = { sliderValue = it },
    steps = 4,
    valueRange = 0f..100f
)

// Range slider
RangeSlider(value = rangeValues, onValueChange = { rangeValues = it })
```

### Date & Time Pickers
```kotlin
// Date picker dialog
DatePickerDialog(
    onDismissRequest = { },
    confirmButton = { TextButton(onClick = { }) { Text("OK") } }
) {
    DatePicker(state = rememberDatePickerState())
}

// Time picker
TimePicker(state = rememberTimePickerState(initialHour = 12, initialMinute = 0))
```

---

## 7. Text Input Components

### TextField Variants
| Variant | Visual | Use |
|---|---|---|
| **Filled** | Colored bg, underline | Default — most forms |
| **Outlined** | Border, no bg | When bg contrast needed |

```kotlin
// Filled (default)
TextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Email") },
    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
    trailingIcon = {
        if (text.isNotEmpty()) {
            IconButton(onClick = { text = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
        }
    },
    supportingText = { Text("Enter your registered email") },
    isError = isError,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    singleLine = true
)

// Outlined
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Name") }
)
```

### Text Field States
- **Enabled** — default
- **Focused** — active input, colored indicator
- **Error** — red indicator + error message in `supportingText`
- **Disabled** — no interaction, greyed out

### Validation Pattern
```kotlin
var text by remember { mutableStateOf("") }
var isError by remember { mutableStateOf(false) }
val errorMessage = "Required field"

TextField(
    value = text,
    onValueChange = {
        text = it
        isError = it.isEmpty()
    },
    isError = isError,
    supportingText = {
        if (isError) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
)
```

### Search Bar
```kotlin
// Docked search bar (in top bar)
DockedSearchBar(
    query = query,
    onQueryChange = { query = it },
    onSearch = { performSearch(it) },
    active = isSearchActive,
    onActiveChange = { isSearchActive = it },
    placeholder = { Text("Search") },
    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
) {
    // Suggestions content
}
```

---

## 8. Component Selection Guide

### Action Hierarchy (Most → Least Emphasis)
```
Filled Button > Filled Tonal Button > Elevated Button > Outlined Button > Text Button
FAB (primary) > Extended FAB > Small FAB
Filled Icon Button > Filled Tonal Icon Button > Outlined Icon Button > Icon Button
```

### "Which component?" Quick Reference
| Need | Use |
|---|---|
| Primary screen action | FAB or Filled Button |
| Confirm/Cancel pair | Filled + Text Button (or Outlined + Text) |
| Overflow menu | `DropdownMenu` via `MoreVert` icon |
| Notification count | `BadgedBox` |
| Temporary status message | `Snackbar` |
| Confirmation for destructive action | `AlertDialog` |
| Complex form in overlay | `ModalBottomSheet` or full-screen dialog |
| Toggle setting | `Switch` |
| Multiple selection | `Checkbox` |
| Exclusive selection | `RadioButton` |
| Continuous value | `Slider` |
| Text filter | `FilterChip` |
| User-entered tag | `InputChip` |
| Form field | `TextField` (filled) / `OutlinedTextField` |
| Loading state | `CircularProgressIndicator` (indeterminate) |
