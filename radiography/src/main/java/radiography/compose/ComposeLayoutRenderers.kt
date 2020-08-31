package radiography.compose

import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.platform.InspectableParameter
import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsProperties.Text
import androidx.compose.ui.semantics.SemanticsPropertyKey
import radiography.AttributeAppendable
import radiography.TypedViewStateRenderer
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
    object : TypedViewStateRenderer<InspectableParameter>(InspectableParameter::class.java) {
      override fun AttributeAppendable.renderTyped(rendered: InspectableParameter) {
        if (rendered is LayoutIdParentData) {
          val idValue = if (rendered.id is CharSequence) {
            "\"${rendered.id}\""
          } else {
            rendered.id.toString()
          }
          append("layout-id:$idValue")
        }
      }
    }
  }

  /**
   * Renderer for standard semantics properties defined in [SemanticsProperties].
   */
  @ExperimentalRadiographyComposeApi
  @JvmField
  val StandardSemanticsRenderer: ViewStateRenderer =
    if (!isComposeAvailable) NoRenderer else {
      object : TypedViewStateRenderer<SemanticsModifier>(SemanticsModifier::class.java) {
        override fun AttributeAppendable.renderTyped(rendered: SemanticsModifier) {
          rendered.semanticsConfiguration.forEach { (key, value) ->
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
    if (!isComposeAvailable) NoRenderer else semanticsRendererFor(Text) { text ->
      append("text-length:${text.text.length}")
      if (includeText) {
        append("text:\"${text.text.ellipsize(maxTextLength)}\"")
      }
    }

  /**
   * Renders a [SemanticsPropertyKey].
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  internal fun <T> semanticsRendererFor(
    key: SemanticsPropertyKey<T>,
    render: AttributeAppendable.(T) -> Unit
  ): ViewStateRenderer =
    object : TypedViewStateRenderer<SemanticsModifier>(SemanticsModifier::class.java) {
      override fun AttributeAppendable.renderTyped(rendered: SemanticsModifier) {
        rendered.semanticsConfiguration.forEach { (k, value) ->
          if (key == k) {
            @Suppress("UNCHECKED_CAST")
            render(value as T)
          }
        }
      }
    }

  private object NoRenderer : ViewStateRenderer {
    override fun AttributeAppendable.render(rendered: Any) {
      // Noop.
    }
  }
}
