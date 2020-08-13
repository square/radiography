package radiography

/**
 * Renders extra attributes for specifics types in the output of [Radiography.scan].
 * Call [viewStateRendererFor] to create instances with reified types:
 * ```
 *  val myRenderer: StateRenderer<MyView> = viewStateRendererFor {
 *    append(it.customAttributeValue)
 *  }
 * ```
 */
class ViewStateRenderer<in T> @PublishedApi internal constructor(
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
}

inline fun <reified T> viewStateRendererFor(noinline renderer: AttributeAppendable.(T) -> Unit) =
  ViewStateRenderer(T::class.java, renderer)
