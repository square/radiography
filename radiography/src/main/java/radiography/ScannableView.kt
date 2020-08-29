package radiography

import android.view.View
import androidx.compose.ui.Modifier
import radiography.ScannableView.AndroidView
import radiography.ScannableView.ComposeView
import radiography.compose.ExperimentalRadiographyComposeApi

/**
 * Represents a logic view that can be rendered as a node in the view tree.
 *
 * Can either be an actual Android [View] ([AndroidView]) or a grouping of Composables that roughly
 * represents the concept of a logical "view" ([ComposeView]).
 */
public sealed class ScannableView {

  public class AndroidView(val view: View) : ScannableView()

  /**
   * Represents a group of Composables that make up a logical "view".
   *
   * @param modifiers The list of [Modifier]s that are currently applied to the Composable.
   */
  @ExperimentalRadiographyComposeApi
  public class ComposeView(val modifiers: List<Modifier>) : ScannableView()
}
