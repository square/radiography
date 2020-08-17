@file:JvmName("ViewFilters")
package radiography

/**
 * Used to filter out views from the output of [Radiography.scan].
 */
public interface ViewFilter {
  /**
   * @return true to keep the view in the output of [Radiography.scan], false to filter it out.
   */
  public fun matches(view: Any): Boolean

  public object All : ViewFilter {
    override fun matches(view: Any): Boolean = true
  }
}

/**
 * Creates a new filter that combines this filter with [otherFilter]
 */
public infix fun ViewFilter.and(otherFilter: ViewFilter): ViewFilter {
  val thisFilter = this
  return object : ViewFilter {
    override fun matches(view: Any) = thisFilter.matches(view) && otherFilter.matches(view)
  }
}
