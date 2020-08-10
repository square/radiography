package radiography

import android.os.Build.VERSION
import android.view.View
import java.lang.reflect.Field

/**
 * Looks for all windows in the current process and finds the associated root view for each window.
 * Based on Expresso RootsOracle.
 */
object WindowScanner {

  private const val WINDOW_MANAGER_GLOBAL_CLASS = "android.view.WindowManagerGlobal"
  private const val VIEWS_FIELD = "mViews"
  private const val GET_GLOBAL_INSTANCE = "getInstance"

  private var initialized = false
  private var windowManager: Any? = null
  private var viewsField: Field? = null

  @Deprecated("Use WindowScanner directly from Kotlin.")
  @JvmStatic val instance: WindowScanner = this

  /**
   * Looks for all Root views
   */
  @Synchronized fun findAllRootViews(): List<View?> {
    if (!initialized) {
      initialize()
    }
    return if (windowManager == null || viewsField == null) {
      emptyList()
    } else try {
      @Suppress("UNCHECKED_CAST")
      if (VERSION.SDK_INT < 19) {
        (viewsField!![windowManager] as Array<View?>).toList()
      } else {
        (viewsField!![windowManager] as List<View?>).toList()
      }
    } catch (ignored: RuntimeException) {
      emptyList<View>()
    } catch (ignored: IllegalAccessException) {
      emptyList<View>()
    }
  }

  private fun initialize() {
    initialized = true
    val accessClass: String = WINDOW_MANAGER_GLOBAL_CLASS
    val instanceMethod: String = GET_GLOBAL_INSTANCE
    try {
      val clazz = Class.forName(accessClass)
      val getMethod = clazz.getMethod(instanceMethod)
      windowManager = getMethod.invoke(null)
      viewsField = clazz.getDeclaredField(VIEWS_FIELD)
          .apply {
            isAccessible = true
          }
    } catch (ignored: Exception) {
    }
  }
}
