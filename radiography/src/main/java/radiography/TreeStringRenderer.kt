package radiography

internal abstract class TreeStringRenderer<N> {

  abstract fun StringBuilder.renderNode(node: N)

  abstract fun N.getChildAt(index: Int): N?

  abstract val N.childCount: Int

  abstract fun N.matches(): Boolean

  fun render(
    stringBuilder: StringBuilder,
    rootNode: N
  ) {
    stringBuilder.renderRecursively(rootNode, 0, 0)
  }

  private fun StringBuilder.renderRecursively(
    node: N,
    depth: Int,
    lastChildMask: Long
  ) {
    appendLinePrefix(depth, lastChildMask)
    renderNode(node)
    @Suppress("DEPRECATION")
    appendln()
    val childCount = node.childCount
    if (childCount > 0) {
      val lastNonSkippedChildIndex = node.findLastNonSkippedChildIndex()
      val lastChildIndex = childCount - 1
      var newLastChildMask = lastChildMask
      for (index in 0..lastChildIndex) {
        if (index == lastNonSkippedChildIndex) {
          newLastChildMask = newLastChildMask or (1 shl depth).toLong()
        }
        val childNode = node.getChildAt(index)
        // Never null on the main thread, but if called from another thread all bets are off.
        childNode?.let {
          if (childNode.matches()) {
            renderRecursively(childNode, depth + 1, newLastChildMask)
          }
        }
      }
    }
  }

  private fun N.findLastNonSkippedChildIndex(): Int {
    val lastChildIndex = childCount - 1
    for (index in lastChildIndex downTo 0) {
      val child = getChildAt(index)
      if (child != null && child.matches()) {
        return index
      }
    }
    return -1
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
}
