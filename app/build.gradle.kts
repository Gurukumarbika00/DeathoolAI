plugins {
    id("com.android.application") version "8.2.2" apply true
    id("org.jetbrains.kotlin.android") version "1.9.22" apply true
}

android {
    namespace = "com.deathpool.ai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.deathpool.ai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.arthenica:ffmpeg-kit-full:6.0-2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
