package radiography.compose

import androidx.compose.ui.layout.LayoutIdParentData
import radiography.ScannableView.ComposeView
import radiography.ViewFilter

@ExperimentalRadiographyComposeApi
object ComposeLayoutFilters {

  /**
   * Filters out Composables with [`testTag`][androidx.compose.ui.platform.testTag] modifiers
   * matching [skippedTestTags].
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  fun skipTestTagsFilter(vararg skippedTestTags: String): ViewFilter = ViewFilter { view ->
    (view as? ComposeView)
        ?.findTestTags()
        ?.none { it in skippedTestTags }
        ?: true
  }

  /**
   * Filters out Composables with [`layoutId`][androidx.compose.ui.layout.layoutId] modifiers for
   * which [skipLayoutId] returns true.
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  fun skipLayoutIdsFilter(skipLayoutId: (Any) -> Boolean): ViewFilter = ViewFilter {
    (it as? ComposeView)
        ?.modifiers
        ?.asSequence()
        ?.filterIsInstance<LayoutIdParentData>()
        ?.none { layoutId -> skipLayoutId(layoutId.id) }
        ?: true
  }
}
