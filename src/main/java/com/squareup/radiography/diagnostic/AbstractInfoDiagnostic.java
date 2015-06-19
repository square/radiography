package com.squareup.radiography.diagnostic;

import android.view.View;
import java.util.Collections;
import java.util.List;

import static com.squareup.radiography.diagnostic.Diagnosis.Severity.INFO;

/** Abstract base class for diagnostics that return a simple INFO diagnosis. */
public abstract class AbstractInfoDiagnostic implements Diagnostic {

  @Override public final List<Diagnosis> diagnose(View view) {
    String message = message(view);
    if (message == null) {
      return null;
    }

    Diagnosis diagnosis = new Diagnosis(INFO, message, view);
    return Collections.singletonList(diagnosis);
  }

  /** Returns an informative message about the view, or null. */
  protected abstract String message(View view);
}
