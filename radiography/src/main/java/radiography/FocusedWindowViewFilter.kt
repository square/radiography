package radiography

import android.view.View

/**
 * Filters out root views that don't currently have the window focus from the output of [Radiography.scan].
 */
public object FocusedWindowViewFilter : ViewFilter {

  override fun matches(view: Any): Boolean {
    return view is View && (view.parent?.parent != null || view.hasWindowFocus())
  }
}
