package radiography.compose

import androidx.compose.ui.layout.LayoutIdParentData
import radiography.ScannableView.ComposeView
import radiography.ViewFilter

@ExperimentalRadiographyComposeApi
public object ComposableFilters {

  /**
   * Filters out Composables with [`testTag`][androidx.compose.ui.platform.testTag] modifiers
   * matching [skippedTestTags].
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  public fun skipTestTagsFilter(vararg skippedTestTags: String): ViewFilter = ViewFilter { view ->
    (view as? ComposeView)
        ?.findTestTags()
        ?.none { it in skippedTestTags }
        ?: true
  }

  /**
   * Filters out Composables with [`Modifier.layoutId`][androidx.compose.ui.layout.layoutId]
   * modifiers for which [skipLayoutId] returns true.
   *
   * @param skipLayoutId A function which takes values that are passed to `Modifier.layoutId` and
   * returns true if they indicate that the composable should be skipped.
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  public fun skipLayoutIdsFilter(skipLayoutId: (Any) -> Boolean): ViewFilter = ViewFilter { view ->
    (view as? ComposeView)
        ?.modifiers
        ?.asSequence()
        ?.filterIsInstance<LayoutIdParentData>()
        ?.none { layoutId -> skipLayoutId(layoutId.id) }
        ?: true
  }
}
