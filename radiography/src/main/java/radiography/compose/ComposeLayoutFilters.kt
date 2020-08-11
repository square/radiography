package radiography.compose

import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.semantics.SemanticsProperties.TestTag
import radiography.ViewFilter
import radiography.ViewFilters.viewFilterFor

@ExperimentalRadiographyComposeApi
object ComposeLayoutFilters {

  /**
   * Filters out Composables with [`testTag`][androidx.compose.ui.platform.testTag] modifiers
   * matching [skippedTestTags].
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  fun skipTestTagsFilter(vararg skippedTestTags: String): ViewFilter =
    viewFilterFor<ComposeLayoutInfo> { layoutInfo ->
      layoutInfo.modifiers.asSequence()
          .filterIsInstance<SemanticsModifier>()
          .flatMap { semantics ->
            semantics.semanticsConfiguration.asSequence()
                .filter { it.key == TestTag }
          }
          .none { it.value in skippedTestTags }
    }

  /**
   * Filters out Composables with [`layoutId`][androidx.compose.ui.layout.layoutId] modifiers for
   * which [skipLayoutId] returns true.
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  fun skipLayoutIdsFilter(skipLayoutId: (Any) -> Boolean): ViewFilter =
    viewFilterFor<ComposeLayoutInfo> { layoutInfo ->
      layoutInfo.modifiers.asSequence()
          .filterIsInstance<LayoutIdParentData>()
          .none { skipLayoutId(it.id) }
    }
}
