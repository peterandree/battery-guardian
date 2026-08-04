# Android ProGuard rules for Battery Guardian

# Basic ProGuard rules for Android apps
-keep class androidx.** { *; }
-keep class com.google.** { *; }
-keep class android.** { *; }
-keep class java.** { *; }

# Keep all activities, services, and receivers
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver

# Keep all ViewModels
-keep class * extends androidx.lifecycle.ViewModel

# Keep all Hilt components
-keep class * implements dagger.Component
-keep class * implements dagger.Module
-keep class * implements dagger.Provides

# Keep all Room entities and DAOs
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Database
-keep class * extends androidx.room.Dao

# Keep all DataStore preferences
-keep class * implements androidx.datastore.core.DataStore

# Keep all Compose functions
-keep class * implements androidx.compose.runtime.Composable

# Keep R classes
-keep class **.R
-keep class **.R$*

# Keep build config
-keep class BuildConfig

# Keep all classes that implement Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep all enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep all Kotlin metadata
-keep class **$$ViewBinder { *; }
-keep class **$$ViewBinding { *; }

# Keep all Hilt generated classes
-keep class * implements dagger.MembersInjector
-keep class * implements dagger.MapKey

# Keep all Room generated classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.Database { *; }

# Keep all DataStore generated classes
-keep class androidx.datastore.** { *; }

# Keep all Compose generated classes
-keep class androidx.compose.** { *; }
