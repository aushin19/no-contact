# Security & Privacy Samples — Reference

**GitHub**: [android/security-samples](https://github.com/android/security-samples)

---

## Patterns Covered

1. Biometric authentication
2. Android Keystore (encrypt/decrypt)
3. Runtime permissions (Compose)
4. Certificate pinning
5. EncryptedSharedPreferences

---

## 1. Biometric Authentication

```kotlin
val biometricManager = BiometricManager.from(context)

// Check capability
when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
    BiometricManager.BIOMETRIC_SUCCESS -> showBiometricPrompt()
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> showPinFallback()
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> promptEnrollment()
}

// Show prompt
val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Authenticate")
    .setSubtitle("Use biometric to access your data")
    .setNegativeButtonText("Cancel")
    .build()

val biometricPrompt = BiometricPrompt(
    activity,
    ContextCompat.getMainExecutor(context),
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            val cryptoObject = result.cryptoObject // use for crypto ops
            onSuccess(cryptoObject)
        }
        override fun onAuthenticationError(code: Int, msg: CharSequence) { onError(code) }
        override fun onAuthenticationFailed() { onFailed() }
    }
)

biometricPrompt.authenticate(promptInfo)
```

**Deps**:
```kotlin
implementation("androidx.biometric:biometric:1.2.x")
```

---

## 2. Android Keystore — Encrypt/Decrypt

```kotlin
object KeystoreHelper {
    private const val KEY_ALIAS = "my_secure_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    fun generateKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) return

        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
            )
            generateKey()
        }
    }

    fun encrypt(plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey) }
        return Pair(cipher.iv, cipher.doFinal(plaintext))
    }

    fun decrypt(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val spec = GCMParameterSpec(128, iv)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.DECRYPT_MODE, secretKey, spec) }
        return cipher.doFinal(ciphertext)
    }
}
```

---

## 3. Runtime Permissions (Compose)

```kotlin
// Single permission
val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

LaunchedEffect(Unit) {
    if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
}

when {
    cameraPermission.status.isGranted -> CameraScreen()
    cameraPermission.status.shouldShowRationale -> PermissionRationale(
        onRequest = { cameraPermission.launchPermissionRequest() }
    )
    else -> PermissionDeniedScreen()
}

// Multiple permissions
val permissions = rememberMultiplePermissionsState(
    listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
) { permMap ->
    val allGranted = permMap.values.all { it }
    if (allGranted) startRecording()
}
```

**Deps**:
```kotlin
implementation("com.google.accompanist:accompanist-permissions:0.36.x")
```

---

## 4. EncryptedSharedPreferences

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

encryptedPrefs.edit().putString("auth_token", token).apply()
val token = encryptedPrefs.getString("auth_token", null)
```

**Deps**:
```kotlin
implementation("androidx.security:security-crypto:1.1.x")
```

---

## Key Notes

- Never store secrets in `BuildConfig` or `strings.xml` — use Keystore + server-side secrets
- Biometric + Keystore combo: tie decryption to biometric auth via `setUserAuthenticationRequired(true)`
- Certificate pinning: use OkHttp `CertificatePinner` or Network Security Config XML
- `EncryptedSharedPreferences` uses Tink internally; suitable for tokens, not large data
