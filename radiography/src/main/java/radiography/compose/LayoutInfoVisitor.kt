package radiography.compose

import android.view.View
import androidx.compose.ui.unit.IntBounds
import radiography.AttributeAppendable
import radiography.TreeRenderingVisitor
import radiography.ViewFilter
import radiography.ViewFilter.FilterResult.EXCLUDE
import radiography.ViewFilter.FilterResult.INCLUDE
import radiography.ViewFilter.FilterResult.INCLUDE_ONLY_CHILDREN
import radiography.ViewStateRenderer
import radiography.formatPixelDimensions

/**
 * A [TreeRenderingVisitor] that recursively renders a tree of [ComposeLayoutInfo]s. It is the
 * Compose analog to [radiography.ViewTreeRenderingVisitor].
 */
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
      description?.appendln(COMPOSE_UNSUPPORTED_MESSAGE)
      description?.append("Error: ${e.message}")
    }
  }

  private fun RenderingScope.visitNodeAssumingComposeSupported(node: ComposeLayoutInfo) {
    description?.layoutInfoToString(node)

    // Visit LayoutNode children. View nodes don't seem to have children, but they theoretically
    // could so try to visit them just in case.
    node.children
        .forEach { childInfo ->
          when (viewFilter.matches(childInfo)) {
            INCLUDE -> addChildToVisit(childInfo, this@LayoutInfoVisitor)
            INCLUDE_ONLY_CHILDREN -> addChildToVisit(childInfo, this@LayoutInfoVisitor, skip = true)
            EXCLUDE -> {
              // Noop.
            }
          }
        }

    // This node was an emitted Android View, so trampoline back to the View renderer.
    when (node.view?.let(viewFilter::matches)) {
      INCLUDE -> addChildToVisit(node.view, classicViewVisitor)
      INCLUDE_ONLY_CHILDREN -> addChildToVisit(node.view, classicViewVisitor, skip = true)
      else -> {
        // Noop.
      }
    }
  }

  private fun StringBuilder.layoutInfoToString(node: ComposeLayoutInfo) {
    append(node.name)

    append(" { ")
    val appendable = AttributeAppendable(this)
    node.bounds.describeSize()?.let(appendable::append)

    node.modifiers.forEach { modifier ->
      modifierRenderers.forEach { renderer ->
        with(renderer) {
          appendable.render(modifier)
        }
      }
    }
    append(" }")
  }
}

private fun IntBounds.describeSize(): String? {
  return if (left != right || top != bottom) {
    formatPixelDimensions(width = right - left, height = bottom - top)
  } else {
    null
  }
}
