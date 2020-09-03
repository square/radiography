package radiography.internal

import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.semantics.SemanticsProperties
import radiography.ScannableView.ComposeView
import radiography.ExperimentalRadiographyComposeApi

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
