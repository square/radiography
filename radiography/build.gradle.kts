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
  }
}

dependencies {
  implementation(kotlin("stdlib"))

  testImplementation("junit:junit:4.13")
  testImplementation("com.squareup:fest-android:1.0.7")
  testImplementation("org.easytesting:fest-assert-core:2.0M10")
  testImplementation("org.mockito:mockito-core:3.4.6")
  testImplementation("org.robolectric:robolectric:4.3.1")
}
