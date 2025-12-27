# 🛡️ Defense Implementation Guide for Attack Vector 1
## Bảo vệ App khỏi APK Decompilation & Reverse Engineering

> **🎯 Mục tiêu:** Triển khai các biện pháp bảo vệ toàn diện để ngăn chặn decompilation, reverse engineering, và information disclosure đã được chứng minh thành công trong Attack Vector 1.

---

## 📚 Mục lục

1. [Tổng quan về Threat Model](#phần-1-tổng-quan-threat-model)
2. [Defense Layer 1: Code Obfuscation](#phần-2-defense-layer-1-code-obfuscation)
3. [Defense Layer 2: String Encryption](#phần-3-defense-layer-2-string-encryption)
4. [Defense Layer 3: API Protection](#phần-4-defense-layer-3-api-protection)
5. [Defense Layer 4: Runtime Protection](#phần-5-defense-layer-4-runtime-protection)
6. [Defense Layer 5: Network Security](#phần-6-defense-layer-5-network-security)
7. [Defense Layer 6: Anti-Tampering](#phần-7-defense-layer-6-anti-tampering)
8. [Verification & Testing](#phần-8-verification--testing)

---

## Phần 1: Tổng quan Threat Model

### A. Kết quả Attack Vector 1 - Những gì đã bị lộ

Sau khi thực hiện thành công Attack Vector 1 (decompile APK với JADX), attacker có thể dễ dàng thu thập:

**🔴 Critical Information Exposed:**

1. **API Endpoints** - Tất cả backend URLs
   ```
   http://10.0.2.2:8080/api/auth/login
   http://10.0.2.2:8080/api/auth/register
   http://10.0.2.2:8080/api/wordsearch/
   http://10.0.2.2:8080/api/batchu/
   http://10.0.2.2:8080/api/sound/levels
   ```

2. **Google OAuth Client ID**
   ```
   1026710210552-v084tbclfnhotnrv4lvf5i0hgppihk2r.apps.googleusercontent.com
   ```

3. **Play Integrity Project Number**
   ```
   177486006662
   ```

4. **Expected Signature Hash**
   ```
   EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:...
   ```

5. **Complete Business Logic**
   - Authentication flow
   - Data encryption methods
   - Database structure (from API calls)
   - Game algorithms

**⚠️ Current Security Posture:**

| Security Feature | Status | Risk Level |
|-----------------|--------|------------|
| Code Obfuscation | ❌ Disabled (`isMinifyEnabled = false`) | CRITICAL |
| String Encryption | ❌ None | HIGH |
| Root Detection | ❌ Not implemented | HIGH |
| Emulator Detection | ❌ Not implemented | MEDIUM |
| Debugger Detection | ❌ Not implemented | HIGH |
| Certificate Pinning | ❌ Not implemented | HIGH |
| API URL Protection | ❌ Hardcoded in code | CRITICAL |
| Debug Logging | ❌ Enabled in release | MEDIUM |

### B. Defense-in-Depth Strategy

Chúng ta sẽ triển khai **7 lớp bảo vệ** (Defense Layers):

```
┌─────────────────────────────────────────────────────┐
│  Layer 7: Anti-Tampering & Integrity Checks         │
├─────────────────────────────────────────────────────┤
│  Layer 6: Network Security (SSL Pinning)            │
├─────────────────────────────────────────────────────┤
│  Layer 5: Runtime Protection (Root/Debug Detection) │
├─────────────────────────────────────────────────────┤
│  Layer 4: API & Endpoint Protection                 │
├─────────────────────────────────────────────────────┤
│  Layer 3: String Encryption                         │
├─────────────────────────────────────────────────────┤
│  Layer 2: Resource Shrinking & Optimization         │
├─────────────────────────────────────────────────────┤
│  Layer 1: Code Obfuscation (ProGuard/R8)            │
└─────────────────────────────────────────────────────┘
```

**📊 Impact Analysis:**

| Defense Layer | Effectiveness | Implementation Difficulty | Performance Impact |
|--------------|---------------|--------------------------|-------------------|
| Code Obfuscation | ⭐⭐⭐⭐⭐ | Easy | None |
| String Encryption | ⭐⭐⭐⭐ | Medium | Minimal (~10ms) |
| API Protection | ⭐⭐⭐⭐⭐ | Medium | None |
| Runtime Protection | ⭐⭐⭐ | Medium | Low (~50-100ms startup) |
| Network Security | ⭐⭐⭐⭐ | Hard | None |
| Anti-Tampering | ⭐⭐⭐⭐ | Medium | Minimal |

---

## Phần 2: Defense Layer 1 - Code Obfuscation

### 🎯 Mục tiêu

Làm cho decompiled code cực kỳ khó đọc và hiểu bằng cách:
- Đổi tên classes, methods, fields thành tên vô nghĩa (a, b, c, d...)
- Loại bỏ unused code
- Optimize bytecode
- Remove debug information

### A. Enable ProGuard/R8 Obfuscation

**Bước 1: Modify `build.gradle.kts`**

```kotlin
// File: app/build.gradle.kts

android {
    // ... existing config ...
    
    buildTypes {
        release {
            // ✅ ENABLE OBFUSCATION
            isMinifyEnabled = true
            isShrinkResources = true
            
            // ✅ ProGuard configuration files
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // ✅ Signing configuration (required for release)
            signingConfig = signingConfigs.getByName("release")
        }
        
        debug {
            // Keep debug builds unobfuscated for development
            isMinifyEnabled = false
        }
    }
    
    // ✅ Add signing configuration
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your_keystore_password"
            keyAlias = "eduquizz"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "your_key_password"
        }
    }
}
```

**Bước 2: Configure ProGuard Rules**

```proguard
# File: app/proguard-rules.pro

# ═══════════════════════════════════════════════════════════
# EDUQUIZZ APP - PROGUARD CONFIGURATION
# ═══════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────
# 1. GENERAL OPTIMIZATION
# ─────────────────────────────────────────────────────────────

# Optimize and obfuscate aggressively
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Remove all logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# ─────────────────────────────────────────────────────────────
# 2. KEEP ESSENTIAL ANDROID COMPONENTS
# ─────────────────────────────────────────────────────────────

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ─────────────────────────────────────────────────────────────
# 3. RETROFIT, OKHTTP, GSON
# ─────────────────────────────────────────────────────────────

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ⚠️ IMPORTANT: Keep your data models for Gson/Retrofit
-keep class com.example.eduquizz.features.**.data.model.** { *; }
-keep class com.example.eduquizz.features.**.model.** { *; }

# ─────────────────────────────────────────────────────────────
# 4. HILT / DAGGER
# ─────────────────────────────────────────────────────────────

-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends javax.inject.Provider

-keepclasseswithmembers class * {
    @dagger.hilt.** *;
}

-keepclasseswithmembers class * {
    @dagger.** *;
}

# ─────────────────────────────────────────────────────────────
# 5. KOTLIN & COROUTINES
# ─────────────────────────────────────────────────────────────

-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ─────────────────────────────────────────────────────────────
# 6. JETPACK COMPOSE
# ─────────────────────────────────────────────────────────────

-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

-keepclassmembers class androidx.compose.** {
    *;
}

# ─────────────────────────────────────────────────────────────
# 7. FIREBASE & GOOGLE PLAY SERVICES
# ─────────────────────────────────────────────────────────────

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Play Integrity
-keep class com.google.android.play.core.integrity.** { *; }
-dontwarn com.google.android.play.core.integrity.**

# Google Sign-In
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ─────────────────────────────────────────────────────────────
# 8. SECURITY CLASSES (⚠️ PARTIAL OBFUSCATION)
# ─────────────────────────────────────────────────────────────

# ⚠️ Strategy: Keep class names but obfuscate method names and logic
# This makes it harder but not impossible to find security code

# Keep security package structure but obfuscate internals
-keep class com.example.eduquizz.security.** {
    public <init>(...);
}

# BUT allow obfuscation of method names and internal logic
-keepclassmembers class com.example.eduquizz.security.** {
    !public *;
}

# ─────────────────────────────────────────────────────────────
# 9. ENCRYPTED SHARED PREFERENCES
# ─────────────────────────────────────────────────────────────

-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ─────────────────────────────────────────────────────────────
# 10. REMOVE DEBUG & DEVELOPMENT CODE
# ─────────────────────────────────────────────────────────────

# Remove all Log calls
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
    public static int e(...);
}

# Remove printStackTrace
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

# Remove BuildConfig debug checks
-assumenosideeffects class com.example.eduquizz.BuildConfig {
    public static final boolean DEBUG return false;
}

# ─────────────────────────────────────────────────────────────
# 11. OBFUSCATE STRING CONSTANTS (Aggressive)
# ─────────────────────────────────────────────────────────────

# This will make string constants harder to find
# ⚠️ Warning: May break some reflection-based code
-adaptclassstrings
```

**Bước 3: Build và Test**

```powershell
# Clean previous builds
cd d:\Android\EduQuizzApp_v1
.\gradlew clean

# Build release APK with obfuscation
.\gradlew assembleRelease

# Check that mapping file was generated
# This file maps obfuscated names back to original names
ls app\build\outputs\mapping\release\

# You should see:
# - mapping.txt (very important - save this for debugging crashes!)
# - usage.txt (removed code report)
# - seeds.txt (kept classes report)
```

**Bước 4: Verify Obfuscation**

```powershell
# Decompile the obfuscated APK
cd d:\APK_Analysis
jadx app-release.apk -d release_obfuscated

# Open and check:
notepad release_obfuscated\sources\com\example\eduquizz\MainActivity.java

# ✅ You should see obfuscated code like:
# package a.b.c;
# public class d extends e {
#     private f g;
#     public void h() { ... }
# }
```

---

## Phần 3: Defense Layer 2 - String Encryption

### 🎯 Mục tiêu

Encrypt các string nhạy cảm trong code để khi decompile, attacker chỉ thấy gibberish.

### A. Create String Encryption Utility

**File mới: `app/src/main/java/com/example/eduquizz/security/StringEncryption.kt`**

```kotlin
package com.example.eduquizz.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object StringEncryption {
    
    private const val KEYSTORE_ALIAS = "eduquizz_string_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    
    init {
        generateOrGetKey()
    }
    
    /**
     * Generate or retrieve encryption key from Android Keystore
     */
    private fun generateOrGetKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        
        // Check if key already exists
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }
        
        // Generate new key
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false) // For deterministic encryption
            .build()
        
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
    
    /**
     * Encrypt a string
     * Returns Base64 encoded: IV + ciphertext
     */
    fun encrypt(plaintext: String): String {
        val key = generateOrGetKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        
        // Combine IV + ciphertext
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }
    
    /**
     * Decrypt a string
     */
    fun decrypt(encrypted: String): String {
        val key = generateOrGetKey()
        val combined = Base64.decode(encrypted, Base64.NO_WRAP)
        
        // Extract IV (first 12 bytes for GCM)
        val iv = combined.sliceArray(0 until 12)
        val ciphertext = combined.sliceArray(12 until combined.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }
    
    /**
     * Convenience wrapper for encrypted strings
     * Usage: private val secretUrl = EncryptedString("aGh3j2k4l...")
     */
    class EncryptedString(private val encrypted: String) {
        private val decrypted: String by lazy { decrypt(encrypted) }
        
        override fun toString(): String = decrypted
        fun get(): String = decrypted
    }
}
```

### B. Encrypt Sensitive Strings

**Helper tool để tạo encrypted strings:**

```kotlin
// Temporary code - run once to generate encrypted strings
// Then remove this code

fun main() {
    // Strings cần encrypt
    val secrets = mapOf(
        "API_BASE_URL" to "http://10.0.2.2:8080/",
        "GOOGLE_CLIENT_ID" to "1026710210552-v084tbclfnhotnrv4lvf5i0hgppihk2r.apps.googleusercontent.com",
        "PROJECT_NUMBER" to "177486006662",
        "EXPECTED_SIGNATURE" to "EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:4A:23:CD:F2:C5:69:D1:FE:20:E9:9E:C3:40:D4:50:57"
    )
    
    secrets.forEach { (name, value) ->
        val encrypted = StringEncryption.encrypt(value)
        println("$name:")
        println("\"$encrypted\"")
        println()
    }
}
```

**Output example (save these!):**
```
API_BASE_URL:
"aGh3ajJrNGxuNW1wNnE3cjhzOXQwdTF2MndleDN5NGFiY2Q="

GOOGLE_CLIENT_ID:
"bXlzZWNyZXRlbmNvZGVkc3RyaW5nZm9yZ29vZ2xl..."
...
```

---

## Phần 4: Defense Layer 3 - API Protection

### 🎯 Mục tiêu

Di chuyển tất cả hardcoded API URLs sang BuildConfig và encrypt chúng.

### A. Configure BuildConfig

**Modify `app/build.gradle.kts`:**

```kotlin
android {
    // ... existing config ...
    
    defaultConfig {
        // ... existing config ...
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            
            // ✅ ADD: BuildConfig fields for production
            buildConfigField("String", "API_BASE_URL", "\"http://your-production-server.com/\"")
            buildConfigField("String", "GOOGLE_CLIENT_ID_ENCRYPTED", "\"aGh3ajJrNGxu...\"") // encrypted value
            buildConfigField("String", "PROJECT_NUMBER_ENCRYPTED", "\"bXlzZWNyZXQ...\"") // encrypted value
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            
            proguardFiles(...)
        }
        
        debug {
            isMinifyEnabled = false
            
            // ✅ ADD: BuildConfig for development (emulator)
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
            buildConfigField("String", "GOOGLE_CLIENT_ID_ENCRYPTED", "\"aGh3ajJrNGxu...\"")
            buildConfigField("String", "PROJECT_NUMBER_ENCRYPTED", "\"bXlzZWNyZXQ...\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
    }
    
    // ✅ ENABLE BuildConfig generation
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
}
```

### B. Update Network Modules

**Modify `dI/AuthModule.kt`:**

```kotlin
package com.example.eduquizz.dI

import com.example.eduquizz.BuildConfig
import com.example.eduquizz.security.StringEncryption
import okhttp3.logging.HttpLoggingInterceptor
// ... other imports ...

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        // ✅ ONLY add logging in debug builds
        if (BuildConfig.ENABLE_LOGGING) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        
        return builder.build()
    }
    
    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(@AuthRetrofit okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            // ✅ USE BuildConfig instead of hardcoded URL
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context
    ): GoogleSignInClient {
        // ✅ DECRYPT the Google Client ID at runtime
        val clientId = StringEncryption.decrypt(BuildConfig.GOOGLE_CLIENT_ID_ENCRYPTED)
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
}
```

**Tương tự cho `NetworkModule.kt` và `MatchGameModule.kt`:**

```kotlin
// NetworkModule.kt
.baseUrl(BuildConfig.API_BASE_URL)

// MatchGameModule.kt
.baseUrl(BuildConfig.API_BASE_URL)
```

### C. Update Repository Classes

**Modify các Repository files:**

```kotlin
// WordSearchRepository.kt
companion object {
    // ✅ BEFORE: private const val BASE_URL = "http://10.0.2.2:8080/api/wordsearch/"
    // ✅ AFTER:
    private val BASE_URL = "${BuildConfig.API_BASE_URL}api/wordsearch/"
}

// SoundRepository.kt
// ✅ BEFORE: private val BASE_URL = "http://10.0.2.2:8080/"
// ✅ AFTER:
private val BASE_URL = BuildConfig.API_BASE_URL

// BatChuRepo.kt
// ✅ BEFORE: private const val BASE_URL = "http://10.0.2.2:8080/api/batchu/"
// ✅ AFTER:
private val BASE_URL = "${BuildConfig.API_BASE_URL}api/batchu/"
```

---

## Phần 5: Defense Layer 4 - Runtime Protection

### 🎯 Mục tiêu

Detect các môi trường nguy hiểm khi app đang chạy:
- Rooted devices
- Emulators/virtual machines
- Debuggers attached
- Hooking frameworks (Frida, Xposed)

### A. Root Detection

**File mới: `security/RootDetection.kt`**

```kotlin
package com.example.eduquizz.security

import android.os.Build
import java.io.File

object RootDetection {
    
    /**
     * Check if device is rooted
     * Returns true if root is detected
     */
    fun isRooted(): Boolean {
        return checkRootMethod1() || 
               checkRootMethod2() || 
               checkRootMethod3() ||
               checkRootMethod4()
    }
    
    /**
     * Method 1: Check for su binary
     */
    private fun checkRootMethod1(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        return paths.any { File(it).exists() }
    }
    
    /**
     * Method 2: Check for root management apps
     */
    private fun checkRootMethod2(): Boolean {
        val rootApps = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk"
        )
        
        return rootApps.any { packageExists(it) }
    }
    
    /**
     * Method 3: Check Build tags
     */
    private fun checkRootMethod3(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }
    
    /**
     * Method 4: Try executing su
     */
    private fun checkRootMethod4(): Boolean {
        return try {
            Runtime.getRuntime().exec("su")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun packageExists(packageName: String): Boolean {
        return try {
            // Will be provided via dependency injection in real implementation
            // For now, just check file system
            File("/data/data/$packageName").exists()
        } catch (e: Exception) {
            false
        }
    }
}
```

### B. Emulator Detection

**File mới: `security/EmulatorDetection.kt`**

```kotlin
package com.example.eduquizz.security

import android.os.Build

object EmulatorDetection {
    
    fun isEmulator(): Boolean {
        return checkEmulatorMethod1() ||
               checkEmulatorMethod2() ||
               checkEmulatorMethod3()
    }
    
    /**
     * Method 1: Check Build properties
     */
    private fun checkEmulatorMethod1(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                "google_sdk" == Build.PRODUCT)
    }
    
    /**
     * Method 2: Check for emulator-specific files
     */
    private fun checkEmulatorMethod2(): Boolean {
        return try {
            val file = java.io.File("/dev/socket/qemud")
            file.exists() || 
            java.io.File("/dev/qemu_pipe").exists() ||
            java.io.File("/system/lib/libc_malloc_debug_qemu.so").exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Method 3: Check phone number
     */
    private fun checkEmulatorMethod3(): Boolean {
        // Emulator default phone numbers
        // This requires TelephonyManager - implement if needed
        return false // Placeholder
    }
}
```

### C. Debugger Detection

**File mới: `security/DebuggerDetection.kt`**

```kotlin
package com.example.eduquizz.security

import android.os.Debug

object DebuggerDetection {
    
    fun isDebuggerConnected(): Boolean {
        return checkDebuggerMethod1() ||
               checkDebuggerMethod2() ||
               checkDebuggerMethod3()
    }
    
    /**
     * Method 1: Standard Android API
     */
    private fun checkDebuggerMethod1(): Boolean {
        return Debug.isDebuggerConnected()
    }
    
    /**
     * Method 2: Check if waiting for debugger
     */
    private fun checkDebuggerMethod2(): Boolean {
        return Debug.waitingForDebugger()
    }
    
    /**
     * Method 3: Check TracerPid (Linux anti-debug)
     */
    private fun checkDebuggerMethod3(): Boolean {
        return try {
            val status = java.io.File("/proc/self/status").readText()
            val tracerPid = status.lines()
                .firstOrNull { it.contains("TracerPid:") }
                ?.split(":")
                ?.getOrNull(1)
                ?.trim()
                ?.toIntOrNull() ?: 0
            
            tracerPid != 0 // If TracerPid > 0, debugger is attached
        } catch (e: Exception) {
            false
        }
    }
}
```

### D. Integrate Security Checks in Application

**Modify `MainApplication.kt`:**

```kotlin
package com.example.eduquizz

import android.app.Application
import android.app.AlertDialog
import android.util.Log
import com.example.eduquizz.security.*
import dagger.hilt.android.HiltAndroidApp
import kotlin.system.exitProcess

@HiltAndroidApp
class MainApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // ✅ RUN SECURITY CHECKS (Only in release builds)
        if (!BuildConfig.DEBUG) {
            performSecurityChecks()
        }
    }
    
    private fun performSecurityChecks() {
        var securityViolations = mutableListOf<String>()
        
        // 1. Root Detection
        if (RootDetection.isRooted()) {
            securityViolations.add("⚠️ Root access detected")
            // CRITICAL: Exit app on rooted device
            handleRootedDevice()
            return
        }
        
        // 2. Emulator Detection
        if (EmulatorDetection.isEmulator()) {
            securityViolations.add("⚠️ Emulator detected")
            // WARNING: Show warning but allow (optional)
            // For high-security apps: also exit here
        }
        
        // 3. Debugger Detection
        if (DebuggerDetection.isDebuggerConnected()) {
            securityViolations.add("⚠️ Debugger detected")
            // CRITICAL: Exit app if debugger attached
            handleDebuggerDetected()
            return
        }
        
        // 4. Signature Verification
        if (!SignatureUtils.verifyAppSignature(this)) {
            securityViolations.add("⚠️ Invalid app signature - possible tampering")
            // CRITICAL: Exit app on signature mismatch
            handleTamperedApp()
            return
        }
        
        // 5. Play Integrity (Background check - don't block startup)
        PlayIntegrityHelper.checkIntegrity(this) { passed, payload ->
            if (!passed) {
                Log.e("Security", "Play Integrity check failed")
                // Optional: Log to backend for monitoring
            }
        }
        
        if (securityViolations.isNotEmpty()) {
            Log.w("Security", "Security violations: ${securityViolations.joinToString()}")
        }
    }
    
    private fun handleRootedDevice() {
        // Show dialog and exit
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            AlertDialog.Builder(this)
                .setTitle("Security Warning")
                .setMessage("This app cannot run on rooted devices for security reasons.")
                .setCancelable(false)
                .setPositiveButton("Exit") { _, _ ->
                    exitProcess(0)
                }
                .show()
        }
    }
    
    private fun handleDebuggerDetected() {
        // Silent exit - don't give attacker information
        exitProcess(0)
    }
    
    private fun handleTamperedApp() {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            AlertDialog.Builder(this)
                .setTitle("Security Error")
                .setMessage("App integrity check failed. Please download from official source.")
                .setCancelable(false)
                .setPositiveButton("Exit") { _, _ ->
                    exitProcess(0)
                }
                .show()
        }
    }
}
```

---

## Phần 6: Defense Layer 5 - Network Security

### Certificate Pinning (SSL Pinning)

**Modify Network Modules to add Certificate Pinning:**

```kotlin
// In NetworkModule.kt or AuthModule.kt

import okhttp3.CertificatePinner

@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    // ✅ Get your server's certificate pins
    // Use: openssl s_client -connect your-server.com:443 -showcerts
    // Then: openssl x509 -pubkey -noout -in cert.pem | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | base64
    
    val certificatePinner = CertificatePinner.Builder()
        .add("your-production-server.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=") // Your server pin
        .add("your-production-server.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBB BBBBBBBBBBBBBB=") // Backup pin
        .build()
    
    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner) // ✅ Add certificate pinning
        .addInterceptor(...)
        .build()
}
```

---

## Phần 7: Defense Layer 6 - Anti-Tampering

### Update SignatureUtils

**Modify `security/SignatureUtils.kt`:**

```kotlin
package com.example.eduquizz.security

import android.content.Context
import com.example.eduquizz.BuildConfig
import // ... other imports

object SignatureUtils {
    
    // ✅ BEFORE: val expected = "EC:0E:5E:7D:..."
    // ✅ AFTER: Encrypted expected signature
    private val EXPECTED_SIGNATURE_ENCRYPTED = "your_encrypted_signature_here"
    
    fun verifyAppSignature(context: Context): Boolean {
        // ✅ Decrypt expected signature at runtime
        val expected = StringEncryption.decrypt(EXPECTED_SIGNATURE_ENCRYPTED)
        
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            if (signatures == null || signatures.isEmpty()) return false
            
            val md = MessageDigest.getInstance("SHA-256")
            val hex = md.digest(signatures[0].toByteArray()).joinToString(":") {
                "%02X".format(it)
            }
            
            // ✅ REMOVED: Log.d("SIGNATURE", hex)  - Don't log signature!
            
            hex == expected
            
        } catch (e: Exception) {
            // ✅ REMOVED: e.printStackTrace()  - Don't leak stack trace
            false
        }
    }
}
```

---

## Phần 8: Verification & Testing

### A. Build Protected APK

```powershell
cd d:\Android\EduQuizzApp_v1

# Clean build
.\gradlew clean

# Build release APK
.\gradlew assembleRelease

# APK location:
# app\build\outputs\apk\release\app-release.apk
```

### B. Run Attack Vector 1 Again

```powershell
# Copy protected APK
copy app\build\outputs\apk\release\app-release.apk d:\APK_Analysis\eduquizz-protected.apk

cd d:\APK_Analysis

# Run analysis script
..\scripts\analyze_apk.ps1 -ApkPath "eduquizz-protected.apk"
```

### C. Expected Results

**✅ After Defense Implementation:**

| What to Check | Before Defense | After Defense |
|--------------|----------------|---------------|
| Class names | `MainActivity`, `AuthModule` | `a`, `b`, `c` (obfuscated) |
| API URLs | `"http://10.0.2.2:8080/"` visible | NOT visible (in BuildConfig) |
| Google Client ID | Plain text visible | Encrypted string |
| Expected Signature | Plain text visible | Encrypted string |
| Debug logs | `Log.d()` statements present | All removed |
| Code readability | Easy to read | Very difficult |

**Decompile with JADX:**

```powershell
jadx eduquizz-protected.apk -d protected_analysis

# Search for sensitive info:
cd protected_analysis\sources
findstr /s /i "http://" *.java   # Should find minimal results
findstr /s /i "10.0.2.2" *.java   # Should find NOTHING
findstr /s /i "1026710210552" *.java  # Should find NOTHING
```

### D. Runtime Testing

```powershell
# Test 1: Normal device
adb install eduquizz-protected.apk
# ✅ Should launch normally

# Test 2: Rooted device (if available)
# ✅ Should show warning and exit

# Test 3: With debugger
adb shell am set-debug-app -w com.example.eduquizz
adb install eduquizz-protected.apk
# ✅ Should detect debugger and exit
```

---

## Phần 9: Summary & Checklist

### ✅ Defense Implementation Checklist

- [ ] **Code Obfuscation**
  - [ ] Enable `isMinifyEnabled = true` in build.gradle.kts
  - [ ] Configure comprehensive proguard-rules.pro
  - [ ] Verify mapping.txt is generated after build
  - [ ] Test app functionality with obfuscation

- [ ] **String Encryption**
  - [ ] Create StringEncryption.kt utility
  - [ ] Encrypt all sensitive strings
  - [ ] Update code to use encrypted strings
  - [ ] Verify strings are encrypted in APK

- [ ] **API Protection**
  - [ ] Move URLs to BuildConfig
  - [ ] Encrypt sensitive BuildConfig values
  - [ ] Update all network modules
  - [ ] Update all repository classes
  - [ ] Disable logging in release builds

- [ ] **Runtime Protection**
  - [ ] Implement RootDetection.kt
  - [ ] Implement EmulatorDetection.kt
  - [ ] Implement DebuggerDetection.kt
  - [ ] Integrate checks in MainApplication.kt
  - [ ] Test on rooted/emulator/debugger

- [ ] **Network Security**
  - [ ] Implement certificate pinning
  - [ ] Get SSL certificate pins from backend
  - [ ] Test with valid/invalid certificates

- [ ] **Anti-Tampering**
  - [ ] Encrypt expected signature
  - [ ] Remove signature logging
  - [ ] Test signature verification

- [ ] **Verification**
  - [ ] Build release APK
  - [ ] Decompile with JADX
  - [ ] Verify sensitive data is NOT visible
  - [ ] Test app functionality
  - [ ] Test security checks

---

## Next Steps

1. Review [implementation_plan.md](implementation_plan.md) for detailed changes
2. Implement defenses layer by layer
3. Test each layer individually
4. Build and verify protected APK
5. Optional: Proceed to Attack Vector 2 to test authentication security

---

**⚠️ Important Notes:**

- Keep `mapping.txt` file safe - you'll need it to debug crash reports
- Test thoroughly on real devices before releasing
- Consider using Google Play App Signing for additional security
- Monitor crash reports for obfuscation-related issues
- Update certificate pins when renewing SSL certificates

**🎯 Expected Difficulty for Attacker After Defense:**

| Attack Method | Before | After | Difficulty Increase |
|--------------|---------|-------|-------------------|
| APK Decompilation | ⭐ Easy | ⭐⭐⭐⭐ Very Hard | +400% |
| API Discovery | ⭐ Easy | ⭐⭐⭐⭐⭐ Extremely Hard | +500% |
| Debugger Attach | ⭐ Easy | ⭐⭐⭐⭐ Very Hard | +400% |
| Code Understanding | ⭐ Easy | ⭐⭐⭐⭐⭐ Extremely Hard | +500% |

---

**📚 Additional Resources:**

- [Android ProGuard Documentation](https://developer.android.com/build/shrink-code)
- [Android Keystore System](https://developer.android.com/privacy-and-security/keystore)
- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)
- [Certificate Pinning Guide](https://square.github.io/okhttp/features/https/#certificate-pinning)

