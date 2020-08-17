package radiography

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RenderTreeStringTest {

  @Test fun `renderTreeString handles single node`() {
    val tree = Node("root")

    val rendering = buildString { renderTreeString(tree, NodeVisitor) }

    assertThat(rendering).isEqualTo("${BLANK}root\n")
  }

  @Test fun `renderTreeString handles deep skinny tree`() {
    val tree = Node("root", Node("1", Node("11")))

    val rendering = buildString { renderTreeString(tree, NodeVisitor) }

    assertThat(rendering).isEqualTo(
        """
          |${BLANK}root
          |$BLANK`-1
          |$BLANK  `-11
          |
        """.trimMargin()
    )
  }

  @Test fun `renderTreeString handles shallow bushy tree`() {
    val tree = Node(
        "root",
        Node("1"),
        Node("2"),
        Node("3")
    )

    val rendering = buildString { renderTreeString(tree, NodeVisitor) }

    assertThat(rendering).isEqualTo(
        """
          |${BLANK}root
          |$BLANK+-1
          |$BLANK+-2
          |$BLANK`-3
          |
        """.trimMargin()
    )
  }

  @Test fun `renderTreeString handles bushy tree with initial deep subtree`() {
    val tree = Node(
        "root",
        Node("1", Node("11", Node("111"))),
        Node("2"),
        Node("3")
    )

    val rendering = buildString { renderTreeString(tree, NodeVisitor) }

    assertThat(rendering).isEqualTo(
        """
          |${BLANK}root
          |$BLANK+-1
          |$BLANK| `-11
          |$BLANK|   `-111
          |$BLANK+-2
          |$BLANK`-3
          |
        """.trimMargin()
    )
  }

  @Test fun `renderTreeString handles bushy tree with last deep subtree`() {
    val tree = Node(
        "root",
        Node("1"),
        Node("2"),
        Node("3", Node("33", Node("333")))
    )

    val rendering = buildString { renderTreeString(tree, NodeVisitor) }

    assertThat(rendering).isEqualTo(
        """
          |${BLANK}root
          |$BLANK+-1
          |$BLANK+-2
          |$BLANK`-3
          |$BLANK  `-33
          |$BLANK    `-333
          |
        """.trimMargin()
    )
  }

  @Test fun `renderTreeString handles maximum depth tree`() {
    val tree = generateSequence(Node("leaf")) { child ->
      Node("node", Node("leaf"), child)
    }
        .take(33)
        .last()

    val rendering = buildString { renderTreeString(tree, NodeVisitor) }
    val lastTwoLines = rendering.trim()
        .lines()
        .takeLast(2)
        .map { it.trim() }

    assertThat(lastTwoLines).containsExactly(
        "+-leaf",
        "`-leaf"
    )
  }

  @Test fun `renderTreeString handles over maximum depth tree`() {
    val tree = generateSequence(Node("leaf")) { child ->
      Node("node", Node("leaf"), child)
    }
        .take(34)
        .last()

    val rendering = buildString { renderTreeString(tree, NodeVisitor) }
    val lastTwoLines = rendering.trim()
        .lines()
        .takeLast(2)
        .map { it.trim() }

    assertThat(lastTwoLines).containsExactly(
        "`-leaf",
        "`-leaf"
    )
  }

  @Test fun `renderTreeString handles multiline nodes`() {
    val tree = Node(
        "root1\nroot2",
        Node("1\n1", Node("11\n11")),
        Node("2\n2")
    )

    val rendering = buildString { renderTreeString(tree, NodeVisitor) }

    assertThat(rendering).isEqualTo(
        """
          |${BLANK}root1
          |${BLANK}root2
          |$BLANK+-1
          |$BLANK| 1
          |$BLANK| `-11
          |$BLANK|   11
          |$BLANK`-2
          |$BLANK  2
          |
        """.trimMargin()
    )
  }

  @Test fun `addChildToVisit with different visitor`() {
    val tree = Node(
        "root",
        Node("regular child"),
        Node("w:parse me")
    )
    val wordVisitor = object : TreeRenderingVisitor<Pair<Int, String>>() {
      override fun RenderingScope.visitNode(node: Pair<Int, String>) {
        val (index, word) = node
        description.append("[$index] $word")
      }
    }
    val mainVisitor = object : TreeRenderingVisitor<Node>() {
      override fun RenderingScope.visitNode(node: Node) {
        description.append(node.description)
        if (node.description.startsWith("w:")) {
          node.description.removePrefix("w:")
              .split("\\s".toRegex())
              .forEachIndexed { index, word ->
                addChildToVisit(Pair(index, word), wordVisitor)
              }
        } else {
          node.children.forEach { addChildToVisit(it) }
        }
      }
    }

    val rendering = buildString { renderTreeString(tree, mainVisitor) }

    assertThat(rendering).isEqualTo(
        """
          |${BLANK}root
          |$BLANK+-regular child
          |$BLANK`-w:parse me
          |$BLANK  +-[0] parse
          |$BLANK  `-[1] me
          |
        """.trimMargin()
    )
  }

  private object NodeVisitor : TreeRenderingVisitor<Node>() {
    override fun RenderingScope.visitNode(node: Node) {
      description.append(node.description)
      node.children.forEach { addChildToVisit(it) }
    }
  }

  private class Node(
    val description: String,
    vararg val children: Node = emptyArray()
  )

  companion object {
    private const val BLANK = '\u00a0'
  }
}
