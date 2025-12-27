# ═══════════════════════════════════════════════════════════
# EDUQUIZZ APP - PROGUARD CONFIGURATION (CORRECTED)
# ═══════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────
# 1. GENERAL OPTIMIZATION
# ─────────────────────────────────────────────────────────────

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# ─────────────────────────────────────────────────────────────
# 2. KEEP ESSENTIAL ANDROID COMPONENTS
# ─────────────────────────────────────────────────────────────

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

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

-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ✅ FIXED: Keep model fields for Gson, but allow class name obfuscation
-keepclassmembers class com.example.eduquizz.features.**.model.** {
    <fields>;
    <init>();
}
-keepclassmembers class com.example.eduquizz.features.**.data.model.** {
    <fields>;
    <init>();
}

# ─────────────────────────────────────────────────────────────
# 4. HILT / DAGGER
# ─────────────────────────────────────────────────────────────

-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends javax.inject.Provider

-keepclasseswithmembers class * {
    @dagger.hilt.** *;
}
-keepclasseswithmembers class * {
    @dagger.** *;
}
-dontwarn dagger.**

# ─────────────────────────────────────────────────────────────
# 5. KOTLIN & COROUTINES
# ─────────────────────────────────────────────────────────────

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ─────────────────────────────────────────────────────────────
# 6. JETPACK COMPOSE (✅ FIXED)
# ─────────────────────────────────────────────────────────────

# ✅ Only keep essential Compose classes, not everything
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    *** getCurrent*(...);
}
-dontwarn androidx.compose.**

# Keep Composable functions from being removed
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ─────────────────────────────────────────────────────────────
# 7. FIREBASE & GOOGLE PLAY SERVICES
# ─────────────────────────────────────────────────────────────

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

-keep class com.google.android.play.core.integrity.** { *; }
-dontwarn com.google.android.play.core.integrity.**

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ─────────────────────────────────────────────────────────────
# 8. SECURITY CLASSES (✅ FIXED - FULL OBFUSCATION)
# ─────────────────────────────────────────────────────────────

# ✅ BETTER: Let security classes be fully obfuscated
# Don't keep class names - let ProGuard rename them to a, b, c, etc.

# Let these be fully obfuscated (class names AND methods):
# - SignatureUtils → will become class a { method a(), b() }
# - PlayIntegrityHelper → will become class b { method c(), d() }

# ─────────────────────────────────────────────────────────────
# 9. ENCRYPTED SHARED PREFERENCES
# ─────────────────────────────────────────────────────────────

-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ─────────────────────────────────────────────────────────────
# 10. ADDITIONAL LIBRARIES
# ─────────────────────────────────────────────────────────────

# Coil Image Loading
-keep class coil.** { *; }
-dontwarn coil.**

# Lottie Animations
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Google Generative AI (Gemini)
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# OSMDroid Maps
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Glance (Widgets)
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# ─────────────────────────────────────────────────────────────
# 11. REMOVE DEBUG & DEVELOPMENT CODE
# ─────────────────────────────────────────────────────────────

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
    public static int e(...);
}

-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}
