# Connectivity Samples — Reference

**GitHub**: [android/connectivity-samples](https://github.com/android/connectivity-samples)

---

## Patterns Covered

1. Retrofit + OkHttp (REST)
2. WorkManager (background tasks)
3. Bluetooth Low Energy (BLE)
4. NFC read/write
5. WiFi P2P
6. WebSocket

---

## 1. Retrofit + OkHttp

```kotlin
// API interface
interface ApiService {
    @GET("tasks")
    suspend fun getTasks(): List<TaskDto>

    @POST("tasks")
    suspend fun createTask(@Body task: CreateTaskRequest): TaskDto

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body task: UpdateTaskRequest): TaskDto

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Unit>
}

// Client setup
@Module @InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.API_KEY}")
                .build()
            chain.proceed(req)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}

// Repository with error handling
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val code: Int, val message: String) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}

suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(call())
    } catch (e: HttpException) {
        NetworkResult.Error(e.code(), e.message())
    } catch (e: IOException) {
        NetworkResult.Error(-1, "Network error: ${e.message}")
    }
}
```

**Deps**:
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.x")
implementation("com.squareup.retrofit2:converter-gson:2.11.x")
implementation("com.squareup.okhttp3:okhttp:4.12.x")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.x")
```

---

## 2. WorkManager

```kotlin
// Worker class
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val data = inputData.getString("userId") ?: return Result.failure()
            repository.sync(data)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

// Schedule periodic sync
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    )
    .setInputData(workDataOf("userId" to userId))
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync_work",
    ExistingPeriodicWorkPolicy.KEEP,
    syncRequest
)

// Observe work state in Compose
val workInfo by WorkManager.getInstance(context)
    .getWorkInfosForUniqueWorkLiveData("sync_work")
    .observeAsState()
```

**Deps**:
```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.x")
implementation("androidx.hilt:hilt-work:1.2.x")
```

---

## 3. Bluetooth Low Energy (BLE)

```kotlin
// BLE scan
class BleScanner(private val context: Context) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter.bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val rssi = result.rssi
            // Process discovered device
        }
        override fun onScanFailed(errorCode: Int) { Log.e(TAG, "Scan failed: $errorCode") }
    }

    fun startScan(serviceUuid: UUID? = null) {
        val filters = serviceUuid?.let {
            listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build())
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        bleScanner.startScan(filters, settings, scanCallback)
    }

    fun stopScan() { bleScanner.stopScan(scanCallback) }
}

// GATT connection + read characteristic
fun connectToDevice(device: BluetoothDevice) {
    device.connectGatt(context, false, object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) gatt.discoverServices()
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val characteristic = gatt
                .getService(SERVICE_UUID)
                ?.getCharacteristic(CHARACTERISTIC_UUID)
            characteristic?.let { gatt.readCharacteristic(it) }
        }
        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic,
            value: ByteArray, status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Read value: ${value.decodeToString()}")
            }
        }
    })
}
```

**Permissions (API 31+)**:
```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```

---

## 4. NFC Read/Write

```kotlin
// AndroidManifest.xml
// <uses-permission android:name="android.permission.NFC"/>
// <intent-filter> with ACTION_NDEF_DISCOVERED for foreground dispatch

// Read NFC tag
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        val messages = rawMessages?.map { it as NdefMessage } ?: return
        messages.forEach { msg ->
            msg.records.forEach { record ->
                val payload = String(record.payload)
                Log.d(TAG, "NFC payload: $payload")
            }
        }
    }
}

// Write NFC tag
fun writeTag(tag: Tag, text: String) {
    val ndef = Ndef.get(tag) ?: return
    ndef.connect()
    val record = NdefRecord.createTextRecord("en", text)
    val message = NdefMessage(arrayOf(record))
    ndef.writeNdefMessage(message)
    ndef.close()
}
```

---

## 5. WebSocket (OkHttp)

```kotlin
class WebSocketManager(private val url: String) {
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) { Log.d(TAG, "Connected") }
            override fun onMessage(ws: WebSocket, text: String) {
                GlobalScope.launch { _messages.emit(text) }
            }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
            }
        })
    }

    fun send(message: String) { webSocket?.send(message) }
    fun disconnect() { webSocket?.close(1000, "Goodbye"); client.dispatcher.executorService.shutdown() }
}
```

---

## Key Notes

- WorkManager is the recommended solution for deferrable, guaranteed background work
- BLE on API 31+ requires `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` runtime permissions
- For BLE scanning, also need `ACCESS_FINE_LOCATION` on API < 31
- Use `ConnectivityManager.registerNetworkCallback()` to observe network availability
- OkHttp logging interceptor: only enable in debug builds (`BuildConfig.DEBUG`)
