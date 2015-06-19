package com.squareup.radiography.diagnostic;

import android.view.View;
import java.util.List;

public class FocusedDiagnostic extends AbstractInfoDiagnostic {
  @Override protected String message(View view) {
    if (view.isFocused()) {
      return "focused";
    }
    return null;
  }
}
