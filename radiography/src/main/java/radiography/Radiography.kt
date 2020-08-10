package radiography

import android.annotation.TargetApi
import android.content.res.Resources.NotFoundException
import android.os.Build.VERSION_CODES.CUPCAKE
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Checkable
import android.widget.TextView
import radiography.Radiography.scan

/**
 * Utility class to scan through a view hierarchy and pretty print it to a [String].
 * Call [scan] or [View.scan].
 */
object Radiography {

  /**
   * Scans the view hierarchies and pretty print them to a [String].
   *
   * You should generally call this method from the main thread, as views are meant to be accessed
   * from a single thread. If you call this from a background thread, it may work or the views
   * might throw an exception. This method will not throw, instead the exception message will be
   * included in the returned string.
   *
   * @param rootView if not null, scanning starts from [rootView] and goes down recursively (you
   * can call the extension function [View.scan] instead). If null, scanning retrieves all windows
   * for the current process using reflection and then scans the view hierarchy for each window.
   *
   * @param includeTextViewText whether to include the string content of TextView instances in
   * the rendered view hierarchy. Defaults to false to avoid including any PII.
   *
   * @param textViewTextMaxLength the max size of the string content of TextView instances when
   * [includeTextViewText] is true. When the max size is reached, the text is trimmed and
   * ellipsized with a '…' character.
   *
   * @param viewFilter a filter to exclude specific views from the rendering. If a view is excluded
   * then all of its children are excluded as well. Use [SkipIdsViewFilter] to ignore views that
   * match specific ids (e.g. a debug drawer). Use [FocusedWindowViewFilter] to keep only the
   * views of the currently focused window, if any.
   */
  @JvmStatic
  fun scan(
    rootView: View? = null,
    includeTextViewText: Boolean = false,
    textViewTextMaxLength: Int = Int.MAX_VALUE,
    viewFilter: ViewFilter = ViewFilter.All
  ): String = buildString {
    if (includeTextViewText) {
      check(textViewTextMaxLength >= 0) {
        "textFieldMaxLength should be greater than 0, not $textViewTextMaxLength"
      }
    }

    val rootViews = rootView?.let {
      listOf(it)
    } ?: WindowScanner.findAllRootViews()

    val matchingRootViews = rootViews.filter(viewFilter::matches)

    val renderer = object : TreeStringRenderer<View>() {
      override fun StringBuilder.renderNode(node: View) {
        viewToString(node, includeTextViewText, textViewTextMaxLength)
      }

      override fun View.getChildAt(index: Int): View? {
        return if (this is ViewGroup) getChildAt(index) else null
      }

      override val View.childCount: Int
        get() = if (this is ViewGroup) childCount else 0

      override fun View.matches() = viewFilter.matches(this)
    }

    for (view in matchingRootViews) {
      if (length > 0) {
        appendln()
      }
      val layoutParams = view.layoutParams
      val title = (layoutParams as? WindowManager.LayoutParams)?.title?.toString()
          ?: view.javaClass.name
      appendln("$title:")

      val startPosition = length
      try {
        appendln("window-focus:${view.hasWindowFocus()}")
        renderer.render(this, view)
      } catch (e: Throwable) {
        insert(
            startPosition,
            "Exception when going through view hierarchy: ${e.message}\n"
        )
      }
    }
  }

  @TargetApi(CUPCAKE)
  private fun StringBuilder.viewToString(
    view: View,
    includeTextViewText: Boolean,
    textViewTextMaxLength: Int
  ) {
    append("${view.javaClass.simpleName} { ")
    if (view.id != View.NO_ID && view.resources != null) {
      try {
        val resourceName = view.resources.getResourceEntryName(view.id)
        append("id:$resourceName, ")
      } catch (ignore: NotFoundException) {
        // Do nothing.
      }
    }

    when (view.visibility) {
      View.GONE -> append("GONE, ")
      View.INVISIBLE -> append("INVISIBLE, ")
    }

    append("${view.width}x${view.height}px")

    if (view.isFocused) {
      append(", focused")
    }

    if (!view.isEnabled) {
      append(", disabled")
    }

    if (view.isSelected) {
      append(", selected")
    }

    if (view is TextView) {
      var text = view.text
      if (text != null) {
        append(", text-length:${text.length}")
        if (includeTextViewText) {
          if (text.length > textViewTextMaxLength) {
            text = "${text.subSequence(0, textViewTextMaxLength - 1)}…"
          }
          append(", text:\"$text\"")
        }
      }
      if (view.isInputMethodTarget) {
        append(", ime-target")
      }
    }
    if (view is Checkable) {
      if (view.isChecked) {
        append(", checked")
      }
    }
    append(" }")
  }
}
