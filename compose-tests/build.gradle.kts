import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdk = 30

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  defaultConfig {
    minSdk = 21
    targetSdk = 30
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    buildConfig = false
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = Versions.Compose
  }

  packagingOptions {
    resources.excludes += listOf(
      "META-INF/AL2.0",
      "META-INF/LGPL2.1",
    )
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs += listOf(
      "-Xopt-in=kotlin.RequiresOptIn"
    )
  }
}

dependencies {
  androidTestImplementation(project(":radiography"))
  androidTestImplementation(Dependencies.AppCompat)
  androidTestImplementation(Dependencies.Compose().Material)
  androidTestImplementation(Dependencies.Compose().Testing)
  androidTestImplementation(Dependencies.Compose().Tooling)
  androidTestImplementation(Dependencies.InstrumentationTests.Rules)
  androidTestImplementation(Dependencies.InstrumentationTests.Runner)
  androidTestImplementation(Dependencies.Truth)
}
