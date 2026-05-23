# Sensors Samples — Reference

**GitHub**: [android/sensors-samples](https://github.com/android/sensors-samples)

---

## Patterns Covered

1. SensorManager — accelerometer / gyroscope
2. Location (FusedLocationProviderClient)
3. Activity Recognition
4. Step Counter

---

## 1. SensorManager — Accelerometer

```kotlin
class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager = application.getSystemService(SensorManager::class.java)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _sensorData = MutableStateFlow(Triple(0f, 0f, 0f))
    val sensorData = _sensorData.asStateFlow()

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            _sensorData.value = Triple(event.values[0], event.values[1], event.values[2])
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    fun registerSensor() {
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun unregisterSensor() {
        sensorManager.unregisterListener(listener)
    }

    override fun onCleared() { unregisterSensor() }
}
```

---

## 2. Location — FusedLocationProvider

```kotlin
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun observeLocation(): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }
}
```

**Deps**:
```kotlin
implementation("com.google.android.gms:play-services-location:21.x")
```

---

## 3. Step Counter

```kotlin
// Requires ACTIVITY_RECOGNITION permission (API 29+)
val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
// Returns cumulative steps since last reboot
// Persist baseline value to calculate session steps
```

---

## Key Notes

- Always unregister listeners in `onPause()` / `ViewModel.onCleared()` — battery drain
- `SENSOR_DELAY_NORMAL` (200ms) for most UI; `SENSOR_DELAY_FASTEST` for gaming
- Location: always check `ACCESS_FINE_LOCATION` permission before requesting
- `callbackFlow {}` is the idiomatic way to wrap callback-based APIs into `Flow`
- Use `TYPE_ROTATION_VECTOR` sensor for reliable orientation (fuses accel + gyro + magnetometer)
