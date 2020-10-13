package radiography.test.compose

import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.TextField
import androidx.compose.runtime.SlotTable
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
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

@OptIn(ExperimentalRadiographyComposeApi::class)
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
    val slotTable = Ref<SlotTable>()
    composeRule.setContentWithExplicitRoot {
      slotTable.value = currentComposer.slotTable

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
    val slotTable = Ref<SlotTable>()
    composeRule.setContentWithExplicitRoot {
      slotTable.value = currentComposer.slotTable

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
          Box:
          ${BLANK}Box { test-tag:"root" }
          ${BLANK}╰─AndroidView {  }
        """.trimIndent()
    )
    // But this view description should show up at some point.
    assertThat(hierarchy).contains("╰─TextView { 0×0px, text-length:0 }")
  }

  companion object {
    private const val BLANK = '\u00a0'
  }
}
