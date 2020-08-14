package radiography

import android.view.View

/**
 * Filters out views with ids matching [skippedIds] from the output of [Radiography.scan].
 */
public class SkipIdsViewFilter(private vararg val skippedIds: Int) : ViewFilter {

  override fun matches(view: Any): Boolean {
    if (view !is View) return false
    val viewId = view.id
    return (viewId == View.NO_ID || skippedIds.isEmpty() || skippedIds.binarySearch(viewId) < 0)
  }
}
