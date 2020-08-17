/*
 * Copyright 2020 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("android")
  id("com.vanniktech.maven.publish")
}

android {
  // Using 28 for now to get the Robolectric tests working.
  compileSdkVersion(28)
  buildToolsVersion = "29.0.2"

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  defaultConfig {
    minSdkVersion(17)
    targetSdkVersion(28)
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    buildConfig = false
  }
}

tasks.withType<KotlinCompile> {
  // Tests aren't part of the public API, don't turn explicit API mode on for them.
  if (!name.contains("test", ignoreCase = true)) {
    kotlinOptions {
      // Require explicit public modifiers and types.
      // TODO this should be moved to a top-level `kotlin { explicitApi() }` once that's working
      //  for android projects, see https://youtrack.jetbrains.com/issue/KT-37652.
      freeCompilerArgs = listOf("-Xexplicit-api=strict")
    }
  }
}

dependencies {
  testImplementation(Dependencies.JUnit)
  testImplementation(Dependencies.Mockito)
  testImplementation(Dependencies.Robolectric)
  testImplementation(Dependencies.Truth)

  androidTestImplementation(Dependencies.InstrumentationTests.Core)
  androidTestImplementation(Dependencies.InstrumentationTests.Espresso)
  androidTestImplementation(Dependencies.InstrumentationTests.Rules)
  androidTestImplementation(Dependencies.InstrumentationTests.Runner)
  androidTestImplementation(Dependencies.Truth)
}
