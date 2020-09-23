import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("android")
}

/**
 * Allows using a different version of Compose to validate that we degrade gracefully on apps
 * built with unsupported Compose versions.
 */
val oldComposeVersion = "1.0.0-alpha04"
val oldComposeCompiler = Versions.KotlinCompiler

android {
  compileSdkVersion(30)

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  defaultConfig {
    minSdkVersion(21)
    targetSdkVersion(30)
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    buildConfig = false
    compose = true
  }

  composeOptions {
    kotlinCompilerVersion = oldComposeCompiler
    kotlinCompilerExtensionVersion = oldComposeVersion
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf(
        "-Xallow-jvm-ir-dependencies",
        "-Xskip-prerelease-check",
        "-Xopt-in=kotlin.RequiresOptIn"
    )
  }
}

dependencies {
  // Don't use Versions.KotlinStdlib for Kotlin stdlib, since this module actually uses the Compose
  // compiler and needs the latest stdlib.

  androidTestImplementation(project(":radiography"))
  androidTestImplementation(Dependencies.AppCompat)
  androidTestImplementation(Dependencies.Compose(oldComposeVersion).Material)
  androidTestImplementation(Dependencies.Compose(oldComposeVersion).Testing)
  androidTestImplementation(Dependencies.InstrumentationTests.Rules)
  androidTestImplementation(Dependencies.InstrumentationTests.Runner)
  androidTestImplementation(Dependencies.Truth)
}
