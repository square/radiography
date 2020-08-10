package radiography

import android.view.View

/**
 * Used to filter out views from the output of [Radiography.scan].
 */
interface ViewFilter {
  /**
   * @return true to keep the view in the output of [Radiography.scan], false to filter it out.
   */
  fun matches(view: View): Boolean

  object All : ViewFilter {
    override fun matches(view: View) = true
  }

  /**
   * Creates a new filter that combines this filter with [otherFilter]
   */
  infix fun and(otherFilter: ViewFilter): ViewFilter {
    val thisFilter = this
    return object : ViewFilter {
      override fun matches(view: View) = thisFilter.matches(view) &&
          otherFilter.matches(view)
    }
  }
}
