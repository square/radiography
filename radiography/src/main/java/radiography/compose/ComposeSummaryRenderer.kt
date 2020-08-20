package radiography.compose

import android.view.View
import androidx.ui.tooling.Group
import radiography.TreeRenderingVisitor
import radiography.TreeRenderingVisitor.RenderingScope
import radiography.ViewFilter

/**
 * TODO write documentation
 */
internal class ComposeSummaryRenderer(
  private val showInnerCalls: Boolean = false,
  private val showStates: Boolean = false,
  private val rawValueFormatters: List<RawValueFormatter>
) : ComposeViewRenderer() {

  override fun RenderingScope.visitGroup(
    group: Group,
    viewFilter: ViewFilter,
    classicViewVisitor: TreeRenderingVisitor<View>
  ) {
    val composeTreeNode = group.toComposeTreeNode(collapseEmptyNodes = true)
    val summaryNodes = composeTreeNode.summary
    val visitor =
      ComposeSummaryNodeVisitor(showInnerCalls, showStates, rawValueFormatters, viewFilter, classicViewVisitor)

    summaryNodes.forEach { summary ->
      addChildToVisit(summary, visitor)
    }
  }
}
