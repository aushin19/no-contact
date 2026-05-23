# Android Studio — Project Structure & Gradle Build System

**Sources:**
- https://developer.android.com/studio/projects
- https://developer.android.com/build
- https://developer.android.com/build/build-variants

---

## Project Structure on Disk

```
MyApp/                              ← project root
├── .gradle/                        ← Gradle cache (auto-generated, don't commit)
├── .idea/                          ← IDE settings (commit selectively)
├── app/                            ← app module
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/pkg/app/   ← Kotlin/Java source
│   │   │   └── res/                ← resources
│   │   ├── test/                   ← local unit tests (JVM)
│   │   └── androidTest/            ← instrumented tests (device)
│   └── build.gradle.kts            ← module-level build script
├── gradle/
│   ├── libs.versions.toml          ← Version Catalog (recommended)
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties  ← Gradle version
├── build.gradle.kts                ← top-level build script
├── settings.gradle.kts             ← module declarations
├── gradle.properties               ← JVM/Gradle flags
└── local.properties                ← SDK path (don't commit!)
```

---

## Module Types

| Type | Purpose |
|------|---------|
| **Android App Module** | Produces APK/AAB, has AndroidManifest |
| **Android Library Module** | Produces AAR, reusable across apps |
| **Java/Kotlin Library** | Pure JVM, produces JAR |
| **Google App Engine Module** | Backend module |
| **Dynamic Feature Module** | On-demand delivery via Play |

Add module: **File > New > New Module**

---

## Gradle Build Files Explained

### `settings.gradle.kts` (project root)
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "MyApp"
include(":app")
include(":feature:login")  // additional modules
```

### `build.gradle.kts` (top-level)
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

### `app/build.gradle.kts` (module-level)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true          // R8/ProGuard shrinking
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true   // generates BuildConfig class
        viewBinding = true   // enables ViewBinding
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
```

---

## Version Catalog (`gradle/libs.versions.toml`)

Recommended for centralizing dependency versions:

```toml
[versions]
agp = "8.7.0"
kotlin = "2.0.21"
coreKtx = "1.15.0"
composeBom = "2024.11.00"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

Reference in `build.gradle.kts`:
```kotlin
implementation(libs.androidx.core.ktx)
implementation(platform(libs.androidx.compose.bom))  // BOM — auto-manages Compose versions
```

---

## Build Variants

**Build Type** × **Product Flavor** = **Build Variant**

### Build Types
```kotlin
buildTypes {
    debug { /* default, debuggable */ }
    release { /* optimized, signed */ }
    // custom:
    create("staging") {
        initWith(getByName("debug"))
        applicationIdSuffix = ".staging"
    }
}
```

### Product Flavors
```kotlin
flavorDimensions += "tier"
productFlavors {
    create("free") {
        dimension = "tier"
        applicationIdSuffix = ".free"
        versionNameSuffix = "-free"
        buildConfigField("Boolean", "IS_PREMIUM", "false")
    }
    create("premium") {
        dimension = "tier"
        applicationIdSuffix = ".premium"
        buildConfigField("Boolean", "IS_PREMIUM", "true")
    }
}
```

**Resulting variants:** `freeDebug`, `freeRelease`, `premiumDebug`, `premiumRelease`

Switch variant: **Build > Select Build Variant** or the **Build Variants** tool window.

### Source Sets
Each variant can have its own source set:
```
app/src/
├── main/         → all variants
├── debug/        → debug only
├── release/      → release only
├── free/         → free flavor only
└── freeDebug/    → freeDebug variant only
```

---

## Dependency Management

### Dependency Configurations
| Config | Purpose |
|--------|---------|
| `implementation` | Compile + runtime; NOT exposed to consumers |
| `api` | Compile + runtime; exposed to consumers (library modules) |
| `compileOnly` | Compile only; not in APK |
| `runtimeOnly` | Runtime only; not visible at compile time |
| `testImplementation` | Local unit tests only |
| `androidTestImplementation` | Instrumented tests only |
| `debugImplementation` | Debug builds only |

### Common Dependencies
```kotlin
dependencies {
    // Compose BOM (manages all Compose versions)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Architecture Components
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.okhttp3)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

### Check/Update Dependencies
- **Build > Analyze APK** → see what's bundled
- Gradle: `./gradlew app:dependencies` → full dependency tree
- **Tools > Dependency Analyzer** (in Studio) → visual graph

---

## Gradle Wrapper

Pins Gradle version per project. Located at `gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
```

Run Gradle tasks via wrapper:
```bash
./gradlew tasks                    # list all tasks
./gradlew assembleDebug            # build debug APK
./gradlew assembleRelease          # build release APK
./gradlew bundleRelease            # build release AAB
./gradlew test                     # run unit tests
./gradlew connectedAndroidTest     # run instrumented tests
./gradlew lint                     # run lint checks
./gradlew clean                    # clean build outputs
./gradlew app:dependencies         # dependency tree
./gradlew --profile assembleDebug  # profiled build report
```

### gradle.properties — Key Flags
```properties
# Increase JVM heap for Gradle daemon
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m

# Enable parallel builds (safe for independent modules)
org.gradle.parallel=true

# Enable configuration cache (faster second builds)
org.gradle.configuration-cache=true

# Enable build cache
org.gradle.caching=true

# Kotlin options
kotlin.code.style=official

# Android
android.useAndroidX=true
android.nonTransitiveRClass=true
```

---

## Project Structure Dialog

**File > Project Structure** (`Ctrl+Alt+Shift+S` / `Cmd+;`)

Tabs:
- **SDK Location** — set Android SDK, NDK, JDK paths
- **Project** — Gradle version, plugin version
- **Modules** — per-module SDK, source sets, dependencies
- **Dependencies** — add/remove/upgrade deps via GUI
- **Build Variants** — view all variants

---

## NDK / C++ Integration

Add to `app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
```

Install NDK: **SDK Manager > SDK Tools > NDK (Side by side)** + **CMake**

---

## Common Gradle Errors & Fixes

| Error | Fix |
|-------|-----|
| `Could not resolve` dependency | Check internet; add/verify repository in `settings.gradle.kts` |
| `Duplicate class` | Exclude transitive dep; use BOM |
| `minSdk X < library minSdk Y` | Raise `minSdk` or add `tools:overrideLibrary` |
| `Configuration cache problems` | Disable with `org.gradle.configuration-cache=false` or fix task |
| `OutOfMemoryError` | Increase `org.gradle.jvmargs` heap |
| `Build failed: Kotlin compilation error` | Check Kotlin + AGP version compatibility table |
| Slow builds | Enable caching, parallel builds; check task graph with `--scan` |

**Kotlin + AGP Compatibility:** https://developer.android.com/build/releases/gradle-plugin#updating-gradle
