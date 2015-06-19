package com.squareup.radiography;

import android.annotation.TargetApi;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.squareup.radiography.diagnostic.CheckedDiagnostic;
import com.squareup.radiography.diagnostic.Diagnosis;
import com.squareup.radiography.diagnostic.Diagnostic;
import com.squareup.radiography.diagnostic.DimensionDiagnostic;
import com.squareup.radiography.diagnostic.DisabledDiagnostic;
import com.squareup.radiography.diagnostic.FocusedDiagnostic;
import com.squareup.radiography.diagnostic.IdDiagnostic;
import com.squareup.radiography.diagnostic.SelectedDiagnostic;
import com.squareup.radiography.diagnostic.TextViewDiagnostic;
import com.squareup.radiography.diagnostic.VisibilityDiagnostic;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.CUPCAKE;
import static android.view.View.NO_ID;
import static com.squareup.radiography.diagnostic.Diagnosis.Severity.INFO;
import static java.util.Arrays.binarySearch;

/**
 * Utility class to scan through a view hierarchy and pretty print it to a {@link String} or
 * {@link StringBuilder}.
 */
public final class Xrays {

  private final int[] skippedIds;
  private final List<Diagnostic> diagnostics;

  private Xrays(Builder builder) {
    this.skippedIds = builder.skippedIds == null ? new int[0] : builder.skippedIds;
    this.diagnostics = builder.diagnostics;
  }

  public static class Builder {
    private int[] skippedIds;
    private final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

    public Builder() {
      // Add default diagnostics.
      withDiagnostic(new IdDiagnostic());
      withDiagnostic(new VisibilityDiagnostic());
      withDiagnostic(new DimensionDiagnostic());
      withDiagnostic(new FocusedDiagnostic());
      withDiagnostic(new DisabledDiagnostic());
      withDiagnostic(new SelectedDiagnostic());
      withDiagnostic(new TextViewDiagnostic());
      withDiagnostic(new CheckedDiagnostic());
    }

    /**
     * @param skippedIds View ids that should be ignored. Can be useful if you want to ignore some
     * debug views.
     */
    public Builder skippedIds(int... skippedIds) {
      this.skippedIds = skippedIds == null ? null : skippedIds.clone();
      return this;
    }

    public Builder clearDiagnostics() {
      this.diagnostics.clear();
      return this;
    }

    public Builder withDiagnostic(Diagnostic diagnostic) {
      this.diagnostics.add(diagnostic);
      return this;
    }

    public Xrays build() {
      return new Xrays(this);
    }
  }

  public static Xrays create() {
    return new Builder().build();
  }

  public static Xrays withSkippedIds(int... skippedIds) {
    return new Builder().skippedIds(skippedIds).build();
  }

  /**
   * Looks for all windows using reflection, and then scans the view hierarchy of each window.
   *
   * Since we're using reflection, it may stop working at some point. If the returned string is
   * empty, you can fallback to the other scanning methods.
   *
   * @see #scan(StringBuilder, View)
   */
  public String scanAllWindows() {
    StringBuilder result = new StringBuilder();

    List<View> rootViews = WindowScanner.getInstance().findAllRootViews();

    for (View view : rootViews) {
      if (result.length() > 0) {
        result.append("\n");
      }
      ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
      String title = null;
      if (layoutParams instanceof WindowManager.LayoutParams) {
        WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) layoutParams;
        CharSequence windowTitle = windowParams.getTitle();
        if (windowTitle != null) {
          title = windowTitle.toString();
        }
      }
      if (title == null) {
        title = view.getClass().getName();
      }
      result.append(title).append(":\n");
      scan(result, view);
    }
    return result.toString();
  }

  /**
   * Goes up to the root parent of the given view before printing the view hierarchy.
   *
   * @see #scan(StringBuilder, View)
   */
  public String scanFromRoot(View view) {
    return scan(view.getRootView());
  }

  /**
   * Goes up to the root parent of the given view before printing the view hierarchy.
   *
   * @see #scan(StringBuilder, View)
   */
  public void scanFromRoot(StringBuilder result, View view) {
    scan(result, view.getRootView());
  }

  /** @see #scan(StringBuilder, View) */
  public String scan(View view) {
    StringBuilder result = new StringBuilder();
    scan(result, view);
    return result.toString();
  }

  /**
   * @param result A container in which the view hierachy is pretty printed by appending to the
   * {@link StringBuilder}.
   * @param view the parent view that gets its hierarchy pretty printed.
   */
  public void scan(StringBuilder result, View view) {
    int startPosition = result.length();
    try {
      if (view != null) {
        result.append("window-focus:").append(view.hasWindowFocus()).append("\n");
      }
      scanRecursively(result, 0, 0, view);
    } catch (Throwable e) {
      result.insert(startPosition,
          "Exception when going through view hierarchy: " + e.getMessage() + "\n");
    }
  }

  private void scanRecursively(StringBuilder result, int depth, long lastChildMask,
      View view) {
    appendLinePrefix(result, depth, lastChildMask);
    viewToString(result, view);
    result.append('\n');

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      int lastNonSkippedChildIndex = findLastNonSkippedChildIndex(viewGroup);
      int lastChildIndex = viewGroup.getChildCount() - 1;
      for (int index = 0; index <= lastChildIndex; index++) {
        if (index == lastNonSkippedChildIndex) {
          lastChildMask = lastChildMask | (1 << depth);
        }
        View child = viewGroup.getChildAt(index);
        if (!skipChild(child)) {
          scanRecursively(result, depth + 1, lastChildMask, child);
        }
      }
    }
  }

  private int findLastNonSkippedChildIndex(ViewGroup viewGroup) {
    int lastChildIndex = viewGroup.getChildCount() - 1;
    for (int index = lastChildIndex; index >= 0; index--) {
      View child = viewGroup.getChildAt(index);
      if (!skipChild(child)) {
        return index;
      }
    }
    return -1;
  }

  private boolean skipChild(View child) {
    int childId = child.getId();
    return childId != NO_ID && binarySearch(skippedIds, childId) >= 0;
  }

  private static void appendLinePrefix(StringBuilder result, int depth, long lastChildMask) {
    int lastDepth = depth - 1;
    // Add a non-breaking space at the beginning of the line because Logcat eats normal spaces.
    result.append('\u00a0');
    for (int parentDepth = 0; parentDepth <= lastDepth; parentDepth++) {
      if (parentDepth > 0) {
        result.append(' ');
      }
      boolean lastChild = (lastChildMask & (1 << parentDepth)) != 0;
      if (lastChild) {
        if (parentDepth == lastDepth) {
          result.append('`');
        } else {
          result.append(' ');
        }
      } else {
        if (parentDepth == lastDepth) {
          result.append('+');
        } else {
          result.append('|');
        }
      }
    }
    if (depth > 0) {
      result.append("-");
    }
  }

  @TargetApi(CUPCAKE)
  private void viewToString(StringBuilder result, View view) {
    if (view == null) {
      result.append("null");
      return;
    }

    result.append(view.getClass().getSimpleName());

    boolean hasDiagnosis = false;
    for (Diagnostic diagnostic : diagnostics) {
      List<Diagnosis> diagnoses = diagnostic.diagnose(view);
      if (diagnoses != null) {
        for (Diagnosis diagnosis : diagnoses) {
          // Add comma.
          if (hasDiagnosis) {
            result.append(", ");
          } else {
            result.append(" { ");
            hasDiagnosis = true;
          }

          // Add severity.
          if (diagnosis.severity !=  INFO) {
            result.append(diagnosis.severity.toString() + ":");
          }
          result.append(diagnosis.message);
        }
      }
    }

    if (hasDiagnosis) {
      result.append(" }");
    }
  }
}
