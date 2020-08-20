package radiography.compose

import android.util.SparseArray
import android.view.View
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.ui.tooling.Group
import androidx.ui.tooling.asTree
import radiography.AttributeAppendable
import radiography.TreeRenderingVisitor
import radiography.TreeRenderingVisitor.RenderingScope
import radiography.ViewFilter
import radiography.ViewStateRenderer

/**
 * TODO write documentation
 */
internal abstract class ComposeViewRenderer : ViewStateRenderer {

  fun visitComposeView(
    renderingScope: RenderingScope,
    maybeComposeView: View,
    viewFilter: ViewFilter,
    classicViewVisitor: TreeRenderingVisitor<View>
  ) {
    if (!maybeComposeView.isComposeView) return
    // If the class is an AndroidComposeView, then Compose is on the classpath, so we can reference
    // Compose definitions.

    val keyedTags = maybeComposeView.getKeyedTags()
    val composition = keyedTags.first { it is Composition } as Composition? ?: return
    val composer = composition.unwrap()
        .getComposerOrNull() ?: return
    val rootGroup = composer.slotTable
        .asTree()
    renderingScope.visitGroup(rootGroup, viewFilter, classicViewVisitor)
  }

  final override fun AttributeAppendable.render(rendered: Any) {
    // View will be rendered by ViewRenderer, so this is a no-op.
  }

  protected abstract fun RenderingScope.visitGroup(
    group: Group,
    viewFilter: ViewFilter,
    classicViewVisitor: TreeRenderingVisitor<View>
  )

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

  private companion object {
    private val VIEW_KEYED_TAGS_FIELD = View::class.java.getDeclaredField("mKeyedTags")
        .apply { isAccessible = true }
    private const val WRAPPED_COMPOSITION_CLASS_NAME =
      "androidx.compose.ui.platform.WrappedComposition"
    private const val COMPOSITION_IMPL_CLASS_NAME = "androidx.compose.runtime.CompositionImpl"
    private const val GROUP_CLASS_NAME = "androidx.ui.tooling.Group"
  }
}
