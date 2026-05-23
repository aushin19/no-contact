# Android Studio — Testing

**Sources:**
- https://developer.android.com/studio/test
- https://developer.android.com/studio/test/test-in-android-studio
- https://developer.android.com/studio/test/managed-devices
- https://developer.android.com/training/testing/fundamentals

---

## Test Types Overview

| Type | Location | Runs On | Speed | Use For |
|------|----------|---------|-------|---------|
| **Unit Test** | `src/test/` | JVM (local) | Fast | Logic, ViewModels, repos |
| **Instrumented Test** | `src/androidTest/` | Device/Emulator | Slow | Database, UI, intents |
| **UI Test (Compose)** | `src/androidTest/` | Device/Emulator | Medium | Compose UI interactions |
| **UI Test (Espresso)** | `src/androidTest/` | Device/Emulator | Slow | View-based UI |

**Strategy:** Test pyramid — many unit tests, fewer instrumented, minimal E2E.

---

## Dependencies Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Unit testing
    testImplementation(libs.junit)                              // JUnit 4
    testImplementation(libs.kotlinx.coroutines.test)           // coroutines
    testImplementation(libs.mockk)                             // mocking (MockK)
    // OR
    testImplementation(libs.mockito.kotlin)                    // mocking (Mockito)
    testImplementation(libs.turbine)                           // Flow testing
    testImplementation(libs.truth)                             // Google Truth assertions
    testImplementation(libs.androidx.arch.core.testing)        // LiveData testing

    // Instrumented testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Room testing
    testImplementation(libs.androidx.room.testing)
    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)
}
```

---

## Unit Tests

### Basic JUnit 4 Test
```kotlin
// src/test/java/com/example/app/CalculatorTest.kt
class CalculatorTest {

    private lateinit var calculator: Calculator

    @Before
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    fun `addition returns correct result`() {
        val result = calculator.add(2, 3)
        assertEquals(5, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `divide by zero throws exception`() {
        calculator.divide(10, 0)
    }

    @After
    fun tearDown() { /* cleanup */ }
}
```

### ViewModel Unit Test
```kotlin
class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val fakeRepo = FakeUserRepository()
    private lateinit var viewModel: UserViewModel

    @Before
    fun setUp() {
        viewModel = UserViewModel(fakeRepo)
    }

    @Test
    fun `loading users updates uiState`() = runTest {
        viewModel.loadUsers()
        val state = viewModel.uiState.value
        assertIs<UserUiState.Success>(state)
        assertEquals(2, state.users.size)
    }
}
```

### Coroutine Testing
```kotlin
class RepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetch data completes successfully`() = runTest {
        val repo = UserRepository(fakeApi)
        val result = repo.getUsers()
        assertTrue(result.isSuccess)
    }
}
```

### Flow Testing with Turbine
```kotlin
@Test
fun `state flow emits correct sequence`() = runTest {
    viewModel.uiState.test {
        // First emission
        assertEquals(UserUiState.Loading, awaitItem())
        // Second emission
        assertIs<UserUiState.Success>(awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Running Tests in Android Studio

### Run Options
- **Right-click test file** > Run 'TestName' — runs single file
- **Right-click test class** > Run — runs all tests in class
- **Right-click test method** > Run — runs single test
- **Run > Run 'All Tests'** — entire test suite
- Gutter ▶ icon next to class/method — quick run

### Test Results
**Run tool window** (`Alt+4`):
- Green ✓ = passed
- Red ✗ = failed (shows stack trace + diff for assertions)
- Yellow ⚪ = skipped / ignored
- Click test node → jump to source
- **Re-run failed tests** button (↻ icon)

### Gradle CLI
```bash
# Unit tests
./gradlew test
./gradlew testDebugUnitTest         # specific variant
./gradlew :app:testDebugUnitTest --tests "*.CalculatorTest"

# Instrumented tests (device must be connected)
./gradlew connectedAndroidTest
./gradlew connectedDebugAndroidTest

# Reports
open app/build/reports/tests/testDebugUnitTest/index.html
```

---

## Instrumented Tests

### Setup
```kotlin
// src/androidTest/java/com/example/app/ExampleInstrumentedTest.kt
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.app", appContext.packageName)
    }
}
```

### Room Database Test
```kotlin
@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.userDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertAndReadUser() = runTest {
        val user = User(id = 1, name = "Alice")
        dao.insert(user)
        val loaded = dao.getUser(1)
        assertEquals(user.name, loaded.name)
    }
}
```

---

## Compose UI Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginButton_disabledWhenFieldsEmpty() {
        composeTestRule.setContent {
            MyAppTheme { LoginScreen(onLoginSuccess = {}) }
        }

        // Find by test tag
        composeTestRule.onNodeWithTag("loginButton")
            .assertIsNotEnabled()

        // Find by text
        composeTestRule.onNodeWithText("Login")
            .assertIsDisplayed()
    }

    @Test
    fun enterCredentials_enablesLoginButton() {
        composeTestRule.setContent {
            MyAppTheme { LoginScreen(onLoginSuccess = {}) }
        }

        composeTestRule.onNodeWithTag("emailField")
            .performTextInput("user@example.com")

        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("password123")

        composeTestRule.onNodeWithTag("loginButton")
            .assertIsEnabled()
            .performClick()
    }
}
```

### Compose Test Matchers
```kotlin
// Finders
onNodeWithText("Login")
onNodeWithTag("myTag")
onNodeWithContentDescription("Close")
onAllNodesWithTag("listItem")

// Assertions
.assertExists()
.assertIsDisplayed()
.assertIsEnabled() / .assertIsNotEnabled()
.assertIsSelected() / .assertIsNotSelected()
.assertHasClickAction()
.assertTextEquals("expected text")
.assertCountEquals(3)  // for onAllNodes

// Actions
.performClick()
.performTextInput("text")
.performTextClearance()
.performScrollTo()
.performScrollToIndex(5)
.performTouchInput { swipeUp() }
```

---

## Espresso Tests (View-based UI)

```kotlin
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun clickButton_showsResult() {
        // Find and click button
        onView(withId(R.id.btnSubmit))
            .check(matches(isDisplayed()))
            .perform(click())

        // Verify result text
        onView(withId(R.id.tvResult))
            .check(matches(withText("Success")))
    }

    @Test
    fun typeInEditText() {
        onView(withId(R.id.etUsername))
            .perform(typeText("alice"), closeSoftKeyboard())

        onView(withId(R.id.etUsername))
            .check(matches(withText("alice")))
    }

    @Test
    fun recyclerViewItem() {
        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(5))

        onView(withId(R.id.recyclerView))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(5, click()))
    }
}
```

---

## Managed Devices (Gradle Managed Devices)

Run instrumented tests on Google-managed virtual devices — no local emulator required.

Configure in `app/build.gradle.kts`:
```kotlin
android {
    testOptions {
        managedDevices {
            localDevices {
                create("pixel6api34") {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "aosp-atd"  // automated testing device
                }
            }
            groups {
                create("phoneAndTablet") {
                    targetDevices.addAll(
                        devices.getByName("pixel6api34"),
                        devices.getByName("pixelTabletApi33")
                    )
                }
            }
        }
    }
}
```

Run:
```bash
./gradlew pixel6api34DebugAndroidTest        # single device
./gradlew phoneAndTabletGroupDebugAndroidTest # group
```

---

## Code Coverage

### Enable in Android Studio
Run > **Run 'Tests' with Coverage** (coverage icon in Run toolbar)

### View Coverage Report
After run: **Coverage** tool window shows:
- Per-class coverage %
- Per-method coverage %
- Line highlighting: green = covered, red = uncovered, yellow = partial branch

### Configure Coverage Reports
```kotlin
android {
    testOptions {
        unitTests.all {
            it.configure {
                // Generate HTML + XML coverage reports
                extensions.configure<JacocoTaskExtension> {
                    isIncludeNoLocationClasses = true
                }
            }
        }
    }
}
```

CLI: `./gradlew createDebugCoverageReport`
Report: `app/build/reports/coverage/debug/index.html`

---

## Test Configuration Best Practices

### `TestApplication` for Dependency Injection
```kotlin
// src/androidTest/
@HiltAndroidApp
class TestApp : Application()

// In test AndroidManifest.xml
<application android:name=".TestApp" />
```

### Fake vs Mock
- **Fakes** (preferred) — real implementations with in-memory data
- **Mocks** (MockK/Mockito) — verify interactions, stub return values

### Test Naming Convention
```kotlin
// Pattern: functionName_condition_expectedResult
fun `getUserById_validId_returnsUser`()
fun `login_invalidPassword_returnsError`()
// OR: Given_When_Then
fun `given invalid email when login clicked then shows error`()
```

### Flaky Test Mitigation
- Use `IdlingResource` for async Espresso tests
- `runTest` with `TestDispatcher` for coroutines
- `composeTestRule.waitForIdle()` before assertions
- Avoid `Thread.sleep()`; use `awaitItem()` (Turbine) or `advanceTimeBy()`
