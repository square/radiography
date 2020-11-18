package com.squareup.radiography.sample.compose

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSubstring
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
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
