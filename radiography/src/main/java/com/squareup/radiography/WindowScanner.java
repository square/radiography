package com.squareup.radiography;

import android.os.Build;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Looks for all windows in the current process and finds the associated root view for each window.
 * Based on Expresso RootsOracle.
 */
public class WindowScanner {

  private static final String WINDOW_MANAGER_IMPL_CLASS = "android.view.WindowManagerImpl";
  private static final String WINDOW_MANAGER_GLOBAL_CLASS = "android.view.WindowManagerGlobal";
  private static final String VIEWS_FIELD = "mViews";
  private static final String GET_DEFAULT_IMPL = "getDefault";
  private static final String GET_GLOBAL_INSTANCE = "getInstance";

  private static final WindowScanner INSTANCE = new WindowScanner();

  public static WindowScanner getInstance() {
    return INSTANCE;
  }

  private boolean initialized;
  private Object windowManager;
  private Field viewsField;

  /**
   * Looks for all Root views
   */
  public synchronized List<View> findAllRootViews() {
    if (!initialized) {
      initialize();
    }

    if (windowManager == null || viewsField == null) {
      return Collections.emptyList();
    }

    try {
      if (Build.VERSION.SDK_INT < 19) {
        return Arrays.asList((View[]) viewsField.get(windowManager));
      } else {
        //noinspection unchecked
        return new ArrayList<View>((List<View>) viewsField.get(windowManager));
      }
    } catch (RuntimeException ignored) {
      return Collections.emptyList();
    } catch (IllegalAccessException ignored) {
      return Collections.emptyList();
    }
  }

  private void initialize() {
    initialized = true;
    String accessClass;
    String instanceMethod;
    if (Build.VERSION.SDK_INT > 16) {
      accessClass = WINDOW_MANAGER_GLOBAL_CLASS;
      instanceMethod = GET_GLOBAL_INSTANCE;
    } else {
      accessClass = WINDOW_MANAGER_IMPL_CLASS;
      instanceMethod = GET_DEFAULT_IMPL;
    }

    try {
      Class<?> clazz = Class.forName(accessClass);
      Method getMethod = clazz.getMethod(instanceMethod);
      windowManager = getMethod.invoke(null);
      viewsField = clazz.getDeclaredField(VIEWS_FIELD);
      viewsField.setAccessible(true);
    } catch (Exception ignored) {
    }
  }
}
