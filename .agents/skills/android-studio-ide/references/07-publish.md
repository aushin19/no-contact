# Android Studio — Publishing Your App

**Sources:**
- https://developer.android.com/studio/publish
- https://developer.android.com/studio/publish/app-signing
- https://developer.android.com/studio/publish/versioning
- https://developer.android.com/guide/app-bundle

---

## Publishing Checklist

Before publishing:
- [ ] Set final `versionCode` and `versionName`
- [ ] Set `minSdk` appropriate for target audience
- [ ] Enable R8/ProGuard (`isMinifyEnabled = true`)
- [ ] Enable resource shrinking (`isShrinkResources = true`)
- [ ] Test release build on physical device
- [ ] Remove all debug logging / test endpoints
- [ ] Verify all required permissions in manifest
- [ ] Add Privacy Policy URL (required for Play)
- [ ] Test in-app purchases / subscriptions
- [ ] Run Lint → fix warnings/errors
- [ ] Sign with release keystore (not debug)
- [ ] Build AAB (preferred over APK for Play)

---

## App Versioning

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 10          // integer, must increment with every release
        versionName = "2.1.0"     // user-visible string
    }
}
```

**`versionCode`:** Google Play uses this to determine upgrade eligibility. Must be higher than previous release. Max: 2,100,000,000.

**`versionName`:** Displayed to users. Use semantic versioning: `MAJOR.MINOR.PATCH`.

**Automate versionCode** (CI pattern):
```kotlin
// In build.gradle.kts — use git commit count
val gitCommitCount = "git rev-list --count HEAD".execute().text.trim().toInt()
versionCode = gitCommitCount
```

---

## App Signing

### Why Signing Matters
- Google Play requires all apps to be signed
- Play App Signing (recommended): Google manages your upload key + app signing key
- Upload key: used to authenticate uploads to Play Console
- App signing key: used to sign APKs delivered to users

### Generate Upload Keystore
**Build > Generate Signed Bundle / APK** → Create new...

Or via keytool CLI:
```bash
keytool -genkey -v \
  -keystore upload-keystore.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**CRITICAL:** Back up your keystore file + passwords. Losing the keystore = cannot update the app.

### Configure Signing in Gradle

**Secure method — from environment or properties file:**
```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../keystores/upload.jks")
            storePassword = System.getenv("STORE_PASSWORD") ?: properties["storePassword"].toString()
            keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
            keyPassword = System.getenv("KEY_PASSWORD") ?: properties["keyPassword"].toString()
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

Store secrets in `~/.gradle/gradle.properties` (not in repo):
```properties
storePassword=myStorePass
keyPassword=myKeyPass
```

### Build Signed App — GUI Method
**Build > Generate Signed Bundle / APK**:
1. Choose **Android App Bundle** (recommended) or APK
2. Select keystore → enter passwords
3. Choose **release** build variant
4. Click **Create**
5. Output: `app/release/app-release.aab` or `app-release.apk`

---

## Android App Bundle (AAB) vs APK

| | AAB | APK |
|-|-----|-----|
| **Play Store** | Required (since Aug 2021) | Legacy support |
| **Size** | ~15-20% smaller delivery | Full size |
| **Dynamic Delivery** | ✅ by density/ABI/language | ❌ |
| **Testing locally** | Requires bundletool | Direct install |
| **Enterprise/sideload** | ❌ | ✅ |

**Build AAB:**
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

**Test AAB locally with bundletool:**
```bash
# Install bundletool
# https://github.com/google/bundletool/releases

# Generate APKs set from AAB
bundletool build-apks \
  --bundle=app-release.aab \
  --output=app-release.apks \
  --ks=upload.jks \
  --ks-pass=pass:password \
  --ks-key-alias=upload \
  --key-pass=pass:password

# Install to connected device
bundletool install-apks --apks=app-release.apks
```

---

## R8 / ProGuard Shrinking

### Enable
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true       // code shrinking + obfuscation
        isShrinkResources = true     // remove unused resources
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### What R8 Does
1. **Shrinks** — removes unused classes, methods, fields
2. **Obfuscates** — renames to short meaningless names (a, b, c...)
3. **Optimizes** — inlines, removes dead code paths
4. **Dex** — converts to DEX format

### Common ProGuard Rules
```proguard
# proguard-rules.pro

# Keep data classes (used with Gson/Moshi/kotlinx.serialization)
-keep class com.example.app.data.model.** { *; }

# Keep Retrofit service interfaces
-keep interface com.example.app.network.** { *; }

# Keep enums
-keepclassmembers enum * { *; }

# Keep custom Application class
-keep class com.example.app.MyApplication { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# If using Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Keep crash reporting stack traces readable
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Suppress warnings for missing classes (use carefully)
-dontwarn com.some.library.**
```

### Troubleshoot R8 Issues
1. Build release → check for R8 errors
2. Run app → if crashes: check Logcat for `ClassNotFoundException` / `MethodNotFoundException`
3. Find missing rule: check library docs for ProGuard rules (most include `consumer-rules.pro`)
4. Add missing `-keep` rule for affected class
5. Debug with: `./gradlew assembleRelease --info` for R8 decision log

### Deobfuscate Crash Stack Traces
```bash
# retrace tool (in SDK build-tools)
$ANDROID_HOME/build-tools/34.0.0/retrace \
  app/build/outputs/mapping/release/mapping.txt \
  stack-trace.txt
```

Keep `mapping.txt` for every released version. Upload to Play Console for automatic deobfuscation.

---

## Dynamic Feature Modules

Split app into on-demand downloadable modules:

```kotlin
// feature/premium/build.gradle.kts
plugins {
    id("com.android.dynamic-feature")
    id("org.jetbrains.kotlin.android")
}
android {
    // no applicationId — inherits from base
}
dependencies {
    implementation(project(":app"))  // base module
}
```

```kotlin
// Request download at runtime
val splitInstallManager = SplitInstallManagerFactory.create(context)
val request = SplitInstallRequest.newBuilder()
    .addModule("premium")
    .build()
splitInstallManager.startInstall(request)
    .addOnSuccessListener { sessionId -> /* module downloaded */ }
    .addOnFailureListener { exception -> /* handle error */ }
```

---

## Upload to Google Play

### First Upload (New App)
1. **Play Console** → Create app
2. Fill in: app name, default language, app/game, free/paid
3. Complete Store Listing: description, screenshots, icon, feature graphic
4. Set Content Rating (IARC questionnaire)
5. Set Pricing & Distribution

### Upload Build
**Play Console > Release > Testing/Production > Create new release:**
1. Upload `.aab` file
2. Add release notes
3. Review → Rollout to track

Or via Android Studio: **Build > Generate Signed Bundle** → follow prompts → option to upload directly.

### Release Tracks
| Track | Purpose |
|-------|---------|
| **Internal testing** | Up to 100 testers; instant publish |
| **Closed testing (Alpha)** | Specific testers/groups |
| **Open testing (Beta)** | Public opt-in |
| **Production** | All users; supports staged rollout |

**Staged rollout:** Release to % of users (e.g., 10% → 50% → 100%) over days. Monitor crash rates before expanding.

---

## Play Feature Delivery

Three delivery modes:
```kotlin
// In dynamic feature module AndroidManifest.xml
<dist:module dist:instant="false">
    <!-- Install-time: always delivered with base app -->
    <dist:delivery>
        <dist:install-time/>
    </dist:delivery>

    <!-- On-demand: requested at runtime -->
    <dist:delivery>
        <dist:on-demand/>
    </dist:delivery>

    <!-- Conditional: based on device config -->
    <dist:delivery>
        <dist:install-time>
            <dist:conditions>
                <dist:user-countries dist:include="true">
                    <dist:country dist:code="IN"/>
                </dist:user-countries>
            </dist:conditions>
        </dist:install-time>
    </dist:delivery>
</dist:module>
```

---

## Play Policy Insights (Android Studio Integration)

**View > Tool Windows > Play Policy Insights** (Iguana+)

Shows policy violations detected in your code:
- Uses sensitive permissions incorrectly
- Potential policy issues before upload
- Links to relevant Play policy pages

Requires: signed in with Play Console account in Android Studio.
