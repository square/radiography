package radiography

import android.view.View
import radiography.ViewStateRenderers.defaultsNoPii

/**
 * Extension function for [Radiography.scan] when scanning starts from a specific view.
 * @see Radiography.scan
 */
@JvmSynthetic
fun View?.scan(
  stateRenderers: List<StateRenderer<*>> = defaultsNoPii,
  viewFilter: ViewFilter = ViewFilter.All
): String {
  return if (this == null) {
    "null"
  } else {
    Radiography.scan(this, stateRenderers, viewFilter)
  }
}
