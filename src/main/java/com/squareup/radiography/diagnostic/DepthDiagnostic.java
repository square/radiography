package com.squareup.radiography.diagnostic;

import android.view.View;
import android.view.ViewParent;
import java.util.Collections;
import java.util.List;

import static com.squareup.radiography.diagnostic.Diagnosis.Severity.ERROR;
import static com.squareup.radiography.diagnostic.Diagnosis.Severity.INFO;
import static com.squareup.radiography.diagnostic.Diagnosis.Severity.WARNING;

public class DepthDiagnostic implements Diagnostic {

  // TODO: What should these values be?
  public static final int DEFAULT_WARNING_DEPTH = 15;
  public static final int DEFAULT_ERROR_DEPTH = 18;

  private final int warningDepth;
  private final int errorDepth;

  public DepthDiagnostic() {
    this(DEFAULT_WARNING_DEPTH, DEFAULT_ERROR_DEPTH);
  }

  public DepthDiagnostic(int warningDepth, int errorDepth) {
    this.warningDepth = warningDepth;
    this.errorDepth = errorDepth;
  }

  @Override public List<Diagnosis> diagnose(View view) {
    int depth = 0;
    ViewParent parent = view.getParent();
    while (parent != null && parent instanceof View) {
      depth++;
      parent = ((View) parent).getParent();
    }

    Diagnosis.Severity severity = INFO;
    if (depth >= errorDepth) {
      severity = ERROR;
    } else if (depth >= warningDepth) {
      severity = WARNING;
    }

    Diagnosis diagnosis = new Diagnosis(severity, "depth:" + depth, view);
    return Collections.singletonList(diagnosis);
  }
}
