package radiography.internal

import android.view.View
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntBounds
import androidx.compose.ui.tooling.Group
import androidx.compose.ui.tooling.NodeGroup
import androidx.compose.ui.tooling.asTree

/**
 * Information about a Compose `LayoutNode`, extracted from a [Group] tree via [Group.layoutInfos].
 *
 * This is a useful layer of indirection from directly handling Groups because it allows us to
 * define our own notion of what an atomic unit of "composable" is independently from how Compose
 * actually represents things under the hood. When this changes in some future dev version, we
 * only need to update the "parsing" logic in this file.
 * It's also helpful since we actually gather data from multiple Groups for a single LayoutInfo,
 * so parsing them ahead of time into these objects means the visitor can be stateless.
 */
internal data class ComposeLayoutInfo(
  val name: String,
  val bounds: IntBounds,
  val modifiers: List<Modifier>,
  val children: Sequence<ComposeLayoutInfo>,
  val view: View?,
  val isSubcomposition: Boolean = false
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
private fun Group.computeLayoutInfos(
  parentName: String = ""
): Sequence<ComposeLayoutInfo> {
  val name = parentName.ifBlank { this.name }.orEmpty()

  // Look for any CompositionReferences stored in this group. These will be rolled up into the
  // SubcomposeLayout if present, otherwise they will just be shown as regular children.
  @OptIn(InternalComposeApi::class)
  val subComposedChildren = getCompositionReferences()
    .flatMap { it.tryGetComposers().asSequence() }
    .map { subcomposer ->
      ComposeLayoutInfo(
        isSubcomposition = true,
        name = name,
        bounds = box,
        modifiers = emptyList(),
        // The compositionData val is marked as internal, and not intended for public consumption.
        children = subcomposer.compositionData.asTree().layoutInfos,
        view = null
      )
    }

  // SubcomposeLayouts need to be handled specially, because all their subcompositions are always
  // logical children of their single LayoutNode. In order to render them so that the rendering
  // actually matches that logical structure, we need to reorganize the subtree a bit so
  // subcompositions are children of the layout node and not siblings of it.
  //
  // Note that there's no sure-fire way to actually detect a SubcomposeLayout. The best we can do is
  // use a heuristic. If any part of the heuristics don't match, then we fall back to treating the
  // group like any other.
  //
  // The heuristic we use is:
  //  - Name of the group is "SubcomposeLayout".
  //  - Has one or more subcompositions under it.
  //  - Has exactly one LayoutNode child.
  //  - That LayoutNode has no children of its own.
  if (this.name == "SubcomposeLayout") {
    val (subcompositions, regularChildren) =
      (children.asSequence().flatMap { it.computeLayoutInfos(name) } + subComposedChildren)
          .partition { it.isSubcomposition }

    if (subcompositions.isNotEmpty() && regularChildren.size == 1) {
      val mainNode = regularChildren.single()
      if (mainNode.children.isEmpty()) {
        // We can be pretty confident at this point that this is an actual SubcomposeLayout, so
        // expose its layout node as the parent of all its subcompositions.
        val subcompositionName = "<subcomposition of ${mainNode.name}>"
        return sequenceOf(
            mainNode.copy(children = subcompositions.asSequence()
                .map { it.copy(name = subcompositionName) }
            )
        )
      }
    }
  }

  // This is an intermediate group that doesn't represent a LayoutNode.
  if (this !is NodeGroup) {
    return children.asSequence()
        .flatMap { it.computeLayoutInfos(name) } + subComposedChildren
  }

  val children = children.asSequence()
      // This node will "consume" the name, so reset it name to empty for children.
      .flatMap { it.computeLayoutInfos() }

  val layoutInfo = ComposeLayoutInfo(
      name = name,
      bounds = box,
      modifiers = modifierInfo.map { it.modifier },
      children = children + subComposedChildren,
      view = node as? View
  )
  return sequenceOf(layoutInfo)
}

private fun Sequence<*>.isEmpty(): Boolean = !iterator().hasNext()
