package com.squareup.radiography;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Checkable;
import android.widget.TextView;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import static android.os.Build.VERSION_CODES.CUPCAKE;
import static android.view.View.NO_ID;
import static java.util.Arrays.binarySearch;

/**
 * Utility class to scan through a view hierarchy and pretty print it to a {@link String} or
 * {@link StringBuilder}.
 */
public final class Xrays {

  private final boolean showTextFieldContent;
  private final int textFieldMaxLength;
  private final int[] skippedIds;

  private Xrays(@NotNull Builder builder) {
    this.showTextFieldContent = builder.showTextFieldContent;
    this.textFieldMaxLength = builder.textFieldMaxLength;
    this.skippedIds = builder.skippedIds == null ? new int[0] : builder.skippedIds;
  }

  public static class Builder {
    private boolean showTextFieldContent;
    private int textFieldMaxLength = Integer.MAX_VALUE;
    private int[] skippedIds;

    public Builder showTextFieldContent(boolean showTextFieldContent) {
      this.showTextFieldContent = showTextFieldContent;
      return this;
    }

    public Builder textFieldMaxLength(int textFieldMaxLength) {
      if (textFieldMaxLength <= 0) {
        throw new IllegalArgumentException("textFieldMaxLength=" + textFieldMaxLength + " <= 0");
      }
      this.textFieldMaxLength = textFieldMaxLength;
      return this;
    }

    /**
     * @param skippedIds View ids that should be ignored. Can be useful if you want to ignore some
     * debug views.
     */
    public Builder skippedIds(int... skippedIds) {
      this.skippedIds = skippedIds == null ? null : skippedIds.clone();
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
  @NotNull public String scanAllWindows() {
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
  @NotNull public String scanFromRoot(@NotNull View view) {
    return scan(view.getRootView());
  }

  /**
   * Goes up to the root parent of the given view before printing the view hierarchy.
   *
   * @see #scan(StringBuilder, View)
   */
  public void scanFromRoot(StringBuilder result, @NotNull View view) {
    scan(result, view.getRootView());
  }

  /** @see #scan(StringBuilder, View) */
  @NotNull public String scan(View view) {
    StringBuilder result = new StringBuilder();
    scan(result, view);
    return result.toString();
  }

  /**
   * @param result A container in which the view hierachy is pretty printed by appending to the
   * {@link StringBuilder}.
   * @param view the parent view that gets its hierarchy pretty printed.
   */
  public void scan(@NotNull StringBuilder result, View view) {
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

  private int findLastNonSkippedChildIndex(@NotNull ViewGroup viewGroup) {
    int lastChildIndex = viewGroup.getChildCount() - 1;
    for (int index = lastChildIndex; index >= 0; index--) {
      View child = viewGroup.getChildAt(index);
      if (!skipChild(child)) {
        return index;
      }
    }
    return -1;
  }

  private boolean skipChild(@NotNull View child) {
    int childId = child.getId();
    return childId != NO_ID && binarySearch(skippedIds, childId) >= 0;
  }

  private static void appendLinePrefix(@NotNull StringBuilder result, int depth,
      long lastChildMask) {
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

    result.append(view.getClass().getSimpleName()).append(" { ");
    if (view.getId() != -1 && view.getResources() != null) {
      try {
        String resourceName = view.getResources().getResourceEntryName(view.getId());
        result.append("id:").append(resourceName).append(", ");
      } catch (Resources.NotFoundException ignore) {
        // Do nothing.
      }
    }

    switch (view.getVisibility()) {
      case View.GONE:
        result.append("GONE, ");
        break;
      case View.INVISIBLE:
        result.append("INVISIBLE, ");
        break;
    }

    result.append(view.getWidth()).append("x").append(view.getHeight()).append("px");

    if (view.isFocused()) {
      result.append(", focused");
    }

    if (!view.isEnabled()) {
      result.append(", disabled");
    }

    if (view.isSelected()) {
      result.append(", selected");
    }

    if (view instanceof TextView) {
      TextView textView = (TextView) view;
      CharSequence text = textView.getText();
      if (text != null) {
        result.append(", text-length:").append(text.length());
        if (showTextFieldContent) {
          if (text.length() > textFieldMaxLength) {
            text = text.subSequence(0, textFieldMaxLength - 1) + "…";
          }
          result.append(", text:\"").append(text).append("\"");
        }
      }
      if (textView.isInputMethodTarget()) {
        result.append(", ime-target");
      }
    }
    if (view instanceof Checkable) {
      Checkable checkable = (Checkable) view;
      if (checkable.isChecked()) {
        result.append(", checked");
      }
    }
    result.append(" }");
  }
}
