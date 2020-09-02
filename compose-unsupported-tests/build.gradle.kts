import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("android")
}

/**
 * Allows using a different version of Compose to validate that we degrade gracefully on apps
 * built with unsupported Compose versions.
 */
val oldComposeVersion = "0.1.0-dev12"
val oldComposeCompiler = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"

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
    kotlinCompilerVersion = oldComposeCompiler
    kotlinCompilerExtensionVersion = oldComposeVersion
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
  // Coordinates of the compose material artifact before it was renamed.
  androidTestImplementation("androidx.ui:ui-material:$oldComposeVersion")
  androidTestImplementation(Dependencies.Compose(oldComposeVersion).Testing)
  androidTestImplementation(Dependencies.InstrumentationTests.Rules)
  androidTestImplementation(Dependencies.InstrumentationTests.Runner)
  androidTestImplementation(Dependencies.Truth)
}
