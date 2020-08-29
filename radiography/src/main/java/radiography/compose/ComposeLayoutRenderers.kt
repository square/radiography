package radiography.compose

import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsProperties.Text
import androidx.compose.ui.semantics.getOrNull
import radiography.AttributeAppendable
import radiography.ScannableView
import radiography.ScannableView.ComposeView
import radiography.ViewStateRenderer
import radiography.ellipsize

// Note that this class can't use viewStateRendererFor, since that function is defined in the
// ViewStateRenderers object, whose class initializer may initialize _this_ class, which will cause
// NoClassDefFoundExceptions. This can happen when a debugger is attached.
//
// Property initializers in this class must also all be guarded by isComposeAvailable since this
// class will be loaded even if Compose is not available on the classpath.
@ExperimentalRadiographyComposeApi
public object ComposeLayoutRenderers {

  /**
   * Renders [layoutId][androidx.compose.ui.layout.layoutId] modifiers.
   */
  @ExperimentalRadiographyComposeApi
  @JvmField
  val LayoutIdRenderer: ViewStateRenderer = if (!isComposeAvailable) NoRenderer else {
    ViewStateRenderer { view ->
      val layoutId = (view as? ComposeView)
          ?.modifiers
          ?.filterIsInstance<LayoutIdParentData>()
          ?.singleOrNull()
          ?: return@ViewStateRenderer

      val idValue = if (layoutId.id is CharSequence) {
        "\"${layoutId.id}\""
      } else {
        layoutId.id.toString()
      }
      append("layout-id:$idValue")
    }
  }

  /**
   * Renderer for standard semantics properties defined in [SemanticsProperties].
   */
  @ExperimentalRadiographyComposeApi
  @JvmField
  val StandardSemanticsRenderer: ViewStateRenderer =
    if (!isComposeAvailable) NoRenderer else {
      ViewStateRenderer {
        val semanticsConfiguration = (it as? ComposeView)
            ?.modifiers
            ?.filterIsInstance<SemanticsModifier>()
            // Technically there can be multiple semantic modifiers on a single node, so read them
            // all.
            ?.flatMap { it.semanticsConfiguration }
            ?: return@ViewStateRenderer

        semanticsConfiguration.forEach { (key, value) ->
          when (key) {
            SemanticsProperties.TestTag -> append("test-tag:\"$value\"")
            SemanticsProperties.AccessibilityLabel -> append("label:\"$value\"")
            SemanticsProperties.AccessibilityValue -> append("value:\"$value\"")
            SemanticsProperties.Disabled -> append("DISABLED")
            SemanticsProperties.Focused -> if (value == true) append("FOCUSED")
            SemanticsProperties.Hidden -> append("HIDDEN")
            SemanticsProperties.IsDialog -> append("DIALOG")
            SemanticsProperties.IsPopup -> append("POPUP")
          }
        }
      }
    }

  @ExperimentalRadiographyComposeApi
  @JvmField
  val DefaultsNoPii: List<ViewStateRenderer> =
    if (!isComposeAvailable) emptyList() else listOf(
        LayoutIdRenderer,
        composeTextRenderer(includeText = false, maxTextLength = 0),
        StandardSemanticsRenderer
    )

  @ExperimentalRadiographyComposeApi
  @JvmField
  val DefaultsIncludingPii: List<ViewStateRenderer> =
    if (!isComposeAvailable) emptyList() else listOf(
        LayoutIdRenderer,
        composeTextRenderer(includeText = true),
        StandardSemanticsRenderer
    )

  /**
   * Renders composables that expose a text value through the [Text] semantics property.
   *
   * @param includeText whether to include the string value of the property in the rendered view
   * hierarchy. Defaults to false to avoid including any PII.
   *
   * @param maxTextLength the max size of the string value of the property when [includeText] is
   * true. When the max size is reached, the text is trimmed to a [maxTextLength] - 1 length and
   * ellipsized with a '…' character.
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  @JvmOverloads
  fun composeTextRenderer(
    includeText: Boolean = false,
    maxTextLength: Int = Int.MAX_VALUE
  ): ViewStateRenderer =
    if (!isComposeAvailable) NoRenderer else ViewStateRenderer {
      val text = (it as? ComposeView)
          ?.modifiers
          ?.filterIsInstance<SemanticsModifier>()
          ?.mapNotNull { it.semanticsConfiguration.getOrNull(Text)?.text }
          ?.takeUnless { it.isEmpty() }
          ?.joinToString(separator = " ")
          ?: return@ViewStateRenderer

      append("text-length:${text.length}")
      if (includeText) {
        append("text:\"${text.ellipsize(maxTextLength)}\"")
      }
    }

  private object NoRenderer : ViewStateRenderer {
    override fun AttributeAppendable.render(view: ScannableView) {
      // Noop.
    }
  }
}
