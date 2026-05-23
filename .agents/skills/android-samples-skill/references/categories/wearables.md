# Wearables Samples — Reference

**GitHub**: [android/wear-os-samples](https://github.com/android/wear-os-samples)
**Docs**: [developer.android.com/training/wearables](https://developer.android.com/training/wearables)

---

## Patterns Covered

1. Compose for Wear OS — basic screen
2. Wear OS navigation
3. Health Services (exercise tracking)
4. Watch Face Service
5. Tiles

---

## 1. Compose for Wear OS — Basic Screen

```kotlin
@Composable
fun WearApp() {
    AppTheme {
        // WearOS scaffold with time + scroll indicator
        val listState = rememberScalingLazyListState()
        AppScaffold(
            timeText = { TimeText() }
        ) {
            ScreenScaffold(scrollState = listState) {
                ScalingLazyColumn(state = listState) {
                    item { Text("Hello Wear OS", style = MaterialTheme.typography.title2) }
                    items(10) { index ->
                        Chip(
                            label = { Text("Item $index") },
                            onClick = { /* action */ }
                        )
                    }
                }
            }
        }
    }
}
```

**Deps**:
```kotlin
implementation("androidx.wear.compose:compose-material3:1.0.x")
implementation("androidx.wear.compose:compose-foundation:1.4.x")
implementation("androidx.wear.compose:compose-navigation:1.4.x")
```

---

## 2. Wear OS Navigation

```kotlin
@Composable
fun WearNavHost() {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(onNavigate = { navController.navigate("detail/$it") }) }
        composable("detail/{id}") { backStack ->
            DetailScreen(id = backStack.arguments?.getString("id") ?: "")
        }
    }
}
```

---

## 3. Health Services — Exercise Tracking

```kotlin
class ExerciseViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository
) : ViewModel() {

    val exerciseState = healthServicesRepository.exerciseSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun startExercise() = viewModelScope.launch {
        healthServicesRepository.startExercise()
    }

    fun pauseExercise() = viewModelScope.launch {
        healthServicesRepository.pauseExercise()
    }
}

// Repository wraps HealthServicesClient
class HealthServicesRepository @Inject constructor(
    private val exerciseClient: ExerciseClient
) {
    val exerciseSessionFlow = exerciseClient.exerciseUpdateFlow
        .map { update ->
            ExerciseState(
                heartRate = update.latestMetrics.getData(DataType.HEART_RATE_BPM)?.last()?.value,
                distance = update.latestMetrics.getData(DataType.DISTANCE)?.last()?.value,
                activeTime = update.activeDurationCheckpoint?.activeDuration
            )
        }
}
```

**Deps**:
```kotlin
implementation("androidx.health:health-services-client:1.1.x")
```

---

## 4. Watch Face (Compose-based)

```kotlin
class MyWatchFaceService : WatchFaceService() {
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = MyWatchFaceRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            complicationSlotsManager = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.HARDWARE
        )
        return WatchFace(WatchFaceType.DIGITAL, renderer)
    }
}
```

**Deps**:
```kotlin
implementation("androidx.wear.watchface:watchface:1.2.x")
implementation("androidx.wear.watchface:watchface-complications-rendering:1.2.x")
```

---

## Key Notes

- Wear OS uses `ScalingLazyColumn` not `LazyColumn` — auto-scales items near screen edges
- `AppScaffold` + `ScreenScaffold` = curved time text + scroll indicator automatically
- Target min API 30 (Wear OS 3) for Compose; API 25 minimum for Wear 2
- Health Services API differs from Android Sensors — use `ExerciseClient` for workout data
- Tiles (quick-glance widgets) use `TileService`, not Compose — separate API surface
