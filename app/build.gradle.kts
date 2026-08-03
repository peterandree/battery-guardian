plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.batteryguardian"
    compileSdk = 35
    defaultConfig { applicationId = "com.batteryguardian"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    buildFeatures { compose = true }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    testImplementation("junit:junit:4.13.2")
}
