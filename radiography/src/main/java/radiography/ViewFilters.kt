package radiography

import android.view.View

public object ViewFilters {

  /** A [ViewFilter] that matches everything (does not do any filtering). */
  @JvmStatic
  public val NoFilter: ViewFilter = object : ViewFilter {
    override fun matches(view: Any): Boolean = true
  }

  /**
   * Filters out root views that don't currently have the window focus from the output of
   * [Radiography.scan].
   */
  @JvmField
  public val FocusedWindowViewFilter: ViewFilter = viewFilterFor<View> { view ->
    view.parent?.parent != null || view.hasWindowFocus()
  }

  /**
   * Filters out views with ids matching [skippedIds] from the output of [Radiography.scan].
   */
  @JvmStatic
  public fun skipIdsViewFilter(vararg skippedIds: Int): ViewFilter = viewFilterFor<View> { view ->
    val viewId = view.id
    (viewId == View.NO_ID || skippedIds.isEmpty() || skippedIds.binarySearch(viewId) < 0)
  }

  /**
   * Creates a new filter that combines this filter with [otherFilter]
   */
  @JvmStatic
  public infix fun ViewFilter.and(otherFilter: ViewFilter): ViewFilter {
    val thisFilter = this
    return object : ViewFilter {
      override fun matches(view: Any) = thisFilter.matches(view) && otherFilter.matches(view)
    }
  }

  /**
   * Returns a [ViewFilter] that matches any instances of [T] for which [predicate] returns true.
   */
  // This function is only visible to Kotlin consumers of this library.
  public inline fun <reified T : Any> viewFilterFor(noinline predicate: (T) -> Boolean): ViewFilter {
    // Don't create an anonymous instance here, since that would generate a new anonymous class at
    // every call site.
    return viewFilterFor(T::class.java, predicate)
  }

  /**
   * Returns a [ViewFilter] that matches any instances of [T] for which [predicate] returns true.
   */
  // This function is only visible to Java consumers of this library.
  @JvmStatic
  @PublishedApi internal fun <T : Any> viewFilterFor(
    filterClass: Class<T>,
    predicate: (T) -> Boolean
  ): ViewFilter = object : TypedViewFilter<T>(filterClass) {
    override fun matchesTyped(view: T): Boolean = predicate(view)
  }
}
