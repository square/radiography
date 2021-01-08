package radiography.internal

import androidx.compose.runtime.Composer
import androidx.compose.runtime.CompositionReference
import androidx.compose.ui.tooling.Group
import kotlin.LazyThreadSafetyMode.PUBLICATION

private val REFLECTION_CONSTANTS by lazy(PUBLICATION) {
  try {
    object {
      val compositionReferenceHolderClass =
        Class.forName("androidx.compose.runtime.Composer\$CompositionReferenceHolder")
      val compositionReferenceImplClass =
        Class.forName("androidx.compose.runtime.Composer\$CompositionReferenceImpl")
      val compositionReferenceHolderRefField =
        compositionReferenceHolderClass.getDeclaredField("ref")
          .apply { isAccessible = true }
      val compositionReferenceImplComposersField =
        compositionReferenceImplClass.getDeclaredField("composers")
          .apply { isAccessible = true }
    }
  } catch (e: Throwable) {
    null
  }
}

internal fun Group.getCompositionReferences(): Sequence<CompositionReference> {
  return REFLECTION_CONSTANTS?.run {
    data.asSequence()
      .filter { it != null && it::class.java == compositionReferenceHolderClass }
      .mapNotNull { holder -> holder.tryGetCompositionReference() }
  } ?: emptySequence()
}

@Suppress("UNCHECKED_CAST")
internal fun CompositionReference.tryGetComposers(): Iterable<Composer<*>> {
  return REFLECTION_CONSTANTS?.let {
    if (!it.compositionReferenceImplClass.isInstance(this)) return emptyList()
    it.compositionReferenceImplComposersField.get(this) as? Iterable<Composer<*>>
  } ?: emptyList()
}

private fun Any?.tryGetCompositionReference() = REFLECTION_CONSTANTS?.let {
  it.compositionReferenceHolderRefField.get(this) as? CompositionReference
}
