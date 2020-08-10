package com.squareup.radiography

import android.app.Activity
import android.view.View

/**
 * Keeps a reference to the root view of the currently focused [Activity] to enable pretty
 * printing of its entire view hierarchy.
 *
 * Usage: call [setFocusedActivity] in [Activity.onResume] and [resetFocusedActivity] in
 * [Activity.onPause].
 */
class FocusedActivityScanner(private vararg val skippedIds: Int) {

  private var focusedRootView: View? = null

  fun resetFocusedActivity() {
    setFocusedActivity(null)
  }

  fun setFocusedActivity(activity: Activity?) {
    if (activity?.window?.decorView == null) {
      focusedRootView = null
      return
    }
    focusedRootView = activity.window.decorView.rootView
  }

  fun scanFocusedActivity(): String {
    return if (focusedRootView == null) {
      "No focused root view"
    } else {
      Xrays.withSkippedIds(*skippedIds)
          .scan(focusedRootView)
    }
  }
}
