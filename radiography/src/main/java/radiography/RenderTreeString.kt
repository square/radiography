package radiography

import radiography.TreeRenderingVisitor.ChildToVisit
import radiography.TreeRenderingVisitor.RenderingScope
import java.util.ArrayDeque
import java.util.BitSet

/**
 * Renders [rootNode] as a [String] by passing it to [visitor]'s [TreeRenderingVisitor.visitNode]
 * method.
 *
 * @param skip If true, this node's description will be ignored and its children will be treated
 * as the roots.
 */
internal fun <N> StringBuilder.renderTreeString(
  rootNode: N,
  visitor: TreeRenderingVisitor<N>,
  skip: Boolean = false
) {
  renderRecursively(rootNode, visitor, skip = skip, depth = 0, lastChildMask = BitSet())
}

internal abstract class TreeRenderingVisitor<in N> {

  internal data class ChildToVisit<N>(
    val node: N,
    val visitor: TreeRenderingVisitor<N>,
    val skip: Boolean
  )

  /**
   * Renders nodes of type [N] by rendering them as strings to [RenderingScope.description] and
   * recursively rendering their children via [RenderingScope.addChildToVisit].
   *
   * Do not call this method directly, instead pass the node and this visitor to [renderTreeString].
   */
  abstract fun RenderingScope.visitNode(node: N)

  class RenderingScope(
    /**
     * A [StringBuilder] which should be used to render the current node, and will be included in
     * the final rendering before any of this node's children.
     *
     * If null, this node is being skipped, and no description will be rendered.
     */
    val description: StringBuilder?,
    private val children: MutableCollection<ChildToVisit<Any?>>
  ) {

    /**
     * Recursively visits a child of the current node using [visitor].
     *
     * @param skip If true, [childNode]'s description will be ignored, and its children (this node's
     * grandchildren) will be rendered as direct children of this node.
     */
    // TODO unit tests for when skip=true
    fun <C> addChildToVisit(
      childNode: C,
      visitor: TreeRenderingVisitor<C>,
      skip: Boolean = false
    ) {
      @Suppress("UNCHECKED_CAST")
      children += ChildToVisit(childNode, visitor as TreeRenderingVisitor<Any?>, skip)
    }
  }
}

private fun <N> StringBuilder.renderRecursively(
  node: N,
  visitor: TreeRenderingVisitor<N>,
  depth: Int,
  skip: Boolean,
  lastChildMask: BitSet
) {
  // Collect the children before actually visiting them. This ensures we know the full list of
  // children before we start iterating, which we need in order to be able to render the correct
  // line prefix for the last child.
  val children = ArrayDeque<ChildToVisit<Any?>>()

  // Render node into a separate buffer to append a prefix to every line.
  val nodeDescription = StringBuilder()
  val scope = RenderingScope(nodeDescription, children)
  with(visitor) { scope.visitNode(node) }

  if (!skip) {
    nodeDescription.lineSequence().forEachIndexed { index, line ->
      appendLinePrefix(depth, continuePreviousLine = index > 0, lastChildMask = lastChildMask)
      @Suppress("DEPRECATION")
      appendln(line)
    }
  }

  while (children.isNotEmpty()) {
    val (childNode, childNodeVisitor, skipChild) = children.removeFirst()
    if (!skipChild) {
      if (children.isEmpty()) {
        // No more children can be enqueued for this node, so we can be certain this is the last
        // child.
        lastChildMask.set(depth)
      }

      // Never null on the main thread, but if called from another thread all bets are off.
      childNode?.let {
        renderRecursively(childNode, childNodeVisitor, depth + 1, skipChild, lastChildMask)
      }
    } else {
      // Visit the child directly, without generating a description but adding its children to our
      // queue to be processed by this loop.
      val childScope = RenderingScope(null, children)
      with(childNodeVisitor) { childScope.visitNode(childNode) }
    }
  }

  // Unset the bit we set above before returning.
  lastChildMask.clear(depth)
}

private fun StringBuilder.appendLinePrefix(
  depth: Int,
  continuePreviousLine: Boolean,
  lastChildMask: BitSet
) {
  val lastDepth = depth - 1
  // Add a non-breaking space at the beginning of the line because Logcat eats normal spaces.
  append('\u00a0')
  for (parentDepth in 0..lastDepth) {
    if (parentDepth > 0) {
      append(' ')
    }
    val lastChild = lastChildMask[parentDepth]
    if (lastChild) {
      if (parentDepth == lastDepth && !continuePreviousLine) {
        append('`')
      } else {
        append(' ')
      }
    } else {
      if (parentDepth == lastDepth && !continuePreviousLine) {
        append('+')
      } else {
        append('|')
      }
    }
  }
  if (depth > 0) {
    if (continuePreviousLine) {
      append(" ")
    } else {
      append("-")
    }
  }
}
