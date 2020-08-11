package radiography.compose

import radiography.ViewStateRenderer

public object ViewStateRenderers {

  /**
   * Configures rendering of any Compose hierarchies found in the view tree.
   */
  @ExperimentalRadiographyComposeApi
  @JvmOverloads
  public fun composeViewRenderer(
    includeText: Boolean = false,
    maxTextLength: Int = Int.MAX_VALUE,
    omitDefaultArgumentValues: Boolean = true,
    collapseEmptyNodes: Boolean = true
  ): ViewStateRenderer = ComposeTreeNodeRenderer(
      omitDefaultArgumentValues, collapseEmptyNodes,
      RawValueFormatters.defaults(includeText, maxTextLength)
  )
}
