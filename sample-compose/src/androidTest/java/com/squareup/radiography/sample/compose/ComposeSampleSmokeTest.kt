package com.squareup.radiography.sample.compose

import androidx.ui.test.android.createAndroidComposeRule
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ComposeSampleSmokeTest {

  @get:Rule
  val activityRule = createAndroidComposeRule<MainActivity>()

  @Test fun displaysInitialScreen() {
    onNodeWithText("The password is Baguette").assertIsDisplayed()
  }
}
