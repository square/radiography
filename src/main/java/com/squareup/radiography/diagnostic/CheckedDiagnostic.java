package com.squareup.radiography.diagnostic;

import android.view.View;
import android.widget.Checkable;
import java.util.List;

public class CheckedDiagnostic extends AbstractInfoDiagnostic {
  @Override protected String message(View view) {
    if (view instanceof Checkable) {
      Checkable checkable = (Checkable) view;
      if (checkable.isChecked()) {
        return "checked";
      }
    }
    return null;
  }
}
