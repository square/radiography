package radiography.compose

import radiography.ViewStateRenderer

public object ViewStateRenderers {

  /**
   * Configures summary rendering of any Compose hierarchies found in the view tree.
   *
   * @param showStates If true, the Compose `(Mutable)State` values visible to each layout node will
   * be displayed.
   * @param showInnerCalls If true, the full chain of calls to each `LayoutNode` will be included
   * in the output.
   */
  @ExperimentalRadiographyComposeApi
  @JvmOverloads
  public fun composeLayoutsRenderer(
    includeText: Boolean = false,
    maxTextLength: Int = Int.MAX_VALUE,
    showStates: Boolean = false,
    showInnerCalls: Boolean = false,
  ): ViewStateRenderer = ComposeSummaryRenderer(
      showInnerCalls,
      showStates,
      RawValueFormatters.defaults(includeText, maxTextLength)
  )

  /**
   * Configures detailed rendering of any Compose hierarchies found in the view tree.
   *
   * @param omitDefaultArgumentValues If true, function arguments that are not explicitly passed
   * in (i.e. they are supplied by the default argument value) will not be displayed.
   * @param collapseEmptyNodes If true, nodes that don't have any data to display will be hidden.
   */
  @ExperimentalRadiographyComposeApi
  @JvmOverloads
  public fun composeDetailedRenderer(
    includeText: Boolean = false,
    maxTextLength: Int = Int.MAX_VALUE,
    omitDefaultArgumentValues: Boolean = true,
    collapseEmptyNodes: Boolean = true,
  ): ViewStateRenderer = ComposeDetailedRenderer(
      omitDefaultArgumentValues, collapseEmptyNodes,
      RawValueFormatters.defaults(includeText, maxTextLength)
  )
}
