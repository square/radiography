package radiography

/**
 * Used to filter out views from the output of [Radiography.scan].
 */
public fun interface ViewFilter {
  /**
   * @return true to keep the view in the output of [Radiography.scan], false to filter it out.
   */
  public fun matches(view: Any): Boolean
}

/**
 * Base class for implementations of [ViewFilter] that only want to filter instances of a specific
 * type. Instances of other types are always "matched" by this filter.
 */
internal abstract class TypedViewFilter<in T : Any>(
  private val filterClass: Class<T>
) : ViewFilter {
  public abstract fun matchesTyped(view: T): Boolean

  final override fun matches(view: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    return !filterClass.isInstance(view) || matchesTyped(view as T)
  }
}
