package radiography

import android.annotation.SuppressLint
import android.content.res.Resources.NotFoundException
import android.view.View
import android.widget.Checkable
import android.widget.TextView
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsProperties.Text
import androidx.compose.ui.semantics.getOrNull
import radiography.ScannableView.AndroidView
import radiography.ScannableView.ComposeView
import radiography.compose.ExperimentalRadiographyComposeApi
import radiography.compose.isComposeAvailable

@OptIn(ExperimentalRadiographyComposeApi::class)
public object ViewStateRenderers {

  private val NoRenderer: ViewStateRenderer = ViewStateRenderer {
    // Noop.
  }

  private val AndroidViewRenderer: ViewStateRenderer = androidViewStateRendererFor<View> { view ->
    if (view.id != View.NO_ID && view.resources != null) {
      try {
        val resourceName = view.resources.getResourceEntryName(view.id)
        append("id:$resourceName")
      } catch (ignore: NotFoundException) {
        // Do nothing.
      }
    }

    @SuppressLint("SwitchIntDef")
    when (view.visibility) {
      View.GONE -> append("GONE")
      View.INVISIBLE -> append("INVISIBLE")
    }

    append(formatPixelDimensions(view.width, view.height))

    if (view.isFocused) {
      append("focused")
    }

    if (!view.isEnabled) {
      append("disabled")
    }

    if (view.isSelected) {
      append("selected")
    }
  }

  @ExperimentalRadiographyComposeApi
  private val ComposeViewRenderer: ViewStateRenderer = if (!isComposeAvailable) NoRenderer else {
    ViewStateRenderer {
      val composeView = it as? ComposeView ?: return@ViewStateRenderer

      // Dimensions
      composeView.apply {
        if (width != 0 || height != 0) {
          append(formatPixelDimensions(width, height))
        }
      }

      // Semantics
      composeView
          .modifiers
          .filterIsInstance<SemanticsModifier>()
          // Technically there can be multiple semantic modifiers on a single node, so read them
          // all.
          .flatMap { semantics -> semantics.semanticsConfiguration }
          .forEach { (key, value) ->
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

      // Layout ID
      composeView.modifiers
          .filterIsInstance<LayoutIdParentData>()
          .singleOrNull()
          ?.let { layoutId ->
            val idValue = if (layoutId.id is CharSequence) {
              "\"${layoutId.id}\""
            } else {
              layoutId.id.toString()
            }
            append("layout-id:$idValue")
          }
    }
  }

  @JvmField
  public val ViewRenderer: ViewStateRenderer = ViewStateRenderer { view ->
    with(AndroidViewRenderer) { render(view) }
    with(ComposeViewRenderer) { render(view) }
  }

  @JvmField
  public val CheckableRenderer: ViewStateRenderer =
    androidViewStateRendererFor<Checkable> { checkable ->
      if (checkable.isChecked) {
        append("checked")
      }
    }

  @JvmField
  public val DefaultsNoPii: List<ViewStateRenderer> = listOf(
      ViewRenderer,
      textViewRenderer(showTextValue = false, textValueMaxLength = 0),
      CheckableRenderer,
  )

  @JvmField
  public val DefaultsIncludingPii: List<ViewStateRenderer> = listOf(
      ViewRenderer,
      textViewRenderer(showTextValue = true),
      CheckableRenderer,
  )

  /**
   * Renders information about [TextView]s and text composables.
   *
   * @param showTextValue whether to include the string content of TextView instances in
   * the rendered view hierarchy. Defaults to false to avoid including any PII.
   *
   * @param textValueMaxLength the max size of the string content of TextView instances when
   * [showTextValue] is true. When the max size is reached, the text is trimmed to
   * a [textValueMaxLength] - 1 length and ellipsized with a '…' character.
   */
  @JvmStatic
  @JvmOverloads
  public fun textViewRenderer(
    showTextValue: Boolean = false,
    textValueMaxLength: Int = Int.MAX_VALUE
  ): ViewStateRenderer {
    if (showTextValue) {
      check(textValueMaxLength >= 0) {
        "textFieldMaxLength should be greater than 0, not $textValueMaxLength"
      }
    }

    val androidTextViewRenderer = androidViewStateRendererFor<TextView> { textView ->
      appendTextValue(textView.text, showTextValue, textValueMaxLength)
      if (textView.isInputMethodTarget) {
        append("ime-target")
      }
    }
    val composeTextRenderer = composeTextRenderer(showTextValue, textValueMaxLength)

    return ViewStateRenderer { view ->
      with(androidTextViewRenderer) { render(view) }
      with(composeTextRenderer) { render(view) }
    }
  }

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
  @JvmSynthetic
  internal fun composeTextRenderer(
    includeText: Boolean = false,
    maxTextLength: Int = Int.MAX_VALUE
  ): ViewStateRenderer = if (!isComposeAvailable) NoRenderer else ViewStateRenderer { view ->
    val text = (view as? ComposeView)
        ?.modifiers
        ?.filterIsInstance<SemanticsModifier>()
        ?.mapNotNull { it.semanticsConfiguration.getOrNull(Text)?.text }
        ?.takeUnless { it.isEmpty() }
        ?.joinToString(separator = " ")
        ?: return@ViewStateRenderer

    appendTextValue(text, includeText, maxTextLength)
  }

  /**
   * Creates a [ViewStateRenderer] that renders [AndroidView]s with views of type [T].
   */
  // This function is only visible to Kotlin consumers of this library.
  public inline fun <reified T : Any> androidViewStateRendererFor(
    noinline renderer: AttributeAppendable.(T) -> Unit
  ): ViewStateRenderer {
    // Don't create an anonymous instance of ViewStateRenderer here, since that would generate a new
    // anonymous class at every call site.
    return androidViewStateRendererFor(T::class.java, renderer)
  }

  /**
   * Creates a [ViewStateRenderer] that renders [AndroidView]s with views of type [T].
   */
  // This function is only visible to Java consumers of this library.
  @JvmStatic
  @PublishedApi internal fun <T : Any> androidViewStateRendererFor(
    renderedClass: Class<T>,
    renderer: AttributeAppendable.(T) -> Unit
  ): ViewStateRenderer = ViewStateRenderer { scannableView ->
    val view = (scannableView as? AndroidView)
        ?.view
        ?: return@ViewStateRenderer
    if (!renderedClass.isInstance(view)) return@ViewStateRenderer
    @Suppress("UNCHECKED_CAST")
    renderer(view as T)
  }

  internal fun AttributeAppendable.appendTextValue(
    text: CharSequence?,
    showTextValue: Boolean,
    textValueMaxLength: Int
  ) {
    if (text == null) return

    val appendTextLength = if (showTextValue) {
      val ellipsizedText = text.ellipsize(textValueMaxLength)
      append("text:\"$ellipsizedText\"")
      ellipsizedText.length != text.length
    } else {
      true
    }
    if (appendTextLength) {
      append("text-length:${text.length}")
    }
  }
}
