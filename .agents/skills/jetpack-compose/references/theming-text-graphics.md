# Compose — Theming, Text, Graphics & Animation

Ref: https://developer.android.com/develop/ui/compose/designsystems/material3
     https://developer.android.com/develop/ui/compose/text
     https://developer.android.com/develop/ui/compose/graphics
     https://developer.android.com/develop/ui/compose/animation/introduction

---

## Material 3 Theme

### Structure
```kotlin
MaterialTheme(
    colorScheme = myColorScheme,
    typography = myTypography,
    shapes = myShapes
) { /* all composables inherit theme */ }
```

### Color scheme
```kotlin
// Generate from seed color (use Material Theme Builder: https://m3.material.io/theme-builder)
val Purple80 = Color(0xFFD0BCFF)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    // 30 color roles — only override what you need
)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Dynamic color (Android 12+)
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
```

### Accessing theme values
```kotlin
MaterialTheme.colorScheme.primary        // main brand color
MaterialTheme.colorScheme.onPrimary      // content on primary
MaterialTheme.colorScheme.surface        // card/sheet background
MaterialTheme.colorScheme.background     // screen background
MaterialTheme.colorScheme.error          // error state
MaterialTheme.typography.displayLarge
MaterialTheme.typography.headlineMedium
MaterialTheme.typography.bodyLarge
MaterialTheme.typography.labelSmall
MaterialTheme.shapes.small               // RoundedCornerShape(4.dp)
MaterialTheme.shapes.medium              // RoundedCornerShape(12.dp)
MaterialTheme.shapes.large               // RoundedCornerShape(16.dp)
MaterialTheme.shapes.extraLarge          // RoundedCornerShape(28.dp)
```

### Custom theme extensions
```kotlin
// Define extension
data class ExtendedColorScheme(val warning: Color, val success: Color)
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColorScheme(Color.Yellow, Color.Green) }

// Provide
CompositionLocalProvider(LocalExtendedColors provides myExtended) {
    MaterialTheme(colorScheme = ...) { content() }
}

// Access
val extended = LocalExtendedColors.current
Box(Modifier.background(extended.warning))
```

### Styles (Compose 1.7+)
```kotlin
// Define reusable style
val PrimaryButtonStyle = ButtonStyle { /* state-based styling */ }

// Styles encapsulate: colors, shapes, typography for a component
// See: https://developer.android.com/develop/ui/compose/styles
```

---

## Typography

```kotlin
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    // ... all 15 type scale roles
)

// Custom fonts
val MontserratFont = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

// Downloadable fonts
val provider = GoogleFonts.Provider(...)
val Roboto = FontFamily(Font(googleFont = GoogleFont("Roboto"), fontProvider = provider))
```

---

## Text

```kotlin
// Display
Text(
    text = "Hello World",
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onBackground,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Italic,
    textDecoration = TextDecoration.Underline,
    textAlign = TextAlign.Center,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.fillMaxWidth()
)

// Annotated string (mixed styles)
val annotated = buildAnnotatedString {
    withStyle(SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) { append("Bold Blue ") }
    append("Normal ")
    withStyle(ParagraphStyle(textIndent = TextIndent(firstLine = 16.sp))) { append("Indented paragraph") }
}
Text(annotated)

// Clickable spans
val annotated = buildAnnotatedString {
    append("Visit ")
    pushStringAnnotation("URL", "https://example.com")
    withStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) { append("example.com") }
    pop()
}
ClickableText(annotated, onClick = { offset ->
    annotated.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
        openUrl(it.item)
    }
})

// SelectionContainer
SelectionContainer { Text("Selectable text") }

// From resources
Text(stringResource(R.string.greeting, name))
Text(pluralStringResource(R.plurals.items, count, count))
```

### Paragraph styling
```kotlin
Text(buildAnnotatedString {
    withStyle(ParagraphStyle(
        textAlign = TextAlign.Justify,
        lineHeight = 24.sp,
        textIndent = TextIndent(firstLine = 24.sp, restLine = 0.sp)
    )) { append("Long paragraph text...") }
})
```

---

## Images & Graphics

### Image
```kotlin
Image(
    painter = painterResource(R.drawable.photo),
    contentDescription = "Profile photo",
    contentScale = ContentScale.Crop,       // Crop, Fit, FillBounds, Inside, None
    alignment = Alignment.Center,
    modifier = Modifier.size(64.dp).clip(CircleShape)
)

// Async image loading (Coil)
// implementation("io.coil-kt.coil3:coil-compose:3.x")
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data("https://example.com/image.jpg")
        .crossfade(true)
        .build(),
    contentDescription = "Remote image",
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error),
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxWidth().height(200.dp)
)

// Vector icon
Icon(
    imageVector = Icons.Default.Favorite,
    contentDescription = "Favorite",
    tint = MaterialTheme.colorScheme.primary
)
Icon(painterResource(R.drawable.ic_custom), contentDescription = null)
```

### Canvas / custom drawing
```kotlin
Canvas(modifier = Modifier.size(200.dp)) {
    // size: Size, center: Offset available
    drawCircle(color = Color.Blue, radius = size.minDimension / 2)
    drawRect(color = Color.Red, topLeft = Offset(0f, 0f), size = Size(100f, 100f))
    drawLine(color = Color.Black, start = Offset(0f, 0f), end = Offset(200f, 200f), strokeWidth = 4f)
    drawArc(color = Color.Green, startAngle = 0f, sweepAngle = 180f, useCenter = false,
        style = Stroke(width = 8f))
    drawRoundRect(color = Color.Cyan, cornerRadius = CornerRadius(16f, 16f))

    // Custom path
    val path = Path().apply {
        moveTo(0f, size.height)
        cubicTo(size.width * 0.25f, 0f, size.width * 0.75f, size.height, size.width, 0f)
    }
    drawPath(path, color = Color.Magenta, style = Stroke(width = 6f))

    // Text on canvas
    drawContext.canvas.nativeCanvas.drawText("Label", x, y, android.graphics.Paint())
}
```

### Brush
```kotlin
val gradient = Brush.horizontalGradient(listOf(Color.Red, Color.Blue))
val vertGradient = Brush.verticalGradient(listOf(Color.Yellow, Color.Green))
val radial = Brush.radialGradient(listOf(Color.White, Color.Black), radius = 200f)
val sweep  = Brush.sweepGradient(listOf(Color.Red, Color.Blue, Color.Red))
val linear = Brush.linearGradient(listOf(Color.Red, Color.Blue),
    start = Offset(0f, 0f), end = Offset(500f, 500f))

// Apply to text
Text("Gradient", style = TextStyle(brush = gradient))
// Apply to background
Box(Modifier.background(gradient))
```

### Shapes
```kotlin
RoundedCornerShape(8.dp)
RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
CircleShape                      // == RoundedCornerShape(50%)
CutCornerShape(8.dp)
AbsoluteCutCornerShape(8.dp)     // ignores RTL
GenericShape { size, layoutDir -> /* custom Path */ }
```

---

## Animation

### Choosing an API
```
Simple value change?    → animate*AsState
Visibility toggle?     → AnimatedVisibility
Content swap?          → AnimatedContent / Crossfade
Continuous loop?       → rememberInfiniteTransition
Multi-value?           → updateTransition
Low-level control?     → Animatable
```

### animate*AsState (simple, fire-and-forget)
```kotlin
val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, label = "alpha")
val color by animateColorAsState(targetValue = if (active) Color.Blue else Color.Gray, label = "color")
val size  by animateDpAsState(targetValue = if (expanded) 200.dp else 48.dp,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "size")
val offset by animateOffsetAsState(Offset(x, y), label = "offset")

// Also: animateIntAsState, animateSizeAsState, animateIntOffsetAsState
```

### AnimatedVisibility
```kotlin
AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
    exit  = fadeOut() + slideOutVertically(targetOffsetY = { -it })
) {
    Card { Text("I animate in and out") }
}
// Enter specs: fadeIn, slideInHorizontally, slideInVertically, expandIn, expandHorizontally,
//              expandVertically, scaleIn
// Exit specs:  fadeOut, slideOutHorizontally, slideOutVertically, shrinkOut, scaleOut
```

### AnimatedContent
```kotlin
AnimatedContent(
    targetState = count,
    transitionSpec = {
        if (targetState > initialState) {
            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
        } else {
            slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
        }
    },
    label = "counter"
) { target ->
    Text("$target", style = MaterialTheme.typography.displayLarge)
}
```

### Crossfade
```kotlin
Crossfade(targetState = currentScreen, label = "screen") { screen ->
    when (screen) {
        Screen.Home   -> HomeContent()
        Screen.Detail -> DetailContent()
    }
}
```

### updateTransition (multi-value, state-driven)
```kotlin
val transition = updateTransition(targetState = expanded, label = "expand")
val cornerRadius by transition.animateDp(label = "corner") { if (it) 0.dp else 16.dp }
val elevation    by transition.animateFloat(label = "elev") { if (it) 8f else 2f }
val color        by transition.animateColor(label = "color") {
    if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
}
```

### rememberInfiniteTransition (looping)
```kotlin
val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
val alpha by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
        animation = tween(1000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
)
```

### Animatable (low-level, imperative)
```kotlin
val anim = remember { Animatable(0f) }
LaunchedEffect(trigger) {
    anim.animateTo(1f, tween(500))
    delay(200)
    anim.animateTo(0f, spring())
}
Box(Modifier.alpha(anim.value))
```

### AnimationSpec options
```kotlin
tween(durationMillis = 300, delayMillis = 0, easing = FastOutSlowInEasing)
spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
snap()                                // instant
keyframes {
    durationMillis = 400
    0.0f at 0 with LinearEasing
    1.0f at 200
    0.8f at 400
}
infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Restart)
```

### Shared element transitions (Compose 1.7+)
```kotlin
// In list
ListItem(Modifier.sharedElement(
    state = rememberSharedContentState(key = "image-${item.id}"),
    animatedVisibilityScope = this@AnimatedContent
)) { ... }

// In detail
DetailImage(Modifier.sharedElement(
    state = rememberSharedContentState(key = "image-${item.id}"),
    animatedVisibilityScope = this@AnimatedContent
))
```

### Animated vector drawables
```kotlin
val avd = AnimatedImageVector.animatedVectorResource(R.drawable.avd_anim)
val atEnd by remember { mutableStateOf(false) }
Icon(painter = rememberAnimatedVectorPainter(avd, atEnd), contentDescription = null)
```

---

## Graphics Layer & Transforms

```kotlin
Modifier.graphicsLayer {
    scaleX = 1.2f
    scaleY = 1.2f
    rotationZ = 45f
    rotationX = 30f        // 3D perspective
    rotationY = 30f
    translationX = 50f
    translationY = 50f
    alpha = 0.8f
    shadowElevation = 8f
    shape = RoundedCornerShape(16.dp)
    clip = true
    renderEffect = BlurEffect(16f, 16f)  // API 31+
    transformOrigin = TransformOrigin(0.5f, 0.5f)  // pivot point
}
```
