package com.squareup.radiography.diagnostic;

import android.content.res.Resources;
import android.view.View;
import java.util.List;

public class IdDiagnostic extends AbstractInfoDiagnostic {
  @Override protected String message(View view) {
    if (view.getId() == -1 || view.getResources() == null) {
      return null;
    }

    try {
      String resourceName = view.getResources().getResourceEntryName(view.getId());
      return "id:" + resourceName;
    } catch (Resources.NotFoundException ignore) {
      return null;
    }
  }
}
