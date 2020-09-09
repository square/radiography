package radiography.test.compose

import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.layout.ExperimentalSubcomposeLayoutApi
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties.AccessibilityLabel
import androidx.compose.ui.semantics.SemanticsProperties.AccessibilityValue
import androidx.compose.ui.semantics.SemanticsProperties.Disabled
import androidx.compose.ui.semantics.SemanticsProperties.Focused
import androidx.compose.ui.semantics.SemanticsProperties.Hidden
import androidx.compose.ui.semantics.SemanticsProperties.IsDialog
import androidx.compose.ui.semantics.SemanticsProperties.IsPopup
import androidx.compose.ui.semantics.SemanticsProperties.TestTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.ui.test.createComposeRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import radiography.ExperimentalRadiographyComposeApi
import radiography.Radiography
import radiography.ScanScopes.composeTestTagScope
import radiography.ViewFilters.skipComposeTestTagsFilter
import radiography.ViewStateRenderers.DefaultsIncludingPii
import radiography.ViewStateRenderers.DefaultsNoPii
import radiography.ViewStateRenderers.ViewRenderer
import radiography.ViewStateRenderers.textViewRenderer

// Ktlint will complain about the extra braces in ${BLANK}, but they're required for some lines,
// so keeping them everywhere helps keep multiline strings aligned.
/* ktlint-disable string-template */
@Suppress("TestFunctionName")
@OptIn(ExperimentalRadiographyComposeApi::class, ExperimentalSubcomposeLayoutApi::class)
class ComposeUiTest {

  @get:Rule
  val composeRule = createComposeRule()

  @Test fun when_includingPii_then_hierarchyContainsText() {
    composeRule.setContentWithExplicitRoot {
      Text("FooBar")
    }

    composeRule.runOnIdle {
      val hierarchy = Radiography.scan(viewStateRenderers = DefaultsIncludingPii)
      assertThat(hierarchy).contains("FooBar")
    }
  }

  @Test fun when_noPii_then_hierarchyExcludesText() {
    composeRule.setContentWithExplicitRoot {
      Text("FooBar")
    }

    composeRule.runOnIdle {
      val hierarchy = Radiography.scan(viewStateRenderers = DefaultsNoPii)
      assertThat(hierarchy).doesNotContain("FooBar")
      assertThat(hierarchy).contains("text-length:6")
    }
  }

  @Test fun viewSizeReported() {
    composeRule.setContentWithExplicitRoot {
      val (width, height) = with(DensityAmbient.current) {
        Pair(30.toDp(), 40.toDp())
      }
      Box(modifier = Modifier.size(width, height))
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(viewStateRenderers = listOf(ViewRenderer))
    }

    assertThat(hierarchy).contains("Box { 30×40px }")
  }

  @Test fun zeroSizeViewReported() {
    composeRule.setContentWithExplicitRoot {
      Box(Modifier)
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(viewStateRenderers = emptyList())
    }

    assertThat(hierarchy).contains("Box {  }")
  }

  @Test fun semanticsAreReported() {
    composeRule.setContentWithExplicitRoot {
      Box(Modifier.semantics { set(TestTag, "test tag") })
      Box(Modifier.semantics { set(AccessibilityLabel, "acc label") })
      Box(Modifier.semantics { set(AccessibilityValue, "acc value") })
      Box(Modifier.semantics { set(Disabled, Unit) })
      Box(Modifier.semantics { set(Focused, true) })
      Box(Modifier.semantics { set(Focused, false) })
      Box(Modifier.semantics { set(Hidden, Unit) })
      Box(Modifier.semantics { set(IsDialog, Unit) })
      Box(Modifier.semantics { set(IsPopup, Unit) })
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan()
    }

    assertThat(hierarchy).contains("Box { test-tag:\"test tag\" }")
    assertThat(hierarchy).contains("Box { label:\"acc label\" }")
    assertThat(hierarchy).contains("Box { value:\"acc value\" }")
    assertThat(hierarchy).contains("Box { DISABLED }")
    assertThat(hierarchy).contains("Box { FOCUSED }")
    assertThat(hierarchy).contains("Box {  }")
    assertThat(hierarchy).contains("Box { HIDDEN }")
    assertThat(hierarchy).contains("Box { DIALOG }")
    assertThat(hierarchy).contains("Box { POPUP }")
  }

  @Test fun checkableChecked() {
    composeRule.setContentWithExplicitRoot {
      Checkbox(checked = true, onCheckedChange = {})
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(viewStateRenderers = listOf(ViewRenderer))
    }

    assertThat(hierarchy).contains("Checkbox")
    assertThat(hierarchy).contains("Checked")
  }

  @Test fun textContents() {
    composeRule.setContentWithExplicitRoot {
      Text("Baguette Avec Fromage")
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(viewStateRenderers = listOf(textViewRenderer(renderTextValue = true)))
    }

    assertThat(hierarchy).doesNotContain("text-length")
    assertThat(hierarchy).contains("text:\"Baguette Avec Fromage\"")
  }

  @Test fun textContentsEllipsized() {
    composeRule.setContentWithExplicitRoot {
      Text("Baguette Avec Fromage")
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(
          viewStateRenderers = listOf(
              textViewRenderer(
                  renderTextValue = true,
                  textValueMaxLength = 11
              )
          )
      )
    }

    assertThat(hierarchy).contains("text-length:21")
    assertThat(hierarchy).contains("text:\"Baguette A…\"")
  }

  @Test fun textExcludedByDefault() {
    composeRule.setContentWithExplicitRoot {
      Text("Baguette Avec Fromage")
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan()
    }

    assertThat(hierarchy).contains("text-length:21")
    assertThat(hierarchy).doesNotContain("text:")
  }

  @Test fun textFieldContents() {
    composeRule.setContentWithExplicitRoot {
      TextField("Baguette Avec Fromage", onValueChange = {}, label = {})
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(viewStateRenderers = listOf(textViewRenderer(renderTextValue = true)))
    }

    assertThat(hierarchy).contains("text:\"Baguette Avec Fromage\"")
  }

  @Test fun textFieldContentsEllipsized() {
    composeRule.setContentWithExplicitRoot {
      TextField("Baguette Avec Fromage", onValueChange = {}, label = {})
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(
          viewStateRenderers = listOf(
              textViewRenderer(
                  renderTextValue = true,
                  textValueMaxLength = 11
              )
          )
      )
    }

    assertThat(hierarchy).contains("text-length:21")
    assertThat(hierarchy).contains("text:\"Baguette A…\"")
  }

  @Test fun skipTestTags() {
    composeRule.setContentWithExplicitRoot {
      Box {
        Button(modifier = Modifier.testTag("42"), onClick = {}, content = {})
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(viewFilter = skipComposeTestTagsFilter("42"))
    }

    assertThat(hierarchy).contains("Box")
    assertThat(hierarchy).doesNotContain("Button")
  }

  @Test fun nestedLayouts() {
    composeRule.setContentWithExplicitRoot {
      Box(Modifier.testTag("root")) {
        Box(Modifier)
        Column {
          Box(Modifier)
          Box(Modifier)
        }
        Row {
          Box(Modifier)
          Box(Modifier)
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("root"))
    }

    @Suppress("RemoveCurlyBracesFromTemplate")
    assertThat(hierarchy).contains(
        """
          Box:
          ${BLANK}Box { test-tag:"root" }
          ${BLANK}├─Box {  }
          ${BLANK}├─Column {  }
          ${BLANK}│ ├─Box {  }
          ${BLANK}│ ╰─Box {  }
          ${BLANK}╰─Row {  }
          ${BLANK}  ├─Box {  }
          ${BLANK}  ╰─Box {  }
        """.trimIndent()
    )
  }

  @Test fun nestedViewsInsideLayouts() {
    composeRule.setContentWithExplicitRoot {
      Box(Modifier.testTag("root")) {
        AndroidView(::TextView) {
          it.layoutParams = LayoutParams(0, 0)
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("root"))
    }

    // The stuff below the AndroidView is implementation details, testing it is brittle and
    // pointless.
    @Suppress("RemoveCurlyBracesFromTemplate")
    assertThat(hierarchy).contains(
        """
        ${BLANK} Box:
          ${BLANK}Box { test-tag:"root" }
        ${BLANK} ${BLANK}╰─AndroidView {  }
        """.trimIndent()
    )
    // But this view description should show up at some point.
    assertThat(hierarchy).contains("╰─TextView { 0×0px, text-length:0 }")
  }

  @Test fun scanningHandlesDialog() {
    composeRule.setContent {
      Box(Modifier.testTag("parent")) {
        Dialog(onDismissRequest = {}) {
          Box(Modifier.testTag("child"))
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { test-tag:"parent" }
        |${BLANK}╰─Dialog {  }
        |${BLANK}  ╰─Providers { DIALOG }
        |${BLANK}    ╰─Box { test-tag:"child" }
        |
        """.trimMargin()
    )
  }

  @Test fun scanningHandlesWrappedDialog() {
    @Composable fun CustomTestDialog(children: @Composable () -> Unit) {
      Dialog(onDismissRequest = {}, children = children)
    }

    composeRule.setContent {
      Box(Modifier.testTag("parent")) {
        CustomTestDialog {
          Box(Modifier.testTag("child"))
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { test-tag:"parent" }
        |${BLANK}╰─CustomTestDialog {  }
        |${BLANK}  ╰─Providers { DIALOG }
        |${BLANK}    ╰─Box { test-tag:"child" }
        |
        """.trimMargin()
    )
  }

  @Test fun scanningHandlesSingleSubcomposeLayout_withSingleChild() {
    composeRule.setContent {
      Box(Modifier.testTag("parent")) {
        SingleSubcompositionLayout(Modifier.testTag("subcompose-layout")) {
          Box(Modifier.testTag("child"))
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { test-tag:"parent" }
        |${BLANK}╰─SingleSubcompositionLayout { test-tag:"subcompose-layout" }
        |${BLANK}  ╰─<subcomposition of SingleSubcompositionLayout> {  }
        |${BLANK}    ╰─Box { test-tag:"child" }
        |
        """.trimMargin()
    )
  }

  @Test fun scanningHandlesSingleSubcomposeLayout_withMultipleChildren() {
    composeRule.setContent {
      Box(Modifier.testTag("parent")) {
        SingleSubcompositionLayout(Modifier.testTag("subcompose-layout")) {
          Box(Modifier.testTag("child1"))
          Box(Modifier.testTag("child2"))
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { test-tag:"parent" }
        |${BLANK}╰─SingleSubcompositionLayout { test-tag:"subcompose-layout" }
        |${BLANK}  ╰─<subcomposition of SingleSubcompositionLayout> {  }
        |${BLANK}    ├─Box { test-tag:"child1" }
        |${BLANK}    ╰─Box { test-tag:"child2" }
        |
        """.trimMargin()
    )
  }

  @Test fun scanningHandlesSingleSubcomposeLayout_withMultipleSubcompositionsAndChildren() {
    composeRule.setContent {
      Box(Modifier.testTag("parent")) {
        MultipleSubcompositionLayout(Modifier.testTag("subcompose-layout"),
            firstChildren = {
              Box(Modifier.testTag("child1.1"))
              Box(Modifier.testTag("child1.2"))
            },
            secondChildren = {
              Box(Modifier.testTag("child2.1"))
              Box(Modifier.testTag("child2.2"))
            })
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { test-tag:"parent" }
        |${BLANK}╰─MultipleSubcompositionLayout { test-tag:"subcompose-layout" }
        |${BLANK}  ├─<subcomposition of MultipleSubcompositionLayout> {  }
        |${BLANK}  │ ├─Box { test-tag:"child1.1" }
        |${BLANK}  │ ╰─Box { test-tag:"child1.2" }
        |${BLANK}  ╰─<subcomposition of MultipleSubcompositionLayout> {  }
        |${BLANK}    ├─Box { test-tag:"child2.1" }
        |${BLANK}    ╰─Box { test-tag:"child2.2" }
        |
        """.trimMargin()
    )
  }

  @Test fun scanningHandlesSiblingSubcomposeLayouts() {
    composeRule.setContent {
      Box(Modifier.testTag("parent")) {
        SingleSubcompositionLayout(Modifier.testTag("subcompose-layout1")) {
          Box(Modifier.testTag("child1"))
        }
        SingleSubcompositionLayout(Modifier.testTag("subcompose-layout2")) {
          Box(Modifier.testTag("child2"))
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { test-tag:"parent" }
        |${BLANK}├─SingleSubcompositionLayout { test-tag:"subcompose-layout1" }
        |${BLANK}│ ╰─<subcomposition of SingleSubcompositionLayout> {  }
        |${BLANK}│   ╰─Box { test-tag:"child1" }
        |${BLANK}╰─SingleSubcompositionLayout { test-tag:"subcompose-layout2" }
        |${BLANK}  ╰─<subcomposition of SingleSubcompositionLayout> {  }
        |${BLANK}    ╰─Box { test-tag:"child2" }
        |
        """.trimMargin()
    )
  }

  @Test fun scanningHandlesWithConstraints() {
    composeRule.setContent {
      Box(Modifier.testTag("parent")) {
        WithConstraints(Modifier.testTag("with-constraints")) {
          Box(Modifier.testTag("child"))
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { test-tag:"parent" }
        |${BLANK}╰─WithConstraints { test-tag:"with-constraints" }
        |${BLANK}  ╰─<subcomposition of WithConstraints> {  }
        |${BLANK}    ╰─Box { test-tag:"child" }
        |
        """.trimMargin()
    )
  }

  @Test fun scanningHandlesLazyLists() {
    composeRule.setContent {
      Box(Modifier.testTag("parent")) {
        LazyColumnFor(items = listOf(1, 2, 3), modifier = Modifier.testTag("list")) {
          Box(Modifier.testTag("child:$it"))
          if (it % 2 == 0) {
            Box(Modifier.testTag("child:$it (even)"))
          }
        }
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { test-tag:"parent" }
        |${BLANK}╰─LazyColumnFor { test-tag:"list" }
        |${BLANK}  ├─<subcomposition of LazyColumnFor> {  }
        |${BLANK}  │ ╰─Box { test-tag:"child:1" }
        |${BLANK}  ├─<subcomposition of LazyColumnFor> {  }
        |${BLANK}  │ ├─Box { test-tag:"child:2" }
        |${BLANK}  │ ╰─Box { test-tag:"child:2 (even)" }
        |${BLANK}  ╰─<subcomposition of LazyColumnFor> {  }
        |${BLANK}    ╰─Box { test-tag:"child:3" }
        |
        """.trimMargin()
    )
  }

  @Test fun scanningSubcomposition_includesSize() {
    composeRule.setContent {
      // Convert 10 px to DP, since output is always in px.
      val sizeDp = with(DensityAmbient.current) { 10.toDp() }

      Box(Modifier.testTag("parent")) {
        MultipleSubcompositionLayout(Modifier.testTag("subcompose-layout"),
            firstChildren = {
              Box(Modifier.testTag("child1").size(sizeDp))
              Box(Modifier.testTag("child2").size(sizeDp))
            },
            secondChildren = {
              Box(Modifier.testTag("child3").size(sizeDp))
            }
        )
      }
    }

    val hierarchy = composeRule.runOnIdle {
      Radiography.scan(composeTestTagScope("parent"))
    }

    assertThat(hierarchy).isEqualTo(
        """
        |Providers:
        |${BLANK}Providers { 10×30px, test-tag:"parent" }
        |${BLANK}╰─MultipleSubcompositionLayout { 10×30px, test-tag:"subcompose-layout" }
        |${BLANK}  ├─<subcomposition of MultipleSubcompositionLayout> {  }
        |${BLANK}  │ ├─Box { 10×10px, test-tag:"child1" }
        |${BLANK}  │ ╰─Box { 10×10px, test-tag:"child2" }
        |${BLANK}  ╰─<subcomposition of MultipleSubcompositionLayout> {  }
        |${BLANK}    ╰─Box { 10×10px, test-tag:"child3" }
        |
        """.trimMargin()
    )
  }

  /**
   * Wrap the call to SubcomposeLayout, since real code almost never calls SubcomposeLayout
   * directly in line, so this more accurately represents a real use case.
   */
  @Composable private fun SingleSubcompositionLayout(
    modifier: Modifier,
    children: @Composable () -> Unit
  ) {
    SubcomposeLayout<Unit>(modifier) { constraints ->
      val placeables = subcompose(Unit, children)
          .map { it.measure(constraints) }

      layout(0, 0) {
        placeables.forEach { it.placeRelative(0, 0) }
      }
    }
  }

  /**
   * Like [SingleSubcompositionLayout] but creates two subcompositions, and lays out all children
   * from both compositions in a column.
   */
  @Composable private fun MultipleSubcompositionLayout(
    modifier: Modifier,
    firstChildren: @Composable () -> Unit,
    secondChildren: @Composable () -> Unit
  ) {
    SubcomposeLayout<Int>(modifier) { constraints ->
      val placeables = listOf(
          subcompose(0, firstChildren),
          subcompose(1, secondChildren),
      ).flatten().map { it.measure(constraints) }

      layout(
          width = placeables.maxOfOrNull { it.width } ?: 0,
          height = placeables.sumOf { it.height }
      ) {
        placeables.fold(0) { y, placeable ->
          placeable.placeRelative(0, y)
          y + placeable.height
        }
      }
    }
  }

  companion object {
    private const val BLANK = '\u00a0'
  }
}
