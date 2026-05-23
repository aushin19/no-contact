# Graphics Samples — Reference

**GitHub**: [android/graphics-samples](https://github.com/android/graphics-samples)

---

## Patterns Covered

1. Canvas 2D drawing (custom View)
2. Compose Canvas
3. OpenGL ES 2.0 surface
4. AGSL shaders (API 33+)
5. Lottie animations

---

## 1. Custom View — Canvas 2D

```kotlin
class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(50, 0, 0, 255)
        style = Paint.Style.FILL
    }

    var dataPoints: List<Float> = emptyList()
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val maxVal = dataPoints.max()
        val path = Path()
        dataPoints.forEachIndexed { i, value ->
            val x = i * width.toFloat() / (dataPoints.size - 1)
            val y = height - (value / maxVal) * height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)
    }
}
```

---

## 2. Compose Canvas

```kotlin
@Composable
fun CircularProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(100.dp)) {
        val strokeWidth = 8.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        // Background track
        drawCircle(
            color = Color.LightGray,
            radius = radius,
            style = Stroke(strokeWidth)
        )

        // Progress arc
        drawArc(
            color = Color.Blue,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(radius * 2, radius * 2)
        )

        // Center text via drawContext.canvas.nativeCanvas
        drawContext.canvas.nativeCanvas.drawText(
            "${(progress * 100).toInt()}%",
            size.width / 2,
            size.height / 2 + 12f,
            android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 32f
            }
        )
    }
}
```

---

## 3. OpenGL ES — GLSurfaceView

```kotlin
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY // battery-friendly
    }
}

class MyGLRenderer : GLSurfaceView.Renderer {
    private lateinit var triangle: Triangle

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        triangle = Triangle()
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        triangle.draw()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }
}
```

---

## 4. Lottie Animations

```kotlin
// In Compose
val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))
val progress by animateLottieCompositionAsState(
    composition,
    iterations = LottieConstants.IterateForever
)

LottieAnimation(
    composition = composition,
    progress = { progress },
    modifier = Modifier.size(200.dp)
)
```

**Deps**:
```kotlin
implementation("com.airbnb.android:lottie-compose:6.x")
```

---

## Key Notes

- Call `invalidate()` to trigger `onDraw()` redraw in custom Views
- Compose `Canvas` coordinates: origin top-left, Y increases downward
- OpenGL: prefer `RENDERMODE_WHEN_DIRTY` over continuous rendering for battery life
- AGSL shaders (API 33+): `RuntimeShader` + `ShaderBrush` in Compose for GPU effects
- For complex 2D: consider `Scene3D` or `RenderNode` for hardware acceleration
