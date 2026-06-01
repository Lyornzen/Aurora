# Flutter specific
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.** { *; }
-keep class io.flutter.util.** { *; }
-keep class io.flutter.view.** { *; }
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }

# Keep the application class
-keep class com.openseek.app.** { *; }

# dart:io related
-keep class java.net.** { *; }
-keep class javax.net.** { *; }

# sqflite
-keep class com.tekartik.sqflite.** { *; }

# speech_to_text
-keep class com.csdcorp.speech_to_text.** { *; }

# image_picker
-keep class io.flutter.plugins.imagepicker.** { *; }

# Suppress warnings for Play Core deferred components (not used)
-dontwarn com.google.android.play.core.**
# Suppress warnings for okhttp/okio (internal to plugins)
-dontwarn okhttp3.**
-dontwarn okio.**
# Suppress warnings for kotlin internal
-dontwarn kotlin.**

# General Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
