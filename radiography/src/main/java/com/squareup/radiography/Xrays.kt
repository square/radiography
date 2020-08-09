package com.squareup.radiography

import android.annotation.TargetApi
import android.content.res.Resources.NotFoundException
import android.os.Build.VERSION_CODES.CUPCAKE
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Checkable
import android.widget.TextView
import java.util.Arrays

/**
 * Utility class to scan through a view hierarchy and pretty print it to a [String] or
 * [StringBuilder].
 */
class Xrays private constructor(builder: Builder) {

  private val showTextFieldContent: Boolean = builder.showTextFieldContent
  private val textFieldMaxLength: Int = builder.textFieldMaxLength
  private val skippedIds: IntArray =
    if (builder.skippedIds == null) IntArray(0) else builder.skippedIds!!

  class Builder {
    internal var showTextFieldContent = false
    internal var textFieldMaxLength = Int.MAX_VALUE
    internal var skippedIds: IntArray? = null

    fun showTextFieldContent(showTextFieldContent: Boolean): Builder = apply {
      this.showTextFieldContent = showTextFieldContent
    }

    fun textFieldMaxLength(textFieldMaxLength: Int): Builder = apply {
      require(textFieldMaxLength > 0) { "textFieldMaxLength=$textFieldMaxLength <= 0" }
      this.textFieldMaxLength = textFieldMaxLength
    }

    /**
     * @param skippedIds View ids that should be ignored. Can be useful if you want to ignore some
     * debug views.
     */
    fun skippedIds(vararg skippedIds: Int): Builder = apply {
      this.skippedIds = skippedIds.clone()
    }

    fun build(): Xrays = Xrays(this)
  }

  /**
   * Looks for all windows using reflection, and then scans the view hierarchy of each window.
   *
   * Since we're using reflection, it may stop working at some point. If the returned string is
   * empty, you can fallback to the other scanning methods.
   *
   * @see scan
   */
  fun scanAllWindows(): String = buildString {
    val rootViews = WindowScanner.findAllRootViews()

    for (view in rootViews) {
      if (length > 0) {
        append("\n")
      }
      val layoutParams = view!!.layoutParams
      val title = (layoutParams as? LayoutParams)?.title?.toString()
          ?: view.javaClass.name
      appendln("$title:")
      scan(this, view)
    }
  }

  /**
   * Goes up to the root parent of the given view before printing the view hierarchy.
   *
   * @see scan
   */
  fun scanFromRoot(view: View): String {
    return scan(view.rootView)
  }

  /**
   * Goes up to the root parent of the given view before printing the view hierarchy.
   *
   * @see scan
   */
  fun scanFromRoot(
    result: StringBuilder,
    view: View
  ) {
    scan(result, view.rootView)
  }

  /** @see scan
   */
  fun scan(view: View?): String {
    val result = StringBuilder()
    scan(result, view)
    return result.toString()
  }

  /**
   * @param result A container in which the view hierarchy is pretty printed by appending to the
   * [StringBuilder].
   * @param view the parent view that gets its hierarchy pretty printed.
   */
  fun scan(
    result: StringBuilder,
    view: View?
  ) {
    val startPosition = result.length
    try {
      if (view != null) {
        result.appendln("window-focus:${view.hasWindowFocus()}")
      }
      scanRecursively(result, 0, 0, view)
    } catch (e: Throwable) {
      result.insert(
          startPosition,
          "Exception when going through view hierarchy: ${e.message}\n"
      )
    }
  }

  private fun scanRecursively(
    result: StringBuilder,
    depth: Int,
    lastChildMask: Long,
    view: View?
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
        if (!skipChild(child)) {
          scanRecursively(result, depth + 1, lastChildMask, child)
        }
      }
    }
  }

  private fun findLastNonSkippedChildIndex(viewGroup: ViewGroup): Int {
    val lastChildIndex = viewGroup.childCount - 1
    for (index in lastChildIndex downTo 0) {
      val child = viewGroup.getChildAt(index)
      if (!skipChild(child)) {
        return index
      }
    }
    return -1
  }

  private fun skipChild(child: View): Boolean {
    val childId = child.id
    return childId != View.NO_ID && Arrays.binarySearch(skippedIds, childId) >= 0
  }

  @TargetApi(CUPCAKE)
  private fun viewToString(
    result: StringBuilder,
    view: View?
  ) {
    if (view == null) {
      result.append("null")
      return
    }

    result.append("${view.javaClass.simpleName} { ")
    if (view.id != -1 && view.resources != null) {
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
        if (showTextFieldContent) {
          if (text.length > textFieldMaxLength) {
            text = "${text.subSequence(0, textFieldMaxLength - 1)}…"
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

  companion object {
    @JvmStatic fun create(): Xrays = Builder().build()

    @JvmStatic fun withSkippedIds(vararg skippedIds: Int): Xrays {
      return Builder().skippedIds(*skippedIds)
          .build()
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
}
