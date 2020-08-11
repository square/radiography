package radiography.compose

import androidx.compose.ui.unit.IntBounds
import radiography.AttributeAppendable
import radiography.TypedViewStateRenderer
import radiography.compose.ComposeTreeNode.DataNode
import radiography.compose.ComposeTreeNode.EmittedValueNode
import radiography.compose.ComposeTreeNode.EmptyNode
import radiography.compose.ComposeTreeNode.FunctionCallNode
import radiography.formatPixelDimensions

private const val RECOMPOSE_SCOPE_CLASS_NAME = "androidx.compose.runtime.RecomposeScope"

/**
 * Renders a [ComposeTreeNode] to a [StringBuilder].
 *
 * @param omitDefaultArgumentValues Whether to omit argument values that are flagged as being
 * defaults when printing [FunctionCallNode.parameters].
 * @param rawValueFormatters A list of [RawValueFormatter]s that will be used to format "raw data"
 * such as parameter values, node data values, and
 */
internal class ComposeTreeNodeRenderer(
  private val omitDefaultArgumentValues: Boolean = true,
    // TODO passing this here is a hack, need a better way.
  internal val collapseEmptyNodes: Boolean = true,
  private val rawValueFormatters: List<RawValueFormatter> = RawValueFormatters.defaults()
) : TypedViewStateRenderer<ComposeTreeNode>(ComposeTreeNode::class.java) {

  override fun AttributeAppendable.renderTyped(rendered: ComposeTreeNode) {
    val description = StringBuilder()
    description.appendComposeTreeNode(rendered)
    append(description)
  }

  private fun StringBuilder.appendComposeTreeNode(node: ComposeTreeNode) {
    when (node) {

      is EmptyNode -> {
        // Even if collapseEmptyNodes is enabled, an empty node will be rendered if it has children.
        append("{no data}")
      }

      is FunctionCallNode -> {
        append("${node.name}() ${node.bounds.describeSize()}")
        if (node.parameters.isNotEmpty()) {
          appendLabelLine("parameters")
          node.parameters
              .filterNot { (omitDefaultArgumentValues && it.fromDefault) }
              .joinTo(this) { param ->
                // TODO unit tests to ensure parameters are formatted
                buildString {
                  if (param.fromDefault) append("default ")
                  if (param.static) append("static ")
                  append(param.name)
                  append('=')
                  append(formatRawValue(param.value))
                  param.inlineClass?.let { append(": $it") }
                }
              }
        }
        if (node.data.isNotEmpty()) {
          appendLabelLine("data")
          formatData(node.data)
        }
      }

      is DataNode -> {
        append("data: ")
        formatData(node.data)
      }

      is EmittedValueNode -> {
        // Nodes don't seem to have parameters, only data.
        append("<${node.emittedValue::class.simpleName}> ${node.bounds.describeSize()}")
        if (node.data.isNotEmpty()) {
          appendLabelLine("data")
          formatData(node.data)
        }
        if (node.modifiers.isNotEmpty()) {
          appendLabelLine("modifiers")
          node.modifiers.joinTo(this, prefix = "[ ", postfix = " ]") { modifier ->
            formatRawValue(modifier.modifier)
          }
        }
      }
    }
  }

  private fun formatRawValue(value: Any?): CharSequence {
    return rawValueFormatters.asSequence()
        .mapNotNull { it.format(value, ::formatRawValue) }
        .firstOrNull()
        ?: value.toString()
  }

  private fun StringBuilder.formatData(data: Collection<Any?>) {
    data
        .filterNot {
          // There are lots of RecomposeScope references in the slot table, but they're not
          // interesting.
          it != null && it::class.java.name == RECOMPOSE_SCOPE_CLASS_NAME
        }
        .joinTo(this, prefix = "[ ", postfix = " ]", transform = ::formatRawValue)
  }

  private fun StringBuilder.appendLabelLine(label: String) {
    append("\n$label: ")
  }
}

private fun IntBounds.describeSize(): String {
  return if (left != right || top != bottom) {
    formatPixelDimensions(width = right - left, height = bottom - top)
  } else {
    ""
  }
}
