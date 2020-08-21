package radiography

import android.view.View
import radiography.ViewFilter.FilterResult
import radiography.ViewFilter.FilterResult.EXCLUDE
import radiography.ViewFilter.FilterResult.INCLUDE
import radiography.ViewFilter.FilterResult.INCLUDE_ONLY_CHILDREN

public object ViewFilters {

  /** A [ViewFilter] that matches everything (does not do any filtering). */
  @JvmStatic
  public val NoFilter: ViewFilter = object : ViewFilter {
    override fun matches(view: Any) = INCLUDE
  }

  /**
   * Filters out root views that don't currently have the window focus from the output of
   * [Radiography.scan].
   */
  @JvmField
  public val FocusedWindowViewFilter: ViewFilter = viewFilterFor<View> { view ->
    if (view.parent?.parent != null || view.hasWindowFocus()) INCLUDE else EXCLUDE
  }

  /**
   * Filters out views with ids matching [skippedIds] from the output of [Radiography.scan].
   */
  @JvmStatic
  public fun skipIdsViewFilter(vararg skippedIds: Int): ViewFilter = viewFilterFor<View> { view ->
    val viewId = view.id
    if (viewId == View.NO_ID ||
        skippedIds.isEmpty() ||
        skippedIds.binarySearch(viewId) < 0
    ) INCLUDE else EXCLUDE
  }

  /**
   * Creates a new filter that combines this filter with [otherFilter]
   */
  // TODO unit tests for all combinations of FilterResults.
  @JvmStatic
  public infix fun ViewFilter.and(otherFilter: ViewFilter): ViewFilter {
    val thisFilter = this
    return object : ViewFilter {
      override fun matches(view: Any): FilterResult {
        val thisResult = thisFilter.matches(view)
        val otherResult = otherFilter.matches(view)

        if (thisResult == EXCLUDE || otherResult == EXCLUDE) return EXCLUDE
        if (thisResult == INCLUDE && otherResult == INCLUDE) return INCLUDE

        // At least one filter wants to exclude this node, but both filters want to include
        // children.
        return INCLUDE_ONLY_CHILDREN
      }
    }
  }

  /** Returns a [ViewFilter] that matches instances of [T] using [matches]. */
  // This function is only visible to Kotlin consumers of this library.
  public inline fun <reified T : Any> viewFilterFor(
    noinline matches: (T) -> FilterResult
  ): ViewFilter {
    // Don't create an anonymous instance here, since that would generate a new anonymous class at
    // every call site.
    return viewFilterFor(T::class.java, matches)
  }

  /** Returns a [ViewFilter] that matches instances of [T] using [matches]. */
  // This function is only visible to Java consumers of this library.
  @JvmStatic
  @PublishedApi internal fun <T : Any> viewFilterFor(
    filterClass: Class<T>,
    matches: (T) -> FilterResult
  ): ViewFilter = object : TypedViewFilter<T>(filterClass) {
    override fun matchesTyped(view: T) = matches(view)
  }
}
