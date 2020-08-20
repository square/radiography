package radiography.compose

import android.view.View

private const val ANDROID_COMPOSE_VIEW_CLASS_NAME =
  "androidx.compose.ui.platform.AndroidComposeView"

///**
// * Tries to determine if Compose is on the classpath by reflectively loading a few key classes.
// */
//internal val isComposeAvailable: Boolean by lazy(PUBLICATION) {
//  try {
//    Class.forName(GROUP_CLASS_NAME)
//    Class.forName(COMPOSITION_IMPL_CLASS_NAME)
//    true
//  } catch (e: ClassNotFoundException) {
//    false
//  }
//}

/**
 * True if this view is an `AndroidComposeView`, which is the private view type that is used to host
 * all UI compositions in classic Android views.
 */
internal val View.isComposeView: Boolean
  get() = this::class.java.name == ANDROID_COMPOSE_VIEW_CLASS_NAME
