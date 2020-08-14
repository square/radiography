package radiography

import android.view.View
import radiography.ViewStateRenderers.DefaultsNoPii

/**
 * Extension function for [Radiography.scan] when scanning starts from a specific view.
 * @see Radiography.scan
 */
@JvmSynthetic
public fun View?.scan(
  viewStateRenderers: List<ViewStateRenderer> = DefaultsNoPii,
  viewFilter: ViewFilter = ViewFilter.All
): String {
  return if (this == null) {
    "null"
  } else {
    Radiography.scan(this, viewStateRenderers, viewFilter)
  }
}
