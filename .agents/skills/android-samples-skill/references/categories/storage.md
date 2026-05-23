# Storage Samples — Reference

**GitHub**: [android/storage-samples](https://github.com/android/storage-samples)

---

## Patterns Covered

1. Room database (full setup)
2. DataStore (Preferences + Proto)
3. Storage Access Framework (SAF)
4. MediaStore (photos, files)
5. File I/O (internal/external)

---

## 1. Room Database

```kotlin
// Entity
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// DAO
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)
}

// Database
@Database(entities = [TaskEntity::class], version = 2, exportSchema = true)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

// Migration
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN description TEXT NOT NULL DEFAULT ''")
    }
}

// Hilt module
@Provides @Singleton
fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
    Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db")
        .addMigrations(MIGRATION_1_2)
        .build()
```

**Deps**:
```kotlin
implementation("androidx.room:room-runtime:2.6.x")
implementation("androidx.room:room-ktx:2.6.x")
ksp("androidx.room:room-compiler:2.6.x")
```

---

## 2. DataStore — Preferences

```kotlin
// Define keys
object PreferencesKeys {
    val SORT_ORDER = stringPreferencesKey("sort_order")
    val SHOW_COMPLETED = booleanPreferencesKey("show_completed")
    val USER_ID = stringPreferencesKey("user_id")
}

// DataStore instance (top-level, singleton)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Repository
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val sortOrder: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[PreferencesKeys.SORT_ORDER] ?: "BY_DATE" }

    suspend fun saveSortOrder(order: String) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.SORT_ORDER] = order }
    }

    suspend fun toggleShowCompleted() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SHOW_COMPLETED] = !(prefs[PreferencesKeys.SHOW_COMPLETED] ?: true)
        }
    }
}
```

**Deps**:
```kotlin
implementation("androidx.datastore:datastore-preferences:1.1.x")
// For Proto DataStore:
implementation("androidx.datastore:datastore:1.1.x")
```

---

## 3. Storage Access Framework (SAF)

```kotlin
// Open document picker
val openDoc = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
) { uri ->
    uri ?: return@rememberLauncherForActivityResult
    // Persist permission across reboots
    context.contentResolver.takePersistableUriPermission(
        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )
    processDocument(uri)
}

Button(onClick = { openDoc.launch(arrayOf("application/pdf", "text/*")) }) {
    Text("Open Document")
}

// Create new document
val createDoc = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
) { uri ->
    uri ?: return@rememberLauncherForActivityResult
    context.contentResolver.openOutputStream(uri)?.use { stream ->
        stream.write(pdfBytes)
    }
}

// Read file content
fun readTextFile(uri: Uri): String {
    return context.contentResolver.openInputStream(uri)
        ?.bufferedReader()
        ?.use { it.readText() } ?: ""
}
```

---

## 4. MediaStore

```kotlin
// Query images
fun queryImages(context: Context): List<Uri> {
    val images = mutableListOf<Uri>()
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_MODIFIED
    )
    val selection = "${MediaStore.Images.Media.SIZE} >= ?"
    val selectionArgs = arrayOf("1024") // > 1KB

    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            images += ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        }
    }
    return images
}

// Save image to MediaStore (API 29+)
fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        context.contentResolver.openOutputStream(it)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(it, values, null, null)
    }
    return uri
}
```

**Permissions**:
```xml
<!-- API < 33 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32"/>
<!-- API 33+ (granular) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
```

---

## 5. File I/O

```kotlin
// Internal storage (no permission needed)
fun writeInternalFile(context: Context, filename: String, content: String) {
    context.openFileOutput(filename, Context.MODE_PRIVATE).use { stream ->
        stream.write(content.toByteArray())
    }
}

fun readInternalFile(context: Context, filename: String): String =
    context.openFileInput(filename).bufferedReader().use { it.readText() }

// Cache directory
val cacheFile = File(context.cacheDir, "temp_image.jpg")

// External files dir (no permission needed, scoped)
val externalFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "report.pdf")

// App-specific downloads
fun downloadFile(url: String, destFile: File) {
    URL(url).openStream().use { input ->
        FileOutputStream(destFile).use { output ->
            input.copyTo(output)
        }
    }
}
```

---

## Key Notes

- Prefer DataStore over SharedPreferences — DataStore is coroutine-safe, no ANR risk
- Room `Flow` queries auto-update UI when DB changes — no manual refresh needed
- Always `takePersistableUriPermission()` for SAF URIs you need after app restart
- `IS_PENDING = 1` pattern in MediaStore prevents partial files from appearing in Gallery
- Scoped storage (API 29+): apps can only write to their own external files dir without `MANAGE_EXTERNAL_STORAGE`
- Use `FileProvider` for sharing files from internal storage with other apps
