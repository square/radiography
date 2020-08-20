package radiography.compose

import android.view.View
import radiography.TreeRenderingVisitor
import radiography.ViewFilter
import radiography.compose.ComposeTreeNode.EmittedValueNode

internal class ComposeTreeNodeVisitor(
  private val treeNodeFormatter: ComposeTreeNodeRenderer,
  private val viewFilter: ViewFilter,
  private val classicViewVisitor: TreeRenderingVisitor<View>
) : TreeRenderingVisitor<ComposeTreeNode>() {

  override fun RenderingScope.visitNode(node: ComposeTreeNode) {
    treeNodeFormatter.appendComposeTreeNode(description, node)

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
