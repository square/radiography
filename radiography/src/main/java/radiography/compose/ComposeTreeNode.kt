package radiography.compose

import androidx.compose.ui.node.ModifierInfo
import androidx.compose.ui.unit.IntBounds
import androidx.ui.tooling.CallGroup
import androidx.ui.tooling.Group
import androidx.ui.tooling.NodeGroup
import androidx.ui.tooling.ParameterInformation
import radiography.compose.ComposeTreeNode.DataNode
import radiography.compose.ComposeTreeNode.EmittedValueNode
import radiography.compose.ComposeTreeNode.EmptyNode
import radiography.compose.ComposeTreeNode.FunctionCallNode

/**
 * A more meaningful and convenient breakdown of different group types than Compose's [Group] class
 * provides. Parse [Group]s into implementations of this class via [toComposeTreeNode].
 */
internal sealed class ComposeTreeNode {

  abstract val children: Sequence<ComposeTreeNode>

  data class EmptyNode(override val children: Sequence<ComposeTreeNode>) : ComposeTreeNode()
  data class DataNode(
    val data: Collection<Any?>,
    override val children: Sequence<ComposeTreeNode>
  ) : ComposeTreeNode()

  data class FunctionCallNode(
    val name: String,
    val parameters: List<ParameterInformation>,
    val bounds: IntBounds,
    val data: Collection<Any?>,
    override val children: Sequence<ComposeTreeNode>
  ) : ComposeTreeNode()

  data class EmittedValueNode(
    val emittedValue: Any,
    val bounds: IntBounds,
    val data: Collection<Any?>,
    val modifiers: List<ModifierInfo>,
    override val children: Sequence<ComposeTreeNode>
  ) : ComposeTreeNode()
}

internal fun Group.toComposeTreeNode(collapseEmptyNodes: Boolean): ComposeTreeNode {
  val childrenNodes = children.asSequence().map { it.toComposeTreeNode(collapseEmptyNodes) }
      .let {
        if (collapseEmptyNodes) it.collapseEmptyNodes() else it
      }

  return when (this) {
    is CallGroup -> {
      if (!name.isNullOrEmpty()) {
        FunctionCallNode(name!!, parameters, box, data, childrenNodes)
      } else if (data.isNotEmpty()) {
        DataNode(data, childrenNodes)
      } else {
        EmptyNode(childrenNodes)
      }
    }
    is NodeGroup -> {
      EmittedValueNode(node, box, data, modifierInfo, childrenNodes)
    }
  }
}

private fun Sequence<ComposeTreeNode>.collapseEmptyNodes(): Sequence<ComposeTreeNode> {
  return flatMap { child ->
    if (child is EmptyNode) {
      // children only contains non-empty nodes.
      child.children
    } else {
      sequenceOf(child)
    }
  }
}
