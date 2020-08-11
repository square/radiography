package radiography.compose

import android.view.View
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntBounds
import androidx.ui.tooling.Group
import androidx.ui.tooling.NodeGroup

/**
 * Information about a Compose `LayoutNode`, extracted from a [Group] tree via [Group.layoutInfos].
 */
internal class ComposeLayoutInfo(
  val name: String,
  val bounds: IntBounds,
  val modifiers: List<Modifier>,
  val children: Sequence<ComposeLayoutInfo>,
  val view: View?
)

/**
 * A sequence that lazily parses [ComposeLayoutInfo]s from a [Group] tree.
 */
internal val Group.layoutInfos: Sequence<ComposeLayoutInfo> get() = computeLayoutInfos()

/**
 * Recursively parses [ComposeLayoutInfo]s from a [Group]. Groups form a tree and can contain different
 * type of nodes which represent function calls, arbitrary data stored directly in the slot table,
 * or just subtrees.
 *
 * This function walks the tree and collects only Groups which represent emitted values
 * ([NodeGroup]s). These either represent `LayoutNode`s (Compose's internal primitive for layout
 * algorithms) or classic Android views that the composition emitted. This function collapses all
 * the groups in between each of these nodes, but uses the top-most Group under the previous node
 * to derive the "name" of the [ComposeLayoutInfo]. The other [ComposeLayoutInfo] properties come directly off
 * [NodeGroup] values.
 */
private fun Group.computeLayoutInfos(parentName: String = ""): Sequence<ComposeLayoutInfo> {
  val name = parentName.ifBlank { this.name }.orEmpty()

  if (this !is NodeGroup) {
    return children.asSequence()
        .flatMap { it.computeLayoutInfos(name) }
  }

  val children = children.asSequence()
      // This node will "consume" the name, so reset it name to empty for children.
      .flatMap { it.computeLayoutInfos() }

  val layoutInfo = ComposeLayoutInfo(
      name = name,
      bounds = box,
      modifiers = modifierInfo.map { it.modifier },
      children = children,
      view = node as? View
  )
  return sequenceOf(layoutInfo)
}
