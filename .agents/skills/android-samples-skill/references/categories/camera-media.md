# Camera & Media Samples — Reference

**GitHub**: [android/camera-samples](https://github.com/android/camera-samples)
**GitHub**: [android/media-samples](https://github.com/android/media-samples)
**Docs**: [developer.android.com/media](https://developer.android.com/media)

---

## Patterns Covered

1. CameraX — Preview + ImageCapture
2. CameraX — Video capture
3. CameraX — Image Analysis (ML)
4. MediaPlayer (audio playback)
5. ExoPlayer / Media3
6. Audio recording (MediaRecorder)
7. Picking media (Photo Picker)

---

## 1. CameraX — Preview + ImageCapture

```kotlin
// Permissions check first
val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

// CameraX setup in Activity/Fragment
private lateinit var cameraProvider: ProcessCameraProvider
private lateinit var imageCapture: ImageCapture

private fun startCamera() {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    cameraProviderFuture.addListener({
        cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build()
            .also { it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider) }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview, imageCapture
            )
        } catch (e: Exception) {
            Log.e(TAG, "Camera bind failed", e)
        }
    }, ContextCompat.getMainExecutor(this))
}

// Capture photo
private fun takePhoto() {
    val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(this),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) { Log.e(TAG, "Capture failed", exc) }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                Log.d(TAG, "Photo saved: $savedUri")
            }
        }
    )
}
```

**Deps**:
```kotlin
val camerax_version = "1.4.x"
implementation("androidx.camera:camera-core:$camerax_version")
implementation("androidx.camera:camera-camera2:$camerax_version")
implementation("androidx.camera:camera-lifecycle:$camerax_version")
implementation("androidx.camera:camera-view:$camerax_version")
implementation("androidx.camera:camera-video:$camerax_version")
```

---

## 2. CameraX — Video Capture

```kotlin
private var videoCapture: VideoCapture<Recorder>? = null
private var recording: Recording? = null

private fun startCamera() {
    val recorder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
        .build()
    videoCapture = VideoCapture.withOutput(recorder)
    // bind to lifecycle with preview + videoCapture
}

private fun captureVideo() {
    val videoCapture = videoCapture ?: return
    val file = File(outputDir, "${System.currentTimeMillis()}.mp4")
    val outputOptions = FileOutputOptions.Builder(file).build()

    recording = videoCapture.output
        .prepareRecording(this, outputOptions)
        .apply { if (hasMicPermission()) withAudioEnabled() }
        .start(ContextCompat.getMainExecutor(this)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> Log.d(TAG, "Recording started")
                is VideoRecordEvent.Finalize -> {
                    if (!event.hasError()) Log.d(TAG, "Saved: ${event.outputResults.outputUri}")
                    else Log.e(TAG, "Error: ${event.error}")
                }
            }
        }
}

fun stopRecording() { recording?.stop(); recording = null }
```

---

## 3. CameraX — Image Analysis (ML)

```kotlin
val imageAnalyzer = ImageAnalysis.Builder()
    .setTargetResolution(Size(1280, 720))
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()
    .also {
        it.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmap = imageProxy.toBitmap() // needs camerax 1.3+
            // Run ML model on bitmap
            val result = mlModel.process(bitmap)
            imageProxy.close() // MUST call close!
        }
    }

cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
```

---

## 4. MediaPlayer — Audio Playback

```kotlin
class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(uri: Uri) {
        release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(context, uri)
            prepare() // or prepareAsync() for network streams
            start()
        }
    }

    fun pause() { mediaPlayer?.pause() }
    fun resume() { mediaPlayer?.start() }
    fun release() { mediaPlayer?.release(); mediaPlayer = null }
}
```

---

## 5. ExoPlayer / Media3

```kotlin
// Build player
val player = ExoPlayer.Builder(context).build()

// Set media item
val mediaItem = MediaItem.fromUri("https://example.com/audio.mp3")
// Or from URI with metadata
val mediaItem = MediaItem.Builder()
    .setUri(uri)
    .setMediaMetadata(MediaMetadata.Builder().setTitle("Song Title").build())
    .build()

player.setMediaItem(mediaItem)
player.prepare()
player.play()

// Compose PlayerView binding
AndroidView(
    factory = { ctx ->
        PlayerView(ctx).apply { this.player = player }
    },
    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
)

// Cleanup
DisposableEffect(Unit) {
    onDispose { player.release() }
}
```

**Deps**:
```kotlin
implementation("androidx.media3:media3-exoplayer:1.4.x")
implementation("androidx.media3:media3-ui:1.4.x")
implementation("androidx.media3:media3-session:1.4.x")
```

---

## 6. Audio Recording

```kotlin
private var mediaRecorder: MediaRecorder? = null

fun startRecording(outputFile: File) {
    mediaRecorder = (if (Build.VERSION.SDK_INT >= 31)
        MediaRecorder(context) else MediaRecorder()
    ).apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioSamplingRate(44100)
        setAudioEncodingBitRate(128000)
        setOutputFile(outputFile.absolutePath)
        prepare()
        start()
    }
}

fun stopRecording() {
    mediaRecorder?.apply { stop(); release() }
    mediaRecorder = null
}
```

**Permissions (AndroidManifest)**:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

---

## 7. Photo Picker (API 33+ / Backport)

```kotlin
// Single image
val pickMedia = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { uri ->
    if (uri != null) {
        // Grant persistent read URI permission
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        onImageSelected(uri)
    }
}

// Multiple images
val pickMultipleMedia = rememberLauncherForActivityResult(
    ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
) { uris -> onImagesSelected(uris) }

// Launch
Button(onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
    Text("Pick Image")
}
```

---

## Key Notes

- Always close `ImageProxy` in ImageAnalysis — memory leak if forgotten
- Use `ExecutorService` (not main thread) for image analysis
- Photo Picker is backported to API 19 via `pickMedia` contract — prefer over `ACTION_GET_CONTENT`
- ExoPlayer/Media3 handles HLS, DASH, audio focus, background playback via `MediaSession`
- CameraX handles rotation automatically; camera2 requires manual rotation compensation
