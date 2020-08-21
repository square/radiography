package radiography.compose

import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.semantics.SemanticsProperties.TestTag
import radiography.ViewFilter
import radiography.ViewFilter.FilterResult.EXCLUDE
import radiography.ViewFilter.FilterResult.INCLUDE
import radiography.ViewFilter.FilterResult.INCLUDE_ONLY_CHILDREN
import radiography.ViewFilters.viewFilterFor

@ExperimentalRadiographyComposeApi
object ComposeLayoutFilters {

  /**
   * TODO kdoc
   */
  // TODO tests
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  fun startFromTestTag(testTag: String): ViewFilter =
    viewFilterFor<ComposeLayoutInfo> { layoutInfo ->
      tailrec fun ComposeLayoutInfo.hasTestTag(): Boolean {
        // TODO This is getting recomputed a lot, can we cache it directly in ComposeLayoutInfo?
        if (testTag in testTags) return true
        if (parent == null) return false
        return parent.hasTestTag()
      }

      return@viewFilterFor if (layoutInfo.hasTestTag()) {
        INCLUDE
      } else {
        INCLUDE_ONLY_CHILDREN
      }
    }

  /**
   * Filters out Composables with [`testTag`][androidx.compose.ui.platform.testTag] modifiers
   * matching [skippedTestTags].
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  fun skipTestTagsFilter(vararg skippedTestTags: String): ViewFilter =
    viewFilterFor<ComposeLayoutInfo> { layoutInfo ->
      layoutInfo.testTags.none { it in skippedTestTags }
          .let { if (it) INCLUDE else EXCLUDE }
    }

  private val ComposeLayoutInfo.testTags: Sequence<String>
    get() = modifiers.asSequence()
        .filterIsInstance<SemanticsModifier>()
        .flatMap { semantics ->
          semantics.semanticsConfiguration.asSequence()
        }
        .mapNotNull { (key, value) -> (value as? String).takeIf { key == TestTag } }

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
          .let { if (it) INCLUDE else EXCLUDE }
    }
}
