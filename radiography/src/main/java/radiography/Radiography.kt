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
   * [includeTextViewText] is true. When the max size is reached, the text is trimmed to
   * a [textViewTextMaxLength] - 1 length and ellipsized with a '…' character.
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

    val config = Config(
        includeTextViewText, textViewTextMaxLength, viewFilter
    )

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
        config.scanRecursively(this, 0, 0, view)
      } catch (e: Throwable) {
        insert(
            startPosition,
            "Exception when going through view hierarchy: ${e.message}\n"
        )
      }
    }
  }

  private class Config(
    val includeTextViewText: Boolean,
    val textViewTextMaxLength: Int,
    val viewFilter: ViewFilter
  )

  private fun Config.scanRecursively(
    result: StringBuilder,
    depth: Int,
    lastChildMask: Long,
    view: View
  ) {
    @Suppress("NAME_SHADOWING")
    var lastChildMask = lastChildMask
    appendLinePrefix(result, depth, lastChildMask)
    viewToString(result, view)
    result.appendln()

    if (view is ViewGroup) {
      val lastNonSkippedChildIndex = findLastNonSkippedChildIndex(view)
      val lastChildIndex = view.childCount - 1
      for (index in 0..lastChildIndex) {
        if (index == lastNonSkippedChildIndex) {
          lastChildMask = lastChildMask or (1 shl depth).toLong()
        }
        val child = view.getChildAt(index)
        // Never null on the main thread, but if called from another thread all bets are off.
        child?.let {
          if (viewFilter.matches(child)) {
            scanRecursively(result, depth + 1, lastChildMask, child)
          }
        }
      }
    }
  }

  private fun Config.findLastNonSkippedChildIndex(viewGroup: ViewGroup): Int {
    val lastChildIndex = viewGroup.childCount - 1
    for (index in lastChildIndex downTo 0) {
      val child = viewGroup.getChildAt(index)
      if (viewFilter.matches(child)) {
        return index
      }
    }
    return -1
  }

  @TargetApi(CUPCAKE)
  private fun Config.viewToString(
    result: StringBuilder,
    view: View
  ) {
    result.append("${view.javaClass.simpleName} { ")
    if (view.id != View.NO_ID && view.resources != null) {
      try {
        val resourceName = view.resources.getResourceEntryName(view.id)
        result.append("id:$resourceName, ")
      } catch (ignore: NotFoundException) {
        // Do nothing.
      }
    }

    when (view.visibility) {
      View.GONE -> result.append("GONE, ")
      View.INVISIBLE -> result.append("INVISIBLE, ")
    }

    result.append("${view.width}x${view.height}px")

    if (view.isFocused) {
      result.append(", focused")
    }

    if (!view.isEnabled) {
      result.append(", disabled")
    }

    if (view.isSelected) {
      result.append(", selected")
    }

    if (view is TextView) {
      var text = view.text
      if (text != null) {
        result.append(", text-length:${text.length}")
        if (includeTextViewText) {
          if (text.length > textViewTextMaxLength) {
            text = "${text.subSequence(0, textViewTextMaxLength - 1)}…"
          }
          result.append(", text:\"$text\"")
        }
      }
      if (view.isInputMethodTarget) {
        result.append(", ime-target")
      }
    }
    if (view is Checkable) {
      if (view.isChecked) {
        result.append(", checked")
      }
    }
    result.append(" }")
  }

  private fun appendLinePrefix(
    result: StringBuilder,
    depth: Int,
    lastChildMask: Long
  ) {
    val lastDepth = depth - 1
    // Add a non-breaking space at the beginning of the line because Logcat eats normal spaces.
    result.append('\u00a0')
    for (parentDepth in 0..lastDepth) {
      if (parentDepth > 0) {
        result.append(' ')
      }
      val lastChild = lastChildMask and (1 shl parentDepth).toLong() != 0L
      if (lastChild) {
        if (parentDepth == lastDepth) {
          result.append('`')
        } else {
          result.append(' ')
        }
      } else {
        if (parentDepth == lastDepth) {
          result.append('+')
        } else {
          result.append('|')
        }
      }
    }
    if (depth > 0) {
      result.append("-")
    }
  }
}
