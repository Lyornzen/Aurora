# Aurora ProGuard Rules

# ── OkHttp ──
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── JSON-org ──
-keep class org.json.** { *; }

# ── Compose ──
-keep class androidx.compose.** { *; }

# ── App data models ──
-keep class com.aurora.app.data.ApiConfig { *; }
-keep class com.aurora.app.data.ChatMessage { *; }
-keep class com.aurora.app.data.Message { *; }
-keep class com.aurora.app.data.ConversationHistory { *; }
-keep class com.aurora.app.data.Role { *; }

# ── Kotlin Coroutines ──
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── General ──
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
