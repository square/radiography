import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.application")
  kotlin("android")
}

/** Use a separate property for the sample so we can test with different versions easily. */
val sampleComposeVersion = "1.0.0-alpha09"

android {
  compileSdkVersion(30)

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  defaultConfig {
    minSdkVersion(21)
    targetSdkVersion(30)
    applicationId = "com.squareup.radiography.sample.compose"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerVersion = Versions.KotlinCompiler
    kotlinCompilerExtensionVersion = sampleComposeVersion
  }

  lintOptions {
    // Workaround lint bug.
    disable("InvalidFragmentVersionForActivityResult")
  }

  packagingOptions {
    exclude("META-INF/AL2.0")
    exclude("META-INF/LGPL2.1")
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
  implementation(project(":radiography"))
  implementation(Dependencies.AppCompat)
  implementation(Dependencies.Compose(sampleComposeVersion).Material)
  implementation(Dependencies.Compose(sampleComposeVersion).Tooling)

  androidTestImplementation(Dependencies.Compose(sampleComposeVersion).Testing)
  androidTestImplementation(Dependencies.InstrumentationTests.Rules)
  androidTestImplementation(Dependencies.InstrumentationTests.Runner)
}
