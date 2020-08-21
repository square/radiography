package radiography

import radiography.ViewFilter.FilterResult
import radiography.ViewFilter.FilterResult.INCLUDE

/**
 * Used to filter out views from the output of [Radiography.scan].
 */
// TODO this isn't really just a "filter" anymore, it's more of a "selector". Rename?
public interface ViewFilter {

  enum class FilterResult {
    /** Include this view and all of its children which also match the filter. */
    INCLUDE,

    /** Exclude this view, but include all of its children which match the filter. */
    INCLUDE_ONLY_CHILDREN,

    /** Exclude this view and don't process any of its children. */
    EXCLUDE
  }

  /**
   * @return true to keep the view in the output of [Radiography.scan], false to filter it out.
   */
  public fun matches(view: Any): FilterResult
}

/**
 * Base class for implementations of [ViewFilter] that only want to filter instances of a specific
 * type. Instances of other types are always [included][INCLUDE] by this filter.
 */
internal abstract class TypedViewFilter<in T : Any>(
  private val filterClass: Class<T>
) : ViewFilter {
  public abstract fun matchesTyped(view: T): FilterResult

  final override fun matches(view: Any): FilterResult {
    if (!filterClass.isInstance(view)) return INCLUDE

    @Suppress("UNCHECKED_CAST")
    return matchesTyped(view as T)
  }
}
