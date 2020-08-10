package radiography

import radiography.StateRenderer.Companion.stateRendererFor

/**
 * Renders extra attributes for specifics types in the output of [Radiography.scan].
 * Call [stateRendererFor] to create instances with reified types:
 * ```
 *  val myRenderer: StateRenderer<MyView> = stateRendererFor {
 *    append(it.customAttributeValue)
 *  }
 * ```
 */
class StateRenderer<in T> @PublishedApi internal constructor(
  private val renderedClass: Class<T>,
  private val renderer: AttributeAppendable.(T) -> Unit
) {

  fun appendAttributes(
    appendable: AttributeAppendable,
    rendered: Any
  ) {
    if (renderedClass.isInstance(rendered)) {
      with(appendable) {
        @Suppress("UNCHECKED_CAST")
        renderer(rendered as T)
      }
    }
  }

  companion object {
    inline fun <reified T> stateRendererFor(noinline renderer: AttributeAppendable.(T) -> Unit) =
      StateRenderer(T::class.java, renderer)
  }
}
