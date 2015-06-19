package com.squareup.radiography.diagnostic;

import android.view.View;
import java.util.List;

public class DisabledDiagnostic extends AbstractInfoDiagnostic {
  @Override protected String message(View view) {
    if (!view.isEnabled()) {
      return "disabled";
    }
    return null;
  }
}
