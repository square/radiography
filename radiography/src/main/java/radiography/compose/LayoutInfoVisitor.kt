package radiography.compose

import android.view.View
import androidx.compose.ui.unit.toSize
import radiography.AttributeAppendable
import radiography.ScannableView.AndroidView
import radiography.ScannableView.ComposeView
import radiography.TreeRenderingVisitor
import radiography.ViewFilter
import radiography.ViewStateRenderer

/**
 * A [TreeRenderingVisitor] that recursively renders a tree of [ComposeLayoutInfo]s. It is the
 * Compose analog to [radiography.ViewTreeRenderingVisitor].
 */
@OptIn(ExperimentalRadiographyComposeApi::class)
internal class LayoutInfoVisitor(
  private val modifierRenderers: List<ViewStateRenderer>,
  private val viewFilter: ViewFilter,
  private val classicViewVisitor: TreeRenderingVisitor<View>
) : TreeRenderingVisitor<ComposeLayoutInfo>() {

  override fun RenderingScope.visitNode(node: ComposeLayoutInfo) {
    try {
      visitNodeAssumingComposeSupported(node)
    } catch (e: LinkageError) {
      // The Compose code on the classpath is not what we expected – the app is probably using a
      // newer (or older) version of Compose than we support.
      description.appendln(COMPOSE_UNSUPPORTED_MESSAGE)
      description.append("Error: ${e.message}")
    }
  }

  private fun RenderingScope.visitNodeAssumingComposeSupported(node: ComposeLayoutInfo) {
    with(description) {
      append(node.name)

      append(" { ")
      val appendable = AttributeAppendable(description)
      val composeView = ComposeView(node.bounds.toSize(), node.modifiers)
      modifierRenderers.forEach { renderer ->
        with(renderer) {
          appendable.render(composeView)
        }
      }
      append(" }")
    }

    // Visit LayoutNode children. View nodes don't seem to have children, but they theoretically
    // could so try to visit them just in case.
    node.children
        .filter { viewFilter.matches(ComposeView(it.bounds.toSize(), it.modifiers)) }
        .forEach {
          addChildToVisit(it)
        }

    // This node was an emitted Android View, so trampoline back to the View renderer.
    node.view?.takeIf { viewFilter.matches(AndroidView(it)) }?.let { view ->
      addChildToVisit(view, classicViewVisitor)
    }
  }
}
