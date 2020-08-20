package radiography.compose

import androidx.compose.runtime.Ambient
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.internal.ComposableLambda
import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.text.AnnotatedString
import radiography.ellipsize

/**
 * TODO write documentation
 */
internal interface RawValueFormatter {
  fun format(
    value: Any?,
    format: (Any?) -> CharSequence?
  ): CharSequence?
}

internal fun Iterable<RawValueFormatter>.format(value: Any?): CharSequence {
  return asSequence()
      .mapNotNull { it.format(value, ::format) }
      .firstOrNull()
      ?: value.toString()
}

// TODO unit tests for all of these
internal object RawValueFormatters {

  fun defaults(
    includeText: Boolean = false,
    maxTextLength: Int = Int.MAX_VALUE
  ): List<RawValueFormatter> = listOf(
      FunctionFormatter,
      ComposableLambdaFormatter,
      StateFormatter,
      MapFormatter,
      SemanticsModifierFormatter,
      StaticValueHolderFormatter,
      charSequenceFormatter(includeText, maxTextLength),
      AnnotatedStringFormatter
  )

  private val FunctionFormatter = object : RawValueFormatter {
    override fun format(
      value: Any?,
      format: (Any?) -> CharSequence?
    ): CharSequence? = (value as? Function<*>)?.let {
      "fun{}@${value.hashCode()}"
    }
  }

  @OptIn(ComposeCompilerApi::class)
  private val ComposableLambdaFormatter = object : RawValueFormatter {
    override fun format(
      value: Any?,
      format: (Any?) -> CharSequence?
    ): CharSequence? =
      (value as? ComposableLambda<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *>)?.let {
        "@Composable fun{}@${value.hashCode()}"
      }
  }

  private val StateFormatter = object : RawValueFormatter {
    override fun format(
      value: Any?,
      format: (Any?) -> CharSequence?
    ): CharSequence? = (value as? State<*>)?.let { state ->
      val stateTypeString = if (state is MutableState<*>) "MutableState" else "State"
      val stateValueString = format(state.value)
      return@let "$stateTypeString($stateValueString)"
    }
  }

  private val MapFormatter = object : RawValueFormatter {
    override fun format(
      value: Any?,
      format: (Any?) -> CharSequence?
    ): CharSequence? = (value as? Map<*, *>)?.let { map ->
      when {
        // If a map is too large, don't try to print it.
        // Unless a sample contains only Ambient keys, in which case the whole thing is probably
        // ambients so print the values only.
        // TODO unit tests for all cases
        map.size > 10 && map.keys.take(10).any { it !is Ambient<*> } -> {
          "Map(size=${map.size})"
        }
        map.keys.all { it is Ambient<*> } -> {
          "ambients: ${map.values.map { format(it) }}"
        }
        else -> {
          "Map { ${map.entries.joinToString { (k, v) -> "$k=${format(v)}" }} }"
        }
      }
    }
  }

  private val SemanticsModifierFormatter = object : RawValueFormatter {
    override fun format(
      value: Any?,
      format: (Any?) -> CharSequence?
    ): CharSequence? = (value as? SemanticsModifier)?.let { modifier ->
      buildString {
        append("semantics-modifier: { ")
        modifier.semanticsConfiguration.joinTo(this) { (key, value) ->
          "${key.name}=${format(value)}"
        }
        append(" }")
      }
    }
  }

  private val StaticValueHolderFormatter = object : RawValueFormatter {
    override fun format(
      value: Any?,
      format: (Any?) -> CharSequence?
    ): CharSequence? {
      // TODO
      return null
    }
  }

  private fun charSequenceFormatter(
    includeText: Boolean,
    maxTextLength: Int
  ) = object : RawValueFormatter {
    override fun format(
      value: Any?,
      format: (Any?) -> CharSequence?
    ): CharSequence? = (value as? CharSequence)?.let {
      if (includeText) {
        "\"${value.ellipsize(maxTextLength)}\""
      } else {
        "text-length:${value.length}"
      }
    }
  }

  private val AnnotatedStringFormatter = object : RawValueFormatter {
    override fun format(
      value: Any?,
      format: (Any?) -> CharSequence?
    ): CharSequence? = (value as? AnnotatedString)?.let {
      value.copy(text = format(value.text).toString())
          .toString()
    }
  }
}
