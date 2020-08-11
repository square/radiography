package radiography.compose

import android.view.View
import androidx.ui.tooling.Group
import radiography.AttributeAppendable
import radiography.TreeRenderingVisitor
import radiography.ViewFilter
import radiography.compose.ComposeTreeNode.EmittedValueNode

/**
 * [TreeRenderingVisitor] for [Group] objects produced by Compose tooling to describe a slot table.
 *
 * @param classicViewVisitor A [TreeRenderingVisitor] to delegate to when a classic view is found
 * inside a composition.
 */
internal class GroupTreeRenderingVisitor(
  private val treeNodeFormatter: ComposeTreeNodeRenderer,
  private val viewFilter: ViewFilter,
  private val classicViewVisitor: TreeRenderingVisitor<View>
) : TreeRenderingVisitor<ComposeTreeNode>() {

  override fun RenderingScope.visitNode(node: ComposeTreeNode) {
    AttributeAppendable(description).let { appendable ->
      with(treeNodeFormatter) {
        appendable.render(node)
      }
    }

    node.children.asSequence()
        .filter(viewFilter::matches)
        .forEach {
          addChildToVisit(it)
        }

    // When we find Android views embedded in the composition, delegate to the view visitor.
    ((node as? EmittedValueNode)?.emittedValue as? View)?.let { view ->
      addChildToVisit(view, classicViewVisitor)
    }
  }
}
