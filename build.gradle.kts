// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false
    id("androidx.room") version "2.6.1" apply false
}

// Global properties for the project
extra {
    // SDK versions
    val minSdk = 31
    val compileSdk = 35
    val targetSdk = 35
    
    // Kotlin
    val kotlinVersion = "2.0.21"
    val jvmTarget = "17"
    
    // AndroidX
    val appCompatVersion = "1.6.1"
    val coreKtxVersion = "1.12.0"
    val lifecycleVersion = "2.7.0"
    val activityComposeVersion = "1.8.2"
    val composeBomVersion = "2024.11.00"
    
    // Room
    val roomVersion = "2.6.1"
    
    // Hilt
    val hiltVersion = "2.55"
    
    // Coroutines
    val coroutinesVersion = "1.9.0"
    
    // DataStore
    val dataStoreVersion = "1.1.1"
}
