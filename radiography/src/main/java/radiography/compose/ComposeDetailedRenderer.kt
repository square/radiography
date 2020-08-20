package radiography.compose

import android.view.View
import androidx.ui.tooling.Group
import radiography.TreeRenderingVisitor
import radiography.TreeRenderingVisitor.RenderingScope
import radiography.ViewFilter

/**
 * TODO write documentation
 */
internal class ComposeDetailedRenderer(
  private val collapseEmptyNodes: Boolean = true,
  private val omitDefaultArgumentValues: Boolean = true,
  private val rawValueFormatters: List<RawValueFormatter> = RawValueFormatters.defaults()
) : ComposeViewRenderer() {

  override fun RenderingScope.visitGroup(
    group: Group,
    viewFilter: ViewFilter,
    classicViewVisitor: TreeRenderingVisitor<View>
  ) {
    val composeTreeNode = group.toComposeTreeNode(collapseEmptyNodes)
    val treeNodeRenderer = ComposeTreeNodeRenderer(omitDefaultArgumentValues, rawValueFormatters)
    val visitor = ComposeTreeNodeVisitor(treeNodeRenderer, viewFilter, classicViewVisitor)
    addChildToVisit(composeTreeNode, visitor)
  }
}
