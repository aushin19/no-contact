# Building Samples — Reference

**Docs**: [developer.android.com/build](https://developer.android.com/build)

---

## Patterns Covered

1. Version Catalog (libs.versions.toml)
2. Build types + flavors
3. KSP vs KAPT
4. BuildConfig fields
5. Multi-module dependency management

---

## 1. Version Catalog (libs.versions.toml)

```toml
# gradle/libs.versions.toml
[versions]
agp = "8.5.x"
kotlin = "2.0.x"
compose-bom = "2024.09.x"
hilt = "2.51.x"
room = "2.6.x"
retrofit = "2.11.x"

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.x-1.0.x" }
```

```kotlin
// build.gradle.kts (app)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
```

---

## 2. Build Types + Flavors

```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            buildConfigField("String", "BASE_URL", "\"https://api-dev.example.com/\"")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.x"
    }
}
```

---

## 3. KSP vs KAPT

```
KAPT (deprecated):  slow, generates Java stubs, avoid for new projects
KSP:                2x+ faster, Kotlin-native, use for Room, Hilt, Moshi

Migration: replace kapt("...") → ksp("...")
           add plugin: alias(libs.plugins.ksp)
```

---

## 4. BuildConfig Fields

```kotlin
// Define in build.gradle.kts
buildConfigField("String", "API_KEY", "\"${properties["API_KEY"]}\"")
buildConfigField("Boolean", "ENABLE_LOGGING", "true")
buildConfigField("int", "VERSION_CODE_OFFSET", "1000")

// local.properties (git-ignored)
API_KEY=abc123xyz

// Read local.properties
val localProps = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}
```

---

## 5. Multi-Module Convention Plugins

```kotlin
// build-logic/convention/src/main/kotlin/AndroidLibraryConventionPlugin.kt
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }
            extensions.configure<LibraryExtension> {
                compileSdk = 35
                defaultConfig.minSdk = 26
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
    }
}
```

---

## Key Notes

- Version catalogs are the standard since AGP 8.0 — avoid hardcoded versions in build files
- KSP is faster than KAPT — migrate all annotation processors (Room, Hilt, Moshi)
- Never commit API keys to git — use `local.properties` + CI secrets
- `isMinifyEnabled = true` + `isShrinkResources = true` for release = smaller APK
- Convention plugins = DRY build config across modules (Now in Android pattern)
- `compileSdk 35`, `targetSdk 35`, `minSdk 26` are current recommended (2025) baselines
