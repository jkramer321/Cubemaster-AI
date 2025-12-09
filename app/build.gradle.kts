// In app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
// <-- ADD THIS LINE
}


android {
    namespace = "com.cs407.cubemaster"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cs407.cubemaster"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    packaging {
        resources {
            // Don't compress .gz files since they're already compressed
            // This prevents Android from double-compressing already-compressed files
            // Note: Assets in assets/ folder are typically not compressed by default,
            // but this ensures .gz files are handled correctly
            excludes += listOf("META-INF/**")
        }
    }
    
    // Ensure assets are included in the build
    // Note: After adding files to assets/, you may need to:
    // 1. Clean the project (Build -> Clean Project)
    // 2. Rebuild the project (Build -> Rebuild Project)
    // 3. Uninstall and reinstall the app on your device/emulator
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Declare the dependency for Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Your existing dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.fragment:fragment-ktx:1.6.0")

    // CameraX dependencies - Updated to 1.4.0 for 16KB page size compatibility
    val camerax_version = "1.4.0"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")

    // Accompanist permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    implementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("io.coil-kt:coil-compose:2.6.0")
    testImplementation(kotlin("test"))
}