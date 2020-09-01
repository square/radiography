package radiography

import android.view.View
import radiography.ScannableView.AndroidView
import radiography.ScannableView.ComposeView
import radiography.compose.ExperimentalRadiographyComposeApi
import radiography.compose.composeRenderingError
import radiography.compose.findTestTags
import radiography.compose.isComposeAvailable

object ScanScopes {

  @JvmField
  internal val EmptyScope: ScanScope = ScanScope { emptyList() }

  /** Scans all the windows owned by the app. */
  @JvmField
  val AllWindowsScope: ScanScope = ScanScope {
    WindowScanner.findAllRootViews()
        .map(::AndroidView)
  }

  /** Scans the window that currently has focus. */
  @JvmField
  val FocusedWindowScope: ScanScope = ScanScope {
    AllWindowsScope.findRoots()
        .filterIsInstance<AndroidView>()
        .filter { it.view.parent?.parent != null || it.view.hasWindowFocus() }
  }

  /** Scans the given [rootView]. */
  @JvmStatic
  fun singleViewScope(rootView: View): ScanScope = ScanScope {
    listOf(AndroidView(rootView))
  }

  /**
   * Scans all Composables in all windows that have the `Modifier.testTag` modifier with the given
   * [testTag].
   */
  @ExperimentalRadiographyComposeApi
  @JvmStatic
  @JvmOverloads
  fun composeTestTagScope(
    testTag: String,
    inScope: ScanScope = AllWindowsScope
  ): ScanScope {
    if (!isComposeAvailable) return EmptyScope

    fun ScannableView.subtreesMatching(
      predicate: (ScannableView) -> Boolean
    ): Sequence<ScannableView> {
      if (predicate(this)) return sequenceOf(this)
      return children.flatMap { it.subtreesMatching(predicate) }
    }

    return ScanScope {
      try {
        return@ScanScope inScope.findRoots()
            .asSequence()
            .flatMap { rootView ->
              rootView.subtreesMatching { (it is ComposeView) && testTag in it.findTestTags() }
            }
            .toList()
      } catch (e: LinkageError) {
        // Some version of Compose is available, but the Compose code on the classpath is
        // not what we expected – the app is probably using a newer (or older) version of Compose
        // than we support.
        return@ScanScope listOf(composeRenderingError(e))
      }
    }
  }
}
