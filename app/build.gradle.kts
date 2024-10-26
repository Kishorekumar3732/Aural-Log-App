// Module-level build.gradle.kts (app module)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Kotlin Annotation Processing Tool
    id("com.google.gms.google-services") // Google services for Firebase integration
}

android {
    namespace = "com.example.myhome02"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myhome02"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {

    // Firebase dependencies
    implementation(libs.firebase.auth) // Firebase Auth
    implementation(libs.firebase.auth.ktx) // Firebase Auth KTX
    implementation(libs.play.services.auth) // Google Play Services Auth
    implementation(libs.com.google.firebase.firebase.auth)

    // For authentication with Google using Credential Manager
    implementation ("com.google.android.gms:play-services-auth:21.2.0")
    implementation ("androidx.credentials:credentials:1.3.0")
    implementation ("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(libs.firebase.auth)

    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.3")

    // For navigation
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // Coil -> For showing image through url
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.compose.material3:material3:1.3.0")

    // AndroidX dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Material design library
    implementation(libs.material)

    // Jetpack Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)

    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database) // Firebase Database
    implementation(libs.firebase.analytics) // Firebase Analytics
    implementation(libs.firebase.firestore) // Firestore

    // Additional libraries
    implementation(libs.mpandroidchart) // MPAndroidChart for charts
    implementation(libs.gson) // Gson for JSON parsing
    implementation(libs.glide) // Glide for image loading
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Volley for networking
    implementation(libs.volley)
}
