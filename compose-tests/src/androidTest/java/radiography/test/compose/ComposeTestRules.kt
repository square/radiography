package radiography.test.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.ui.test.ComposeTestRuleJUnit

/**
 * Calls [ComposeTestRuleJUnit.setContent] but wraps [content] with a [Box] to emulate how real
 * apps usually have one or more root-level composables. Compose UI tests should use this instead
 * of `setContent` to insulate them from the implementation details of the internal composable graph
 * that `setContent` creates behind the scenes and might change between releases.
 *
 * E.g. If you use `setContent` directly, root children will all show up as `Providers` in alpha04.
 */
fun ComposeTestRuleJUnit.setContentWithExplicitRoot(content: @Composable () -> Unit) {
  setContent {
    Box {
      content()
    }
  }
}
