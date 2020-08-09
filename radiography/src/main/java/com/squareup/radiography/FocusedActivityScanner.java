package com.squareup.radiography;

import android.app.Activity;
import android.view.View;
import org.jetbrains.annotations.NotNull;

/**
 * Keeps a reference to the root view of the currently focused {@link Activity} to enable pretty
 * printing of its entire view hierarchy.
 *
 * Usage: call {@link #setFocusedActivity(Activity)} in {@link Activity#onResume()} and {@link
 * #resetFocusedActivity()} in {@link Activity#onPause()}.
 */
public final class FocusedActivityScanner {

  private final int[] skippedIds;
  private View focusedRootView;

  public FocusedActivityScanner(int... skippedIds) {
    this.skippedIds = skippedIds;
  }

  public void resetFocusedActivity() {
    setFocusedActivity(null);
  }

  public void setFocusedActivity(Activity activity) {
    if (activity == null || activity.getWindow().getDecorView() == null) {
      focusedRootView = null;
      return;
    }
    focusedRootView = activity.getWindow().getDecorView().getRootView();
  }

  @NotNull public String scanFocusedActivity() {
    if (focusedRootView == null) {
      return "No focused root view";
    }
    return Xrays.withSkippedIds(skippedIds).scan(focusedRootView);
  }
}
