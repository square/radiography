package com.squareup.radiography.sample.compose

import androidx.ui.test.android.createAndroidComposeRule
import androidx.ui.test.assert
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.hasSubstring
import androidx.ui.test.onChild
import androidx.ui.test.onNodeWithTag
import androidx.ui.test.onNodeWithText
import androidx.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test

class ComposeSampleUiTest {

  @get:Rule
  val activityRule = createAndroidComposeRule<MainActivity>()

  @Test fun launches() {
    onNodeWithText("The password is Baguette").assertIsDisplayed()
  }

  @Test fun displaysHierarchyInline() {
    onNodeWithTag(LIVE_HIERARCHY_TEST_TAG)
        .onChild()
        .assert(hasSubstring("The password is Baguette"))
        .assert(hasSubstring("Unchecked"))

    onNodeWithTag(TEXT_FIELD_TEST_TAG)
        .performTextReplacement("foobar")

    onNodeWithTag(LIVE_HIERARCHY_TEST_TAG)
        .onChild()
        .assert(hasSubstring("The password is Baguette"))
        .assert(hasSubstring("foobar"))
  }
}
