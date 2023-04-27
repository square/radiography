import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("android")
}

/**
 * Allows using a different version of Compose to validate that we degrade gracefully on apps
 * built with unsupported Compose versions.
 */
val oldComposeVersion = "1.2.1"
// Older version of Compose requires an older version of Kotlin.
val oldComposeCompiler = "1.7.20"

android {
  compileSdk = 33

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  defaultConfig {
    minSdk = 21
    targetSdk = 33
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    buildConfig = false
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = oldComposeVersion
  }

  packagingOptions {
    resources.excludes += listOf(
      "META-INF/AL2.0",
      "META-INF/LGPL2.1"
    )
  }
  namespace = "com.squareup.radiography.test.compose.unsupported.empty"
  testNamespace = "com.squareup.radiography.test.compose.unsupported"
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs += listOf(
      "-opt-in=kotlin.RequiresOptIn"
    )
  }
}

dependencies {
  androidTestImplementation(project(":radiography"))
  androidTestImplementation(Dependencies.AppCompat)
  androidTestImplementation(Dependencies.Compose().Activity("1.5.0"))
  androidTestImplementation(Dependencies.Compose(oldComposeVersion).Material)
  androidTestImplementation(Dependencies.Compose(oldComposeVersion).Testing)
  androidTestImplementation(Dependencies.InstrumentationTests.Rules)
  androidTestImplementation(Dependencies.InstrumentationTests.Runner)
  androidTestImplementation(Dependencies.Truth)
}
