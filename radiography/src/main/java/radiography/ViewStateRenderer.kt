package radiography

/**
 * Renders extra attributes for specifics types in the output of [Radiography.scan].
 *
 * Call [androidViewStateRendererFor][ViewStateRenderers.androidViewStateRendererFor] to create
 * instances for specific [View][android.view.View] types:
 * ```
 *  val myRenderer: StateRenderer<MyView> = androidViewStateRendererFor {
 *    append(it.customAttributeValue)
 *  }
 * ```
 */
public fun interface ViewStateRenderer {
  public fun AttributeAppendable.render(view: ScannableView)
}
