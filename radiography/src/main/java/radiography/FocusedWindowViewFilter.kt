package radiography

import android.view.View

/**
 * Filters out root views that don't currently have the window focus from the output of [Radiography.scan].
 */
object FocusedWindowViewFilter : ViewFilter {

  override fun matches(view: View): Boolean {
    return view.parent?.parent != null || view.hasWindowFocus()
  }
}
