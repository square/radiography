package com.squareup.radiography.sample.compose

import androidx.ui.test.assert
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.createAndroidComposeRule
import androidx.ui.test.hasSubstring
import androidx.ui.test.onNodeWithTag
import androidx.ui.test.onNodeWithText
import androidx.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test

class ComposeSampleUiTest {

  @get:Rule
  val composeRule = createAndroidComposeRule<MainActivity>()

  @Test fun launches() {
    composeRule.onNodeWithText("Remember me").assertIsDisplayed()
  }

  @Test fun displaysHierarchyInline() {
    composeRule.onNodeWithTag(LIVE_HIERARCHY_TEST_TAG)
        .assert(hasSubstring("Remember me"))
        .assert(hasSubstring("Unchecked"))

    composeRule.onNodeWithTag(TEXT_FIELD_TEST_TAG)
        .performTextReplacement("foobar")

    composeRule.onNodeWithTag(LIVE_HIERARCHY_TEST_TAG)
        .assert(hasSubstring("Remember me"))
        .assert(hasSubstring("foobar"))
  }
}
