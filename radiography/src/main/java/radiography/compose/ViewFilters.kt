package radiography.compose

import androidx.compose.ui.semantics.SemanticsModifier
import radiography.Radiography
import radiography.ViewFilter
import radiography.ViewFilters.viewFilterFor
import radiography.compose.ComposeTreeNode.EmittedValueNode

object ViewFilters {

  /**
   * Filters out Composables with `testTag` modifiers matching [skippedTestTags] from the output of
   * [Radiography.scan].
   */
  @ExperimentalRadiographyComposeApi
  fun skipTestTagsViewFilter(vararg skippedTestTags: String): ViewFilter =
    viewFilterFor<EmittedValueNode> { view ->
      val testTags = view.modifiers
          .asSequence()
          .map { it.modifier }
          .filterIsInstance<SemanticsModifier>()
          .flatMap { it.semanticsConfiguration.asSequence() }
          .filter { (key, _) -> key.name == "TestTag" }

      return@viewFilterFor testTags.none { it.value in skippedTestTags }
    }
}
