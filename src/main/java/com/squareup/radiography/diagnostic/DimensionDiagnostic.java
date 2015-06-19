package com.squareup.radiography.diagnostic;

import android.view.View;
import java.util.List;

public class DimensionDiagnostic extends AbstractInfoDiagnostic {
  @Override protected String message(View view) {
    return view.getWidth() + "x" + view.getHeight() + "px";
  }
}
