@file:JvmName("SlotTables")

package radiography.compose

import androidx.compose.runtime.SlotTable
import androidx.ui.tooling.asTree
import radiography.ViewFilter
import radiography.ViewFilters
import radiography.ViewStateRenderer
import radiography.ViewStateRenderers
import radiography.ViewTreeRenderingVisitor
import radiography.renderTreeString

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
  val viewVisitor = ViewTreeRenderingVisitor(viewStateRenderers, viewFilter)
  val visitor = LayoutInfoVisitor(viewStateRenderers, viewFilter, viewVisitor)

  rootGroup.layoutInfos.forEach {
    renderTreeString(it, visitor)
  }
}
