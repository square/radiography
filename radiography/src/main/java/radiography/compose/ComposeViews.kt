package radiography.compose

import android.util.SparseArray
import android.view.View
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.ui.tooling.asTree
import radiography.TreeRenderingVisitor
import radiography.TreeRenderingVisitor.RenderingScope
import radiography.ViewFilter
import radiography.ViewStateRenderer
import kotlin.LazyThreadSafetyMode.PUBLICATION

private val VIEW_KEYED_TAGS_FIELD = View::class.java.getDeclaredField("mKeyedTags")
    .apply { isAccessible = true }
private const val WRAPPED_COMPOSITION_CLASS_NAME = "androidx.compose.ui.platform.WrappedComposition"
private const val COMPOSITION_IMPL_CLASS_NAME = "androidx.compose.runtime.CompositionImpl"
private const val ANDROID_COMPOSE_VIEW_CLASS_NAME =
  "androidx.compose.ui.platform.AndroidComposeView"

internal const val COMPOSE_UNSUPPORTED_MESSAGE =
  "Composition was found, but either Compose Tooling artifact is missing or the Compose version " +
      "is not supported. Please ensure you have a dependency on androidx.ui:ui-tooling or check " +
      "https://github.com/square/radiography for a new release."

/** Reflectively tries to determine if Compose is on the classpath. */
internal val isComposeAvailable by lazy(PUBLICATION) {
  try {
    Class.forName(ANDROID_COMPOSE_VIEW_CLASS_NAME)
    true
  } catch (e: Throwable) {
    false
  }
}

/**
 * True if this view looks like the private view type that Compose uses to host compositions.
 * It does a fuzzy match to try to detect unsupported Compose versions, which will not be rendered
 * but will at least warn that the version is unsupported.
 */
internal val View.mightBeComposeView: Boolean
  get() = "AndroidComposeView" in this::class.java.name

/**
 * Uses [renderingScope] to visit and render an entire composition. This is the entry point into
 * compose support from [radiography.ViewTreeRenderingVisitor]. This function is a no-op if
 * [maybeComposeView] is not an instance of the special private View that Compose uses to host
 * compositions. The view's [Composer]'s `SlotTable` is parsed using the Compose Tooling library's
 * [asTree] function, and then further distilled into [ComposeLayoutInfo]s.
 *
 * @param modifierRenderers A list of [ViewStateRenderer]s which will be passed
 * [Modifier][androidx.compose.ui.Modifier] instances to render.
 * @param viewFilter A [ViewFilter] which will be passed [ComposeLayoutInfo]s to filter.
 * @param classicViewVisitor The [TreeRenderingVisitor] that will be used to render any Android
 * views emitted by the composition.
 * @return True if the Compose view was visited successfully, false if the runtime version of
 * Compose is not supported by this library and we couldn't render the Compose view.
 * @see LayoutInfoVisitor
 */
internal fun tryVisitComposeView(
  renderingScope: RenderingScope,
  maybeComposeView: View,
  modifierRenderers: List<ViewStateRenderer>,
  viewFilter: ViewFilter,
  classicViewVisitor: TreeRenderingVisitor<View>
): Boolean {
  if (!maybeComposeView.mightBeComposeView) return false

  var linkageError: String? = null
  val visited = try {
    visitComposeView(
        renderingScope, maybeComposeView, modifierRenderers, viewFilter, classicViewVisitor
    )
  } catch (e: LinkageError) {
    // The view looks like an AndroidComposeView, but the Compose code on the classpath is
    // not what we expected – the app is probably using a newer (or older) version of Compose than
    // we support.
    linkageError = e.message
    false
  }

  if (!visited) {
    // Compose version is unsupported, include a warning but then continue rendering Android
    // views.
    renderingScope.description.append("\n$COMPOSE_UNSUPPORTED_MESSAGE")
    linkageError?.let {
      renderingScope.description.append("\nError: $linkageError")
    }
  }

  return visited
}

/**
 * Uses reflection to try to pull a `SlotTable` out of [composeView] and render it. If any of the
 * reflection fails, returns false.
 */
private fun visitComposeView(
  renderingScope: RenderingScope,
  composeView: View,
  modifierRenderers: List<ViewStateRenderer>,
  viewFilter: ViewFilter,
  classicViewVisitor: TreeRenderingVisitor<View>
): Boolean {
  val keyedTags = composeView.getKeyedTags()
  val composition = keyedTags.first { it is Composition } as Composition? ?: return false
  val composer = composition.unwrap()
      .getComposerOrNull() ?: return false
  val rootGroup = composer.slotTable.asTree()
  val visitor = LayoutInfoVisitor(modifierRenderers, viewFilter, classicViewVisitor)

  rootGroup.layoutInfos.forEach {
    renderingScope.addChildToVisit(it, visitor)
  }
  // At this point we will not have actually visited any LayoutInfos (addChildToVisit doesn't
  // immediately visit), but if we were able to successfully construct the LayoutInfos, then we
  // assume the Compose version is supported. LayoutInfoVisitor will also try to detect unsupported
  // Compose versions.
  return true
}

private fun View.getKeyedTags(): SparseArray<*> {
  return VIEW_KEYED_TAGS_FIELD.get(this) as SparseArray<*>
}

private inline fun SparseArray<*>.first(predicate: (Any?) -> Boolean): Any? {
  for (i in 0 until size()) {
    val item = valueAt(i)
    if (predicate(item)) return item
  }
  return null
}

/**
 * If this is a `WrappedComposition`, returns the original composition, else returns this.
 */
private fun Composition.unwrap(): Composition {
  if (this::class.java.name != WRAPPED_COMPOSITION_CLASS_NAME) return this
  val wrappedClass = Class.forName(WRAPPED_COMPOSITION_CLASS_NAME)
  val originalField = wrappedClass.getDeclaredField("original")
      .apply { isAccessible = true }
  return originalField.get(this) as Composition
}

/**
 * Tries to pull a [Composer] out of this [Composition], or returns null if it can't find one.
 */
private fun Composition.getComposerOrNull(): Composer<*>? {
  if (this::class.java.name != COMPOSITION_IMPL_CLASS_NAME) return null
  val compositionImplClass = Class.forName(COMPOSITION_IMPL_CLASS_NAME)
  val composerField = compositionImplClass.getDeclaredField("composer")
      .apply { isAccessible = true }
  return composerField.get(this) as? Composer<*>
}
