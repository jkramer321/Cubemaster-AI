plugins {
    id("com.android.application") version "8.13.1"
    id("org.jetbrains.kotlin.multiplatform") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CubemasterShared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Common dependencies can go here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.activity.compose)
                implementation(platform("androidx.compose:compose-bom:2024.09.00"))
                implementation(libs.androidx.compose.ui)
                implementation(libs.androidx.compose.ui.graphics)
                implementation(libs.androidx.compose.ui.tooling.preview)
                implementation(libs.androidx.compose.material3)
                implementation(libs.androidx.navigation.compose)
                implementation(libs.androidx.compose.material.icons.extended)
                
                // CameraX dependencies
                val camerax_version = "1.3.0"
                implementation("androidx.camera:camera-core:$camerax_version")
                implementation("androidx.camera:camera-camera2:$camerax_version")
                implementation("androidx.camera:camera-lifecycle:$camerax_version")
                implementation("androidx.camera:camera-view:$camerax_version")
                
                // Accompanist permissions
                implementation("com.google.accompanist:accompanist-permissions:0.32.0")
                
                // Fragment
                implementation("androidx.fragment:fragment-ktx:1.6.0")
            }
        }
        val androidUnitTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.junit)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    namespace = "com.cs407.cubemaster"
    compileSdk = 36

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.setSrcDirs(listOf("src/androidMain/kotlin"))
            res.setSrcDirs(listOf("src/androidMain/res"))
            assets.setSrcDirs(listOf("src/androidMain/assets"))
        }
        getByName("test") {
            java.setSrcDirs(listOf("src/androidUnitTest/kotlin"))
        }
    }

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Firebase dependencies (not in source sets, so keep here)
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation("com.google.firebase:firebase-analytics")

    // Android test dependencies (not in source sets)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}