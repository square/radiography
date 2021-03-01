package radiography.internal

import androidx.compose.runtime.Composer
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.tooling.data.Group
import androidx.compose.ui.tooling.data.UiToolingDataApi
import kotlin.LazyThreadSafetyMode.PUBLICATION

private val REFLECTION_CONSTANTS by lazy(PUBLICATION) {
  try {
    object {
      val CompositionContextHolderClass =
        Class.forName("androidx.compose.runtime.ComposerImpl\$CompositionContextHolder")
      val CompositionContextImplClass =
        Class.forName("androidx.compose.runtime.ComposerImpl\$CompositionContextImpl")
      val CompositionContextHolderRefField =
        CompositionContextHolderClass.getDeclaredField("ref")
          .apply { isAccessible = true }
      val CompositionContextImplComposersField =
        CompositionContextImplClass.getDeclaredField("composers")
          .apply { isAccessible = true }
    }
  } catch (e: Throwable) {
    null
  }
}

@OptIn(UiToolingDataApi::class)
internal fun Group.getCompositionContexts(): Sequence<CompositionContext> {
  return REFLECTION_CONSTANTS?.run {
    data.asSequence()
      .filter { it != null && it::class.java == CompositionContextHolderClass }
      .mapNotNull { holder -> holder.tryGetCompositionContext() }
  } ?: emptySequence()
}

@Suppress("UNCHECKED_CAST")
internal fun CompositionContext.tryGetComposers(): Iterable<Composer> {
  return REFLECTION_CONSTANTS?.let {
    if (!it.CompositionContextImplClass.isInstance(this)) return emptyList()
    it.CompositionContextImplComposersField.get(this) as? Iterable<Composer>
  } ?: emptyList()
}

private fun Any?.tryGetCompositionContext() = REFLECTION_CONSTANTS?.let {
  it.CompositionContextHolderRefField.get(this) as? CompositionContext
}
