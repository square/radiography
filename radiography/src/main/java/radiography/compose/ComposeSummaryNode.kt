package radiography.compose

import androidx.compose.runtime.State
import androidx.compose.ui.node.ModifierInfo
import androidx.compose.ui.semantics.SemanticsModifier
import radiography.compose.ComposeTreeNode.DataNode
import radiography.compose.ComposeTreeNode.EmittedValueNode
import radiography.compose.ComposeTreeNode.FunctionCallNode

/**
 * TODO write documentation
 */
internal val ComposeTreeNode.summary: Sequence<ComposeSummaryNode>
  get() = summarize()

private fun ComposeTreeNode.summarize(
  parentPath: List<String> = emptyList(),
  states: MutableList<State<*>> = mutableListOf()
): Sequence<ComposeSummaryNode> {
  fun statesWith(data: Collection<Any?>) = states.apply {
    addAll(data.filterIsInstance<State<*>>())
  }

  return children.flatMap { child ->
    when (child) {
      is EmittedValueNode -> {
        val semantics = mutableMapOf<String, MutableList<Any?>>()
        val otherModifiers = mutableListOf<ModifierInfo>()
        child.modifiers.forEach {
          if (it.modifier is SemanticsModifier) {
            (it.modifier as SemanticsModifier).semanticsConfiguration
                .forEach { (key, value) ->
                  val values = semantics.getOrPut(key.name) { mutableListOf() }
                  values += value
                }
          } else {
            otherModifiers += it
          }
        }

        sequenceOf(
            ComposeSummaryNode(
                path = parentPath + child.emittedValue::class.java.simpleName,
                states = statesWith(child.data),
                semantics = semantics,
                modifiers = otherModifiers,
                emittedValueNode = child,
                children = child.summarize()
            )
        )
      }
      is FunctionCallNode -> {
        child.summarize(parentPath + child.name, statesWith(child.data))
      }
      is DataNode -> {
        child.summarize(parentPath, statesWith(child.data))
      }
      else -> {
        child.summarize(parentPath, states)
      }
    }
  }.constrainOnce()
}

internal class ComposeSummaryNode(
  val path: List<String>,
  val states: List<State<*>>,
  val semantics: Map<String, List<Any?>>,
  val modifiers: List<ModifierInfo>,
  val emittedValueNode: EmittedValueNode,
  val children: Sequence<ComposeSummaryNode>
)
