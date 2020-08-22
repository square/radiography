package radiography

import android.annotation.TargetApi
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.ViewGroup
import radiography.compose.tryVisitComposeView

/**
 * A [TreeRenderingVisitor] that renders [View]s and their children which match [viewFilter] using
 * [viewStateRenderers].
 */
internal class ViewTreeRenderingVisitor(
  private val viewStateRenderers: List<ViewStateRenderer>,
  private val viewFilter: ViewFilter
) : TreeRenderingVisitor<View>() {

  override fun RenderingScope.visitNode(node: View) {
    description.viewToString(node)

    val isComposeView = tryVisitComposeView(
        this, node, viewStateRenderers, viewFilter, this@ViewTreeRenderingVisitor
    )
    if (isComposeView) {
      // Don't visit children ourselves, the compose renderer will have done that.
      return
    }

    if (node !is ViewGroup) return

    // Capture this value, since it might change while we're iterating.
    val childCount = node.childCount
    for (index in 0 until childCount) {
      // Child may be null, if children were removed by another thread after we captured the child
      // count. getChildAt returns null for invalid indices, it doesn't throw.
      val child = node.getChildAt(index) ?: continue
      if (viewFilter.matches(child)) {
        addChildToVisit(child)
      }
    }
  }

  @TargetApi(VERSION_CODES.CUPCAKE)
  private fun StringBuilder.viewToString(view: View) {
    append("${view.javaClass.simpleName} { ")
    val appendable = AttributeAppendable(this)
    for (renderer in viewStateRenderers) {
      with(renderer) {
        appendable.render(view)
      }
    }
    append(" }")
  }
}
