package com.squareup.radiography.diagnostic;

import android.view.View;
import java.util.List;

public class SelectedDiagnostic extends AbstractInfoDiagnostic {
  @Override protected String message(View view) {
    if (view.isSelected()) {
      return "selected";
    }
    return null;
  }
}
