package radiography

import android.view.View

/**
 * Extension function for [Radiography.scan] when scanning starts from a specific view.
 * @see Radiography.scan
 */
@JvmSynthetic
fun View?.scan(
  includeTextViewText: Boolean = false,
  textViewTextMaxLength: Int = Int.MAX_VALUE,
  viewFilter: ViewFilter = ViewFilter.All
): String {
  return if (this == null) {
    "null"
  } else {
    Radiography.scan(this, includeTextViewText, textViewTextMaxLength, viewFilter)
  }
}
