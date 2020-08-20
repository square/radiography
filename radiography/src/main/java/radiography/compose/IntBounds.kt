package radiography.compose

import androidx.compose.ui.unit.IntBounds
import radiography.formatPixelDimensions

/**
 * TODO write documentation
 */
internal fun IntBounds.describeSize(): String {
  return if (left != right || top != bottom) {
    formatPixelDimensions(width = right - left, height = bottom - top)
  } else {
    ""
  }
}
