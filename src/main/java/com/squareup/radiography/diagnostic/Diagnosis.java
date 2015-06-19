package com.squareup.radiography.diagnostic;

import android.view.View;

/** The result of a {@link Diagnostic}. */
public class Diagnosis {

  public static enum Severity {
    INFO, WARNING, ERROR
  }

  public final Severity severity;
  public final String message;
  public final View view;

  public Diagnosis(Severity severity, String message, View view) {
    this.severity = severity;
    this.message = message;
    this.view = view;
  }
}
