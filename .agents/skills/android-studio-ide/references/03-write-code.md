# Android Studio — Writing Code

**Sources:**
- https://developer.android.com/studio/write
- https://developer.android.com/studio/write/lint
- https://developer.android.com/studio/write/annotations
- https://developer.android.com/studio/write/layout-editor

---

## Code Editor Features

### Auto-Import & Completion
- **Basic completion:** `Ctrl+Space` — class names, methods, keywords
- **Smart completion:** `Ctrl+Shift+Space` — type-aware suggestions
- **Statement completion:** `Ctrl+Shift+Enter` — completes current statement + formats
- **Auto-import:** Alt+Enter on unresolved symbol → Import; or enable auto-import in Settings > Editor > General > Auto Import
- **Import optimization:** `Ctrl+Alt+O` — removes unused imports

### Intentions & Quick Fixes (`Alt+Enter`)
Context-aware actions on current symbol/expression:
- Add missing import
- Create function/class from usage
- Convert lambda ↔ anonymous class
- Add `?.` safe call
- Implement interface members
- Wrap in `if`/`try-catch`/`with`
- Convert Java to Kotlin
- Make Composable function

### Code Generation (`Alt+Insert` / `Cmd+N`)
- Override methods
- Implement methods
- Generate constructor, getters/setters (Java)
- Generate `equals()` / `hashCode()` / `toString()`
- Generate test method
- Generate Compose preview

### Postfix Completion
Type expression, then suffix + Tab:
- `x.if` → `if (x) {}`
- `x.for` → `for (item in x) {}`
- `x.null` → `if (x == null) {}`
- `x.nn` → `if (x != null) {}`
- `x.let` → `x.let { }`
- `x.return` → `return x`
- `x.val` → `val name = x`

### Multi-Cursor Editing
- `Alt+Click` (Win) / `Opt+Click` (Mac) — add cursor
- `Ctrl+Ctrl+↓` — clone caret down
- Select word → `Alt+J` — select next occurrence
- `Ctrl+Alt+Shift+J` — select all occurrences

---

## File Templates

**File > New** or right-click in Project pane > New:
- Kotlin Class/File
- Android Resource File / Android Resource Directory
- Activity (from template)
- Fragment (from template)
- Composable function (Compose template)
- ViewModel, Repository, etc.

Customize templates: **Settings > Editor > File and Code Templates**

### Common Templates via New Activity Wizard
Access via **File > New > Activity**:
- **Empty Views Activity** — minimal `AppCompatActivity` + layout XML
- **Empty Activity** — Compose scaffold
- **Basic Views Activity** — AppBar + FAB + NavController
- **Bottom Navigation Activity** — BottomNavigationView with nav graph
- **Navigation Drawer Activity** — DrawerLayout setup
- **Login Activity** — pre-built login form

---

## Compose Tooling

### Compose Preview
Add `@Preview` annotation to composable:
```kotlin
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = Devices.PIXEL_6
)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyScreenPreview() {
    MyAppTheme {
        MyScreen()
    }
}
```

Preview panel: opens automatically when `@Preview` detected. Controls:
- **Interactive mode** — click buttons in preview
- **Deploy to device** — push preview to connected device
- **Animation inspector** — inspect Compose animations

### Live Edit
Real-time preview updates as you type (no full recompile).
- Enable: **Settings > Editor > Live Edit**
- Requires: API 30+, device/emulator connected, Compose 1.3+

### Apply Changes (Hot Reload)
More powerful than Live Edit for code logic:
- **Apply Changes** (`Ctrl+F10`) — push code changes without restart
- **Apply Changes and Restart Activity** — restart just the activity
- **Run** — full reinstall (use when Apply Changes fails)

### Compose Layout Inspector
**View > Tool Windows > Layout Inspector** (app must be running):
- Visual tree of Compose hierarchy
- Click node → see recompositions count, modifiers, parameters
- **Recomposition highlighting** — shows which composables recompose

---

## Layout Editor (Views / XML)

Open any `*.xml` layout file in `res/layout/`.

### Editor Modes (top-right tabs)
- **Code** — raw XML
- **Split** — XML + visual side-by-side
- **Design** — drag-and-drop visual editor

### Design Panel Features
- **Palette** — drag Views/ViewGroups onto canvas
- **Component Tree** — hierarchy of views in layout
- **Attributes** panel (right) — all XML attributes for selected view
- **Constraint handles** — drag to set ConstraintLayout constraints
- **Device selector** — preview on different screen sizes
- **API level selector** — preview with different API themes
- **Orientation toggle** — portrait/landscape

### Common Layout Types
```xml
<!-- ConstraintLayout — recommended for flat hierarchies -->
<androidx.constraintlayout.widget.ConstraintLayout>
    <TextView
        android:id="@+id/tvTitle"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>

<!-- LinearLayout — simple vertical/horizontal stacking -->
<LinearLayout android:orientation="vertical">

<!-- RecyclerView — scrollable lists -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
```

---

## Resource Manager

**View > Tool Windows > Resource Manager** (or from Project pane)

Manages:
- **Drawables** — icons, images, XML drawables
- **Colors** — color resources, Material palette
- **Layouts** — layout XMLs with thumbnails
- **Strings** — `strings.xml` entries
- **Fonts** — custom fonts

Features:
- Drag-and-drop import
- Convert PNG/JPG → WebP
- Scale and density management
- Dark/light theme preview

---

## Vector Asset Studio

**File > New > Vector Asset**

Import options:
- **Clip art** — Google Material icons library (1000+ icons)
- **Local SVG/PSD** — import and convert to `VectorDrawable` XML

Config:
- Override size, opacity, color
- Generates `res/drawable/ic_name.xml`

Use in layouts:
```xml
<ImageView app:srcCompat="@drawable/ic_search" />
```

---

## Image Asset Studio (App Icons)

**File > New > Image Asset**

Generates adaptive icons for all densities:
- **Foreground layer** — icon graphic
- **Background layer** — solid color or image
- Outputs to `res/mipmap-*/` for all densities
- Supports legacy icons + round icons + adaptive icons

---

## Lint

### What Lint Checks
- **Correctness** — API level issues, missing permissions, wrong types
- **Security** — insecure HTTP, weak crypto, SQL injection risks
- **Performance** — overdraw, unnecessary allocations, inefficient layouts
- **Usability** — missing content descriptions, hard-coded text
- **Internationalization** — RTL issues, hard-coded strings
- **Accessibility** — missing labels, touch target sizes

### Run Lint
- **Analyze > Inspect Code** — full project inspection
- **Analyze > Run Inspection by Name** — specific check
- Auto-runs on build for configured severities
- CLI: `./gradlew lint` → generates `app/build/reports/lint-results.html`

### Configure Lint
In `app/build.gradle.kts`:
```kotlin
android {
    lint {
        warningsAsErrors = true
        abortOnError = false
        disable += setOf("MissingTranslation", "ExtraTranslation")
        enable += setOf("RtlHardcoded")
        checkDependencies = true
        htmlReport = true
        htmlOutput = file("lint-report.html")
    }
}
```

Suppress per-file with `@SuppressLint`:
```kotlin
@SuppressLint("HardcodedText")
@Composable
fun MyComposable() { ... }
```

Suppress in XML: `tools:ignore="HardcodedText"`

---

## Annotations

Jetpack Annotations Library — catches bugs at compile time.

### Nullability
```kotlin
fun process(@NonNull input: String): @Nullable Result
```
(In Kotlin, use `?` types — Kotlin compiler handles this natively)

### Resource Type
```kotlin
fun setColor(@ColorRes colorId: Int) { ... }
fun setIcon(@DrawableRes iconId: Int) { ... }
fun setString(@StringRes stringId: Int) { ... }
fun playAnimation(@RawRes animId: Int) { ... }
```

### Threading
```kotlin
@MainThread
fun updateUI() { ... }

@WorkerThread
fun fetchData() { ... }

@UiThread  // alias for @MainThread
@AnyThread // safe to call from any thread
```

### Visibility
```kotlin
@VisibleForTesting
internal fun parseResponse(json: String): Data
```

### Value Constraints
```kotlin
fun setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float)
fun setCount(@IntRange(from = 0) count: Int)
fun setFlags(@IntDef(FLAG_A, FLAG_B, FLAG_C) flags: Int)
```

### Requires Permission
```kotlin
@RequiresPermission(Manifest.permission.CAMERA)
fun openCamera() { ... }

@RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE])
fun accessStorage() { ... }
```

---

## Code Inspections & Analysis

### Find Usages
Right-click symbol > **Find Usages** (`Alt+F7`) — all call sites across project.

### Analyze Data Flow
**Analyze > Analyze Data Flow to Here / from Here** — traces how a value propagates.

### Call Hierarchy
`Ctrl+Alt+H` — who calls this function and what it calls.

### Dependency Matrix
**Analyze > Dependency Matrix** — visualize module dependencies.

---

## Translations Editor

**Open from:** `res/values/strings.xml` → click globe icon, or **Tools > Google > Android Studio Translations Editor**

Features:
- All locales in one table
- Identify untranslated strings
- Export/import XLIFF for translators
- Mark strings as `translatable="false"`

---

## Code Style & Formatting

Auto-format: `Ctrl+Alt+L` / `Cmd+Opt+L`

Configure: **Settings > Editor > Code Style > Kotlin** (or Java)

Key options:
- Indent size (default 4)
- Continuation indent
- Blank lines between declarations
- Import layout order
- Max line length (default 100)

Import `.editorconfig` for team-shared settings. Android Studio reads `.editorconfig` from project root.

**Kotlin style guide:** https://kotlinlang.org/docs/coding-conventions.html
Android official style uses `ktlint` rules.
