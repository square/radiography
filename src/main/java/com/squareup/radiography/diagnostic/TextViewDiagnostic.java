package com.squareup.radiography.diagnostic;

import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.CUPCAKE;
import static com.squareup.radiography.diagnostic.Diagnosis.Severity.INFO;

public class TextViewDiagnostic implements Diagnostic {

  private final boolean showTextFieldContent;
  private final int textFieldMaxLength;

  public TextViewDiagnostic() {
    this(false);
  }

  public TextViewDiagnostic(boolean showTextFieldContent) {
    this(showTextFieldContent, Integer.MAX_VALUE);
  }

  public TextViewDiagnostic(boolean showTextFieldContent, int textFieldMaxLength) {
    this.showTextFieldContent = showTextFieldContent;
    this.textFieldMaxLength = textFieldMaxLength;
  }

  @Override public List<Diagnosis> diagnose(View view) {
    if (!(view instanceof TextView)) {
      return null;
    }

    List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
    TextView textView = (TextView) view;
    CharSequence text = textView.getText();
    if (text != null) {
      diagnoses.add(new Diagnosis(INFO, "text-length:" + text.length(), view));
      if (showTextFieldContent) {
        if (text.length() > textFieldMaxLength) {
          text = text.subSequence(0, textFieldMaxLength - 1) + "…";
        }
        diagnoses.add(new Diagnosis(INFO, "text:\"" + text + "\"", view));
      }
    }

    if (SDK_INT >= CUPCAKE && textView.isInputMethodTarget()) {
      diagnoses.add(new Diagnosis(INFO, "ime-target", view));
    }

    return diagnoses.isEmpty() ? null : diagnoses;
  }
}
