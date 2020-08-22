import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdkVersion(30)

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  defaultConfig {
    minSdkVersion(21)
    targetSdkVersion(28)
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    buildConfig = false
    compose = true
  }

  composeOptions {
    kotlinCompilerVersion = Versions.OldComposeCompiler
    kotlinCompilerExtensionVersion = Versions.OldCompose
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
  }
}

dependencies {
  // Don't use Versions.KotlinStdlib for Kotlin stdlib, since this module actually uses the Compose
  // compiler and needs the latest stdlib.

  androidTestImplementation(project(":radiography"))
  androidTestImplementation(Dependencies.AppCompat)
  androidTestImplementation(Dependencies.Compose.OldMaterial)
  androidTestImplementation(Dependencies.Compose.OldTesting)
  androidTestImplementation(Dependencies.InstrumentationTests.Rules)
  androidTestImplementation(Dependencies.InstrumentationTests.Runner)
  androidTestImplementation(Dependencies.Truth)
}
