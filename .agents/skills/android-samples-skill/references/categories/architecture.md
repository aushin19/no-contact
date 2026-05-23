# Architecture Samples — Reference

**GitHub**: [android/architecture-samples](https://github.com/android/architecture-samples)
**GitHub**: [android/nowinandroid](https://github.com/android/nowinandroid)

---

## Patterns Covered

1. MVVM with ViewModel + StateFlow
2. Repository pattern
3. Hilt dependency injection
4. Navigation Compose
5. Use Cases (Interactors)
6. Unidirectional Data Flow (UDF)
7. Multi-module architecture

---

## 1. MVVM — ViewModel + StateFlow

```kotlin
// UiState sealed class
sealed interface TasksUiState {
    object Loading : TasksUiState
    data class Success(val tasks: List<Task>) : TasksUiState
    data class Error(val message: String) : TasksUiState
}

// ViewModel
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init { loadTasks() }

    private fun loadTasks() {
        viewModelScope.launch {
            tasksRepository.getTasks()
                .catch { e -> _uiState.value = TasksUiState.Error(e.message ?: "Unknown") }
                .collect { tasks -> _uiState.value = TasksUiState.Success(tasks) }
        }
    }
}
```

**Deps**:
```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.x")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.x")
```

---

## 2. Repository Pattern

```kotlin
interface TasksRepository {
    fun getTasks(): Flow<List<Task>>
    suspend fun addTask(task: Task)
    suspend fun deleteTask(id: String)
}

class DefaultTasksRepository @Inject constructor(
    private val localDataSource: TasksLocalDataSource,
    private val remoteDataSource: TasksRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TasksRepository {

    override fun getTasks(): Flow<List<Task>> =
        localDataSource.getTasks()

    override suspend fun addTask(task: Task) = withContext(ioDispatcher) {
        localDataSource.insertTask(task)
        remoteDataSource.saveTask(task)
    }
}
```

---

## 3. Hilt DI — Full Setup

```kotlin
// App
@HiltAndroidApp
class MyApp : Application()

// Module
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db").build()

    @Provides @Singleton
    fun provideTasksRepository(db: AppDatabase): TasksRepository =
        DefaultTasksRepository(db.tasksDao())
}

// Dispatcher binding
@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module @InstallIn(SingletonComponent::class)
object CoroutinesModule {
    @Provides @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

**Deps**:
```kotlin
implementation("com.google.dagger:hilt-android:2.51.x")
ksp("com.google.dagger:hilt-android-compiler:2.51.x")
implementation("androidx.hilt:hilt-navigation-compose:1.2.x")
```

---

## 4. Navigation Compose

```kotlin
// Route definitions
sealed class Screen(val route: String) {
    object TaskList : Screen("tasks")
    object TaskDetail : Screen("tasks/{taskId}") {
        fun createRoute(id: String) = "tasks/$id"
    }
    object AddTask : Screen("add_task")
}

// NavHost setup
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = Screen.TaskList.route) {

        composable(Screen.TaskList.route) {
            TaskListScreen(
                onTaskClick = { id ->
                    navController.navigate(Screen.TaskDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(taskId = taskId, onBack = { navController.popBackStack() })
        }
    }
}
```

**Deps**:
```kotlin
implementation("androidx.navigation:navigation-compose:2.8.x")
```

---

## 5. Use Case / Interactor

```kotlin
// Single-responsibility use case
class GetSortedTasksUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(sortOrder: SortOrder): Flow<List<Task>> =
        repository.getTasks().map { tasks ->
            when (sortOrder) {
                SortOrder.BY_DATE -> tasks.sortedBy { it.createdAt }
                SortOrder.BY_PRIORITY -> tasks.sortedByDescending { it.priority }
            }
        }
}

// In ViewModel
val sortedTasks = getSortedTasksUseCase(SortOrder.BY_DATE)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

---

## 6. Unidirectional Data Flow (UDF)

```kotlin
// Events from UI → ViewModel
sealed interface TasksEvent {
    data class DeleteTask(val id: String) : TasksEvent
    object RefreshTasks : TasksEvent
    data class ToggleComplete(val id: String, val complete: Boolean) : TasksEvent
}

// ViewModel handles events
fun onEvent(event: TasksEvent) {
    viewModelScope.launch {
        when (event) {
            is TasksEvent.DeleteTask -> repository.deleteTask(event.id)
            is TasksEvent.RefreshTasks -> loadTasks()
            is TasksEvent.ToggleComplete -> repository.updateTask(event.id, event.complete)
        }
    }
}
```

---

## 7. Multi-Module Setup (Now in Android pattern)

```
:app              → entry point, DI wiring
:core:data        → repositories, data sources
:core:domain      → use cases, models
:core:ui          → shared composables, theme
:core:database    → Room DAOs
:core:network     → Retrofit services
:feature:taskList → screen + ViewModel
:feature:taskDetail
```

settings.gradle.kts:
```kotlin
include(":app", ":core:data", ":core:domain", ":core:ui",
        ":feature:taskList", ":feature:taskDetail")
```

---

## Key Notes

- Prefer `StateFlow` over `LiveData` in new projects
- Use `stateIn(WhileSubscribed(5_000))` to survive config changes without over-collecting
- `@HiltViewModel` requires `@AndroidEntryPoint` on host Activity/Fragment
- Now in Android uses type-safe navigation via `@Serializable` data classes (Navigation 2.8+)
- For testing: use `TestCoroutineDispatcher` + `hilt-testing` artifact
