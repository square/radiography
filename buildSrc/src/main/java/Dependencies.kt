object Versions {
  val KotlinCompiler = System.getProperty("square.kotlinVersion") ?: "1.4.0"

  /** Use a lower version of the stdlib so the library can be consumed by lower kotlin versions. */
  val KotlinStdlib = System.getProperty("square.kotlinStdlibVersion") ?: "1.3.72"
}

object Dependencies {
  object Build {
    const val Android = "com.android.tools.build:gradle:4.0.0"
    const val MavenPublish = "com.vanniktech:gradle-maven-publish-plugin:0.12.0"
    val Kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KotlinCompiler}"
    const val Ktlint = "org.jlleitschuh.gradle:ktlint-gradle:9.2.1"
    const val BinaryCompatibility = "org.jetbrains.kotlinx:binary-compatibility-validator:0.2.3"
  }

  const val JUnit = "junit:junit:4.13"
  const val Mockito = "org.mockito:mockito-core:3.4.6"
  const val Robolectric = "org.robolectric:robolectric:4.3.1"
  const val Truth = "com.google.truth:truth:1.0.1"

  object InstrumentationTests {
    const val Core = "androidx.test:core:1.0.0"
    const val Espresso = "androidx.test.espresso:espresso-core:3.1.0"
    const val Rules = "androidx.test:rules:1.1.0"
    const val Runner = "androidx.test:runner:1.1.0"
  }
}
