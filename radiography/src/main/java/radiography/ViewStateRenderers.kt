package radiography

import android.content.res.Resources.NotFoundException
import android.view.View
import android.widget.Checkable
import android.widget.TextView

public object ViewStateRenderers {

  @JvmField
  public val ViewRenderer: ViewStateRenderer = viewStateRendererFor<View> { view ->
    if (view.id != View.NO_ID && view.resources != null) {
      try {
        val resourceName = view.resources.getResourceEntryName(view.id)
        append("id:$resourceName")
      } catch (ignore: NotFoundException) {
        // Do nothing.
      }
    }

    when (view.visibility) {
      View.GONE -> append("GONE")
      View.INVISIBLE -> append("INVISIBLE")
    }

    append("${view.width}×${view.height}px")

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

  @JvmField
  public val CheckableRenderer: ViewStateRenderer = viewStateRendererFor<Checkable> { checkable ->
    if (checkable.isChecked) {
      append("checked")
    }
  }

  @JvmField
  public val DefaultsNoPii: List<ViewStateRenderer> = listOf(
      ViewRenderer,
      textViewRenderer(includeTextViewText = false, textViewTextMaxLength = 0),
      CheckableRenderer
  )

  @JvmField
  public val DefaultsIncludingPii: List<ViewStateRenderer> = listOf(
      ViewRenderer,
      textViewRenderer(includeTextViewText = true),
      CheckableRenderer
  )

  /**
   * @param includeTextViewText whether to include the string content of TextView instances in
   * the rendered view hierarchy. Defaults to false to avoid including any PII.
   *
   * @param textViewTextMaxLength the max size of the string content of TextView instances when
   * [includeTextViewText] is true. When the max size is reached, the text is trimmed to
   * a [textViewTextMaxLength] - 1 length and ellipsized with a '…' character.
   */
  @JvmStatic
  @JvmOverloads
  public fun textViewRenderer(
    includeTextViewText: Boolean = false,
    textViewTextMaxLength: Int = Int.MAX_VALUE
  ): ViewStateRenderer {
    if (includeTextViewText) {
      check(textViewTextMaxLength >= 0) {
        "textFieldMaxLength should be greater than 0, not $textViewTextMaxLength"
      }
    }
    return viewStateRendererFor<TextView> { textView ->
      var text = textView.text
      if (text != null) {
        append("text-length:${text.length}")
        if (includeTextViewText) {
          if (text.length > textViewTextMaxLength) {
            text = "${text.subSequence(0, textViewTextMaxLength - 1)}…"
          }
          append("text:\"$text\"")
        }
      }
      if (textView.isInputMethodTarget) {
        append("ime-target")
      }
    }
  }

  /**
   * Creates a [ViewStateRenderer] that renders views of type [T].
   */
  // This function is only visible to Kotlin consumers of this library.
  public inline fun <reified T : Any> viewStateRendererFor(
    noinline renderer: AttributeAppendable.(T) -> Unit
  ): ViewStateRenderer {
    // Don't create an anonymous instance of ViewStateRenderer here, since that would generate a new
    // anonymous class at every call site.
    return viewStateRendererFor(T::class.java, renderer)
  }

  /**
   * Creates a [ViewStateRenderer] that renders views of type [T].
   */
  // This function is only visible to Java consumers of this library.
  @JvmStatic
  @PublishedApi internal fun <T : Any> viewStateRendererFor(
    renderedClass: Class<T>,
    renderer: AttributeAppendable.(T) -> Unit
  ): ViewStateRenderer = object : TypedViewStateRenderer<T>(renderedClass) {
    override fun AttributeAppendable.renderTyped(rendered: T) {
      renderer(rendered)
    }
  }
}
