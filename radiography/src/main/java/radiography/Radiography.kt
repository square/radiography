package radiography

import android.view.View
import android.view.WindowManager
import radiography.Radiography.scan
import radiography.ViewStateRenderers.defaultsNoPii

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
   * @param stateRenderers render extra attributes for specifics types, in order.
   *
   * @param viewFilter a filter to exclude specific views from the rendering. If a view is excluded
   * then all of its children are excluded as well. Use [SkipIdsViewFilter] to ignore views that
   * match specific ids (e.g. a debug drawer). Use [FocusedWindowViewFilter] to keep only the
   * views of the currently focused window, if any.
   */
  @JvmStatic
  fun scan(
    rootView: View? = null,
    stateRenderers: List<StateRenderer<*>> = defaultsNoPii,
    viewFilter: ViewFilter = ViewFilter.All
  ): String = buildString {
    val rootViews = rootView?.let {
      listOf(it)
    } ?: WindowScanner.findAllRootViews()

    val matchingRootViews = rootViews.filter(viewFilter::matches)
    val viewVisitor = ViewTreeRenderingVisitor(stateRenderers, viewFilter)

    for (view in matchingRootViews) {
      if (length > 0) {
        @Suppress("DEPRECATION")
        appendln()
      }
      val layoutParams = view.layoutParams
      val title = (layoutParams as? WindowManager.LayoutParams)?.title?.toString()
          ?: view.javaClass.name
      @Suppress("DEPRECATION")
      appendln("$title:")

      val startPosition = length
      try {
        @Suppress("DEPRECATION")
        appendln("window-focus:${view.hasWindowFocus()}")
        renderTreeString(view, viewVisitor)
      } catch (e: Throwable) {
        insert(
            startPosition,
            "Exception when going through view hierarchy: ${e.message}\n"
        )
      }
    }
  }
}
