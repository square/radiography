package radiography.compose

import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.semantics.SemanticsProperties
import radiography.ScannableView.ComposeView

/** Returns all tag strings set on the composable via `Modifier.testTag`. */
@OptIn(ExperimentalRadiographyComposeApi::class)
internal fun ComposeView.findTestTags(): Sequence<String> {
  return modifiers
      .asSequence()
      .filterIsInstance<SemanticsModifier>()
      .flatMap { semantics ->
        semantics.semanticsConfiguration.asSequence()
            .filter { it.key == SemanticsProperties.TestTag }
      }
      .mapNotNull { it.value as? String }
}
