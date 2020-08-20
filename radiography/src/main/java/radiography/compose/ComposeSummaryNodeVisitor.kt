package radiography.compose

import android.view.View
import radiography.TreeRenderingVisitor
import radiography.ViewFilter

/**
 * TODO write documentation
 */
// TODO Semantics formatting should be smarter, and only hide text for Text semantics. Other
//  string-value keys, such as TestTag and AccessibilityAction, do not need to be hidden. It should
//  also hide semantics pairs that can't be rendered, such as OnClick.
internal class ComposeSummaryNodeVisitor(
  private val showInnerCalls: Boolean = false,
  private val showStates: Boolean = false,
  private val rawValueFormatters: List<RawValueFormatter>,
  private val viewFilter: ViewFilter,
  private val classicViewVisitor: TreeRenderingVisitor<View>
) : TreeRenderingVisitor<ComposeSummaryNode>() {

  override fun RenderingScope.visitNode(node: ComposeSummaryNode) {
    with(description) {
      if (showInnerCalls) {
        node.path.joinTo(this, separator = "/")
      } else {
        append(node.path.firstOrNull())
      }
      append(" { ")
      append(node.emittedValueNode.bounds.describeSize())

      node.semantics.takeUnless { it.isEmpty() }
          ?.entries
          ?.joinTo(this, prefix = ", ") { (key, value) ->
            val formattedValue = if (value.size > 1) {
              value.joinToString(prefix = "[ ", postfix = " ]") { rawValueFormatters.format(it) }
            } else {
              rawValueFormatters.format(value.singleOrNull())
            }
            return@joinTo "$key=$formattedValue"
          }

      append(" }")

      node.states.takeIf { showStates && it.isNotEmpty() }
          ?.let { states ->
            append("\nstates: ")
            states.joinTo(this, prefix = "[ ", postfix = " ]") {
              rawValueFormatters.format(it.value)
            }
          }

      // TODO Formatters for specific modifier types. Can probably generalize the custom semantics
      //  modifier parsing to use that as well. Should filter unrenderable modifiers, such as
      //  PointerInputFilter. Should provide one for LayoutId and TestTag by default.
//      node.modifiers.takeUnless { it.isEmpty() }
//          ?.let { modifiers ->
//            append("\nmodifiers: ")
//            modifiers.joinTo(this, prefix = "[ ", postfix = " ]") { modifier ->
//              rawValueFormatters.format(modifier.modifier)
//            }
//          }
    }

    node.children.asSequence()
        .filter(viewFilter::matches)
        .forEach {
          addChildToVisit(it)
        }

    // When we find Android views embedded in the composition, delegate to the view visitor.
    (node.emittedValueNode.emittedValue as? View)?.let { view ->
      addChildToVisit(view, classicViewVisitor)
    }
  }
}
