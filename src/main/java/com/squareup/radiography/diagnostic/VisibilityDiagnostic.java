package com.squareup.radiography.diagnostic;

import android.view.View;
import java.util.List;

public class VisibilityDiagnostic extends AbstractInfoDiagnostic {
  @Override protected String message(View view) {
    switch (view.getVisibility()) {
      case View.GONE:
        return "GONE";
      case View.INVISIBLE:
        return "INVISIBLE";
      default:
        return null;
    }
  }
}
