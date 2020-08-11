package radiography

import radiography.TreeRenderingVisitor.RenderingScope

/**
 * Renders [rootNode] as a [String] by passing it to [visitor]'s [TreeRenderingVisitor.visitNode]
 * method.
 */
internal fun <N> StringBuilder.renderTreeString(
  rootNode: N,
  visitor: TreeRenderingVisitor<N>
) {
  renderRecursively(rootNode, visitor, depth = 0, lastChildMask = 0)
}

internal abstract class TreeRenderingVisitor<in N> {

  /**
   * Renders nodes of type [N] by rendering them as strings to [RenderingScope.description] and
   * recursively rendering their children via [RenderingScope.addChildToVisit].
   *
   * Do not call this method directly, instead pass the node and this visitor to [renderTreeString].
   */
  abstract fun RenderingScope.visitNode(node: N)

  /**
   * Convenience overload of [RenderingScope.addChildToVisit] for children of the same type as this
   * visitor.
   */
  protected fun RenderingScope.addChildToVisit(childNode: N) =
    addChildToVisit(childNode, this@TreeRenderingVisitor)

  class RenderingScope(
    /**
     * A [StringBuilder] which should be used to render the current node, and will be included in
     * the final rendering before any of this node's children.
     */
    val description: StringBuilder,
    private val children: MutableList<Pair<Any?, TreeRenderingVisitor<Any?>>>
  ) {

    /**
     * Recursively visits a child of the current node using [visitor].
     */
    fun <C> addChildToVisit(
      childNode: C,
      visitor: TreeRenderingVisitor<C>
    ) {
      @Suppress("UNCHECKED_CAST")
      children += Pair(childNode, visitor as TreeRenderingVisitor<Any?>)
    }
  }
}

private fun <N> StringBuilder.renderRecursively(
  node: N,
  visitor: TreeRenderingVisitor<N>,
  depth: Int,
  lastChildMask: Long
) {
  appendLinePrefix(depth, lastChildMask)

  // Collect the children before actually visiting them. This ensures we know the full list of
  // children before we start iterating, which we need in order to be able to render the correct
  // line prefix for the last child.
  val children = mutableListOf<Pair<Any?, TreeRenderingVisitor<Any?>>>()
  val scope = RenderingScope(this@renderRecursively, children)
  with(visitor) { scope.visitNode(node) }
  @Suppress("DEPRECATION")
  appendln()

  if (children.isEmpty()) return

  val lastChildIndex = children.size - 1
  var newLastChildMask = lastChildMask
  for (index in 0..lastChildIndex) {
    if (index == lastChildIndex) {
      newLastChildMask = newLastChildMask or (1 shl depth).toLong()
    }
    val (childNode, childNodeVisitor) = children[index]
    // Never null on the main thread, but if called from another thread all bets are off.
    childNode?.let {
      renderRecursively(childNode, childNodeVisitor, depth + 1, newLastChildMask)
    }
  }
}

private fun StringBuilder.appendLinePrefix(
  depth: Int,
  lastChildMask: Long
) {
  val lastDepth = depth - 1
  // Add a non-breaking space at the beginning of the line because Logcat eats normal spaces.
  append('\u00a0')
  for (parentDepth in 0..lastDepth) {
    if (parentDepth > 0) {
      append(' ')
    }
    val lastChild = lastChildMask and (1 shl parentDepth).toLong() != 0L
    if (lastChild) {
      if (parentDepth == lastDepth) {
        append('`')
      } else {
        append(' ')
      }
    } else {
      if (parentDepth == lastDepth) {
        append('+')
      } else {
        append('|')
      }
    }
  }
  if (depth > 0) {
    append("-")
  }
}
