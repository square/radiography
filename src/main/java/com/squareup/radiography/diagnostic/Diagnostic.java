package com.squareup.radiography.diagnostic;

import android.view.View;
import java.util.List;

/** Analyzes a view and return a list of {@link Diagnosis} if any are found. */
public interface Diagnostic {

  /**
   * Examines the view returns information, warnings, and errors.
   *
   * @param view the view whose attribute is to be rendered
   * @return a list of diagnoses, or null if none are found
   */
  List<Diagnosis> diagnose(View view);
}
