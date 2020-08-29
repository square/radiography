package radiography

import android.view.View
import radiography.ScannableView.AndroidView

public object ViewFilters {

  /** A [ViewFilter] that matches everything (does not do any filtering). */
  @JvmStatic
  public val NoFilter: ViewFilter = ViewFilter { true }

  /**
   * Filters out root views that don't currently have the window focus from the output of
   * [Radiography.scan].
   */
  @JvmField
  public val FocusedWindowViewFilter: ViewFilter = androidViewFilterFor<View> { view ->
    view.parent?.parent != null || view.hasWindowFocus()
  }

  /**
   * Filters out views with ids matching [skippedIds] from the output of [Radiography.scan].
   */
  @JvmStatic
  public fun skipIdsViewFilter(vararg skippedIds: Int): ViewFilter =
    androidViewFilterFor<View> { view ->
      val viewId = view.id
      (viewId == View.NO_ID || skippedIds.isEmpty() || skippedIds.binarySearch(viewId) < 0)
    }

  /**
   * Creates a new filter that combines this filter with [otherFilter]
   */
  @JvmStatic
  public infix fun ViewFilter.and(otherFilter: ViewFilter): ViewFilter {
    return ViewFilter { this.matches(it) && otherFilter.matches(it) }
  }

  /**
   * Returns a [ViewFilter] that matches any [AndroidView]s whose views are instances of [T]
   * for which [predicate] returns true.
   */
  // This function is only visible to Kotlin consumers of this library.
  public inline fun <reified T : Any> androidViewFilterFor(
    noinline predicate: (T) -> Boolean
  ): ViewFilter {
    // Don't create an anonymous instance here, since that would generate a new anonymous class at
    // every call site.
    return androidViewFilterFor(T::class.java, predicate)
  }

  /**
   * Returns a [ViewFilter] that matches any [AndroidView]s whose views are instances of [T]
   * for which [predicate] returns true.
   */
  // This function is only visible to Java consumers of this library.
  @JvmStatic
  @PublishedApi internal fun <T : Any> androidViewFilterFor(
    filterClass: Class<T>,
    predicate: (T) -> Boolean
  ): ViewFilter = ViewFilter {
    val view = (it as? AndroidView)?.view ?: return@ViewFilter true
    if (!filterClass.isInstance(view)) return@ViewFilter true
    @Suppress("UNCHECKED_CAST")
    predicate(view as T)
  }
}
