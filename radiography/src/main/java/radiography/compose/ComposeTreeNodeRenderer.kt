package radiography.compose

import radiography.compose.ComposeTreeNode.DataNode
import radiography.compose.ComposeTreeNode.EmittedValueNode
import radiography.compose.ComposeTreeNode.EmptyNode
import radiography.compose.ComposeTreeNode.FunctionCallNode

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
  private val rawValueFormatters: List<RawValueFormatter> = RawValueFormatters.defaults()
) {

  internal fun appendComposeTreeNode(
    builder: StringBuilder,
    node: ComposeTreeNode
  ) {
    when (node) {

      is EmptyNode -> {
        // Even if collapseEmptyNodes is enabled, an empty node will be rendered if it has children.
        builder.append("{no data}")
      }

      is FunctionCallNode -> {
        builder.append("${node.name}() ${node.bounds.describeSize()}")
        if (node.parameters.isNotEmpty()) {
          builder.appendLabelLine("parameters")
          node.parameters
              .filterNot { (omitDefaultArgumentValues && it.fromDefault) }
              .joinTo(builder) { param ->
                // TODO unit tests to ensure parameters are formatted
                buildString {
                  if (param.fromDefault) append("default ")
                  if (param.static) append("static ")
                  append(param.name)
                  append('=')
                  append(rawValueFormatters.format(param.value))
                  param.inlineClass?.let { append(": $it") }
                }
              }
        }
        if (node.data.isNotEmpty()) {
          builder.appendLabelLine("data")
          builder.formatData(node.data)
        }
      }

      is DataNode -> {
        builder.append("data: ")
        builder.formatData(node.data)
      }

      is EmittedValueNode -> {
        // Nodes don't seem to have parameters, only data.
        builder.append("<${node.emittedValue::class.simpleName}> ${node.bounds.describeSize()}")
        if (node.data.isNotEmpty()) {
          builder.appendLabelLine("data")
          builder.formatData(node.data)
        }
        if (node.modifiers.isNotEmpty()) {
          builder.appendLabelLine("modifiers")
          node.modifiers.joinTo(builder, prefix = "[ ", postfix = " ]") { modifier ->
            rawValueFormatters.format(modifier.modifier)
          }
        }
      }
    }
  }

  private fun StringBuilder.formatData(data: Collection<Any?>) {
    data
        .filterNot {
          // There are lots of RecomposeScope references in the slot table, but they're not
          // interesting.
          it != null && it::class.java.name == RECOMPOSE_SCOPE_CLASS_NAME
        }
        .joinTo(this, prefix = "[ ", postfix = " ]", transform = rawValueFormatters::format)
  }

  private fun StringBuilder.appendLabelLine(label: String) {
    append("\n$label: ")
  }
}

internal fun StringBuilder.appendEmittedValueNode(node: EmittedValueNode) {

}
