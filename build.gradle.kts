// Project-level build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://github.com/google-ar/sceneform-android-sdk") }
    }
    dependencies {
        classpath(libs.android.gradle.v851) // Android Gradle Plugin
        classpath(libs.kotlin.gradle.plugin) // Kotlin Gradle Plugin
        classpath(libs.google.services.v4315) // Google Services Plugin
        classpath(libs.gradle.v810) // Gradle Version
        classpath ("com.google.ar.sceneform:plugin:1.15.0")
        // Add other classpath dependencies needed for build configuration
    }
}



plugins {
    id("com.android.application") version "8.5.2" apply false // Android Application Plugin
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false // Kotlin Android Plugin
    id("com.google.gms.google-services") version "4.4.2" apply false // Google Services Plugin
    // Apply other plugins that are used across the project
}

// Task to clean the build directory
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
