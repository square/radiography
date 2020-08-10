package radiography

internal abstract class TreeStringRenderer<N> {

  abstract fun StringBuilder.renderNode(node: N)
  abstract val N.children: Collection<N?>

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
    appendln()

    val children = node.children
    if (children.isNotEmpty()) {
      val lastChildIndex = children.size - 1
      var newLastChildMask = lastChildMask
      children.forEachIndexed { index, childNode ->
        if (index == lastChildIndex) {
          newLastChildMask = newLastChildMask or (1 shl depth).toLong()
        }
        // Never null on the main thread, but if called from another thread all bets are off.
        childNode?.let {
          renderRecursively(childNode, depth + 1, newLastChildMask)
        }
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
}
