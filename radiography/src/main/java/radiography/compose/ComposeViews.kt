package radiography.compose

import android.util.SparseArray
import android.view.View
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.ui.tooling.CallGroup
import androidx.ui.tooling.Group
import androidx.ui.tooling.asTree
import radiography.TreeRenderingVisitor
import radiography.TreeRenderingVisitor.RenderingScope
import radiography.ViewFilter
import kotlin.LazyThreadSafetyMode.PUBLICATION

private const val ANDROID_COMPOSE_VIEW_CLASS_NAME =
  "androidx.compose.ui.platform.AndroidComposeView"
private const val WRAPPED_COMPOSITION_CLASS_NAME = "androidx.compose.ui.platform.WrappedComposition"
private const val COMPOSITION_IMPL_CLASS_NAME = "androidx.compose.runtime.CompositionImpl"
private const val GROUP_CLASS_NAME = "androidx.ui.tooling.Group"
private val VIEW_KEYED_TAGS_FIELD = View::class.java.getDeclaredField("mKeyedTags")
    .apply { isAccessible = true }

/**
 * Tries to determine if Compose is on the classpath by reflectively loading a few key classes.
 */
internal val isComposeAvailable: Boolean by lazy(PUBLICATION) {
  try {
    Class.forName(GROUP_CLASS_NAME)
    Class.forName(COMPOSITION_IMPL_CLASS_NAME)
    true
  } catch (e: ClassNotFoundException) {
    false
  }
}

/**
 * True if this view is an `AndroidComposeView`, which is the private view type that is used to host
 * all UI compositions in classic Android views.
 */
internal val View.isComposeView: Boolean
  get() = this::class.java.name == ANDROID_COMPOSE_VIEW_CLASS_NAME

internal fun RenderingScope.visitComposeView(
  maybeComposeView: View,
  collapseEmptyNodes: Boolean,
  treeNodeFormatter: ComposeTreeNodeRenderer?,
  viewFilter: ViewFilter,
  viewVisitor: TreeRenderingVisitor<View>
) {
  if (!maybeComposeView.isComposeView) return
  // If the class is an AndroidComposeView, then Compose is on the classpath, so we can reference
  // Compose definitions.

  val keyedTags = maybeComposeView.getKeyedTags()
  val composition = keyedTags.first { it is Composition } as Composition? ?: return
  val composer = composition.unwrap()
      .getComposerOrNull() ?: return
  val tree = composer.slotTable
      .asTree()
      .toComposeTreeNode(collapseEmptyNodes)
  val formatter = treeNodeFormatter ?: ComposeTreeNodeRenderer()
  val visitor = GroupTreeRenderingVisitor(formatter, viewFilter, viewVisitor)
  addChildToVisit(tree, visitor)
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

private val Group.isEmptyCallGroup: Boolean
  get() = (this is CallGroup) && name.isNullOrBlank() && data.isEmpty()
