@file:JvmName("SlotTables")

package radiography.compose

import androidx.compose.runtime.SlotTable
import androidx.ui.tooling.asTree
import radiography.Radiography.renderScannableViewTree
import radiography.ScannableView.ComposeView
import radiography.ViewFilter
import radiography.ViewFilters
import radiography.ViewStateRenderer
import radiography.ViewStateRenderers

/**
 * Scans a particular [SlotTable], ie. an entire Compose tree. You probably want to use
 * [Radiography.scan][radiography.Radiography.scan] instead.
 */
@ExperimentalRadiographyComposeApi
@JvmSynthetic
fun SlotTable.scan(
  viewStateRenderers: List<ViewStateRenderer> = ViewStateRenderers.DefaultsNoPii,
  viewFilter: ViewFilter = ViewFilters.NoFilter
): String = buildString {
  val rootGroup = asTree()
  rootGroup.layoutInfos
      .map(::ComposeView)
      .forEach {
        if (length > 0) {
          appendln()
        }
        renderScannableViewTree(this, it, viewStateRenderers, viewFilter)
      }
}
