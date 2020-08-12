package radiography

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import radiography.Radiography.scan
import radiography.ViewStateRenderers.defaultsNoPii
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicReference

/**
 * Utility class to scan through a view hierarchy and pretty print it to a [String].
 * Call [scan] or [View.scan].
 */
object Radiography {

  /**
   * Scans the view hierarchies and pretty print them to a [String].
   *
   * You should generally call this method from the main thread, as views are meant to be accessed
   * from a single thread. If you call this from a background thread, this will schedule a message
   * to the main thread to retrieve the view hierarchy from there and will wait up to 5 seconds
   * or return an error message. This method will never throw, any thrown exception will have
   * its message included in the returned string.
   *
   * @param rootView if not null, scanning starts from [rootView] and goes down recursively (you
   * can call the extension function [View.scan] instead). If null, scanning retrieves all windows
   * for the current process using reflection and then scans the view hierarchy for each window.
   *
   * @param viewStateRenderers render extra attributes for specifics types, in order.
   *
   * @param viewFilter a filter to exclude specific views from the rendering. If a view is excluded
   * then all of its children are excluded as well. Use [SkipIdsViewFilter] to ignore views that
   * match specific ids (e.g. a debug drawer). Use [FocusedWindowViewFilter] to keep only the
   * views of the currently focused window, if any.
   */
  @JvmStatic
  @JvmOverloads
  fun scan(
    rootView: View? = null,
    viewStateRenderers: List<ViewStateRenderer<*>> = defaultsNoPii,
    viewFilter: ViewFilter = ViewFilter.All
  ): String {

    // The entire view tree is single threaded, and that's typically the main thread, but
    // it doesn't have to be, and we don't know where the passed in view is coming from.
    val viewLooper = rootView?.handler?.looper ?: Looper.getMainLooper()!!

    if (viewLooper.thread == Thread.currentThread()) {
      return scanFromLooperThread(rootView, viewStateRenderers, viewFilter)
    }
    val latch = CountDownLatch(1)
    val hierarchyString = AtomicReference<String>()
    Handler(viewLooper).post {
      hierarchyString.set(scanFromLooperThread(rootView, viewStateRenderers, viewFilter))
      latch.countDown()
    }
    return if (latch.await(5, SECONDS)) {
      hierarchyString.get()!!
    } else {
      "Could not retrieve view hierarchy from main thread after 5 seconds wait"
    }
  }

  private fun scanFromLooperThread(
    rootView: View?,
    viewStateRenderers: List<ViewStateRenderer<*>>,
    viewFilter: ViewFilter
  ): String = buildString {
    val rootViews = rootView?.let {
      listOf(it)
    } ?: WindowScanner.findAllRootViews()

    val matchingRootViews = rootViews.filter(viewFilter::matches)
    val viewVisitor = ViewTreeRenderingVisitor(viewStateRenderers, viewFilter)

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
