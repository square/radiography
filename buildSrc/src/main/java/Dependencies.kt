object Versions {
  /**
   * To change this in the IDE, use `systemProp.square.kotlinVersion=x.y.z` in your
   * `~/.gradle/gradle.properties` file.
   */
  val KotlinCompiler = System.getProperty("square.kotlinVersion") ?: "1.4.30"

  const val AndroidXTest = "1.3.0"
  const val Compose = "1.0.0-beta01"
}

object Dependencies {
  object Build {
    const val Android = "com.android.tools.build:gradle:7.0.0"
    const val MavenPublish = "com.vanniktech:gradle-maven-publish-plugin:0.14.0"
    val Kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KotlinCompiler}"
    const val Ktlint = "org.jlleitschuh.gradle:ktlint-gradle:9.2.1"
    const val BinaryCompatibility = "org.jetbrains.kotlinx:binary-compatibility-validator:0.2.3"
    const val Dokka = "org.jetbrains.dokka:dokka-gradle-plugin:1.4.20"
  }

  const val AppCompat = "androidx.appcompat:appcompat:1.2.0"
  const val ConstraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
  const val Curtains = "com.squareup.curtains:curtains:1.0.1"
  const val JUnit = "junit:junit:4.13"
  const val Mockito = "org.mockito:mockito-core:3.4.6"
  const val Robolectric = "org.robolectric:robolectric:4.3.1"
  const val Truth = "com.google.truth:truth:1.0.1"

  class Compose(composeVersion: String = Versions.Compose) {
    fun Activity(version: String = "1.3.0-alpha03") = "androidx.activity:activity-compose:$version"
    val Material = "androidx.compose.material:material:${composeVersion}"
    val Testing = "androidx.compose.ui:ui-test-junit4:${composeVersion}"
    val Tooling = "androidx.compose.ui:ui-tooling:${composeVersion}"
    val ToolingData = "androidx.compose.ui:ui-tooling-data:${composeVersion}"
  }

  object InstrumentationTests {
    const val Core = "androidx.test:core:${Versions.AndroidXTest}"
    const val Espresso = "androidx.test.espresso:espresso-core:3.3.0"
    const val JUnit = "androidx.test.ext:junit:1.1.2"
    const val Orchestrator = "androidx.test:orchestrator:${Versions.AndroidXTest}"
    const val Rules = "androidx.test:rules:${Versions.AndroidXTest}"
    const val Runner = "androidx.test:runner:${Versions.AndroidXTest}"
  }
}
