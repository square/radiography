package radiography

import radiography.ViewStateRenderers.viewStateRendererFor

/**
 * Renders extra attributes for specifics types in the output of [Radiography.scan].
 * Call [viewStateRendererFor] to create instances with reified types:
 * ```
 *  val myRenderer: StateRenderer<MyView> = viewStateRendererFor {
 *    append(it.customAttributeValue)
 *  }
 * ```
 */
public fun interface ViewStateRenderer {
  public fun AttributeAppendable.render(rendered: Any)
}

/**
 * Base class for implementations of [ViewStateRenderer] that only want to render instances of a
 * specific type. Instances of other types are ignored.
 */
internal abstract class TypedViewStateRenderer<in T : Any>(
  private val renderClass: Class<T>
) : ViewStateRenderer {
  public abstract fun AttributeAppendable.renderTyped(rendered: T)

  final override fun AttributeAppendable.render(rendered: Any) {
    if (renderClass.isInstance(rendered)) {
      @Suppress("UNCHECKED_CAST")
      renderTyped(rendered as T)
    }
  }
}
