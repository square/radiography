package radiography

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.fest.assertions.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RadiographyTest {
  @Suppress("DEPRECATION")
  private val context: Context = RuntimeEnvironment.application

  @Test fun viewDetailsReported() {
    val view = View(context)
        .apply {
          visibility = View.INVISIBLE
          right = 30
          bottom = 30
          isEnabled = false
          isSelected = true
        }
    assertThat(view.scan())
        .contains("INVISIBLE")
        .contains("30x30px")
        .contains("disabled")
        .contains("selected")
  }

  @Test fun nullView() {
    val view: View? = null
    assertThat(view.scan())
        .contains("null")
  }

  @Test fun checkableChecked() {
    val view = CheckBox(context)
    view.isChecked = true
    assertThat(view.scan())
        .contains("checked")
  }

  @Test fun textView() {
    val view = TextView(context)
    view.text = "Baguette"
    val scan = view.scan()
    assertThat(scan)
        .contains("text-length:8")
        .doesNotContain("text:")
  }

  @Test fun textViewContents() {
    val view = TextView(context)
    view.text = "Baguette Avec Fromage"
    val scan = view.scan(includeTextViewText = true)
    assertThat(scan)
        .contains("text-length:21")
        .contains("text:\"Baguette Avec Fromage\"")
  }

  @Test fun textViewContentsEllipsized() {
    val view = TextView(context)
    view.text = "Baguette Avec Fromage"
    val scan = view.scan(
        includeTextViewText = true,
        textViewTextMaxLength = 11
    )
    assertThat(scan)
        .contains("text-length:21")
        .contains("text:\"Baguette A…\"")
  }

  @Test fun recoversFromException() {
    val layout = FrameLayout(context)
    layout.addView(object : View(context) {
      override fun isEnabled(): Boolean {
        throw UnsupportedOperationException("Leave me alone")
      }
    })
    assertThat(layout.scan())
        .contains("FrameLayout")
        .contains("Leave me alone")
  }

  @Test fun skipIds() {
    val layout = FrameLayout(context)
    val view = Button(context).apply {
      id = 42
    }
    layout.addView(view)
    assertThat(layout.scan(viewFilter = SkipIdsViewFilter(42)))
        .contains("FrameLayout")
        .doesNotContain("Button")
  }

  @Test fun combineFilters() {
    val layout = FrameLayout(context)
    layout.addView(CheckBox(context))
    layout.addView(Button(context).apply {
      id = 42
    })
    layout.addView(EditText(context))

    val filter = SkipIdsViewFilter(42) and object : ViewFilter {
      override fun matches(view: View) = view !is EditText
    }
    assertThat(layout.scan(viewFilter = filter))
        .contains("CheckBox")
        .doesNotContain("Button")
        .doesNotContain("TextView")
  }

  @Test fun nestedViews() {
    val root = FrameLayout(context).apply {
      addView(View(context))
    }
    val childLayout1 = LinearLayout(context)
        .apply {
          addView(View(context))
          addView(View(context))
        }
    root.addView(childLayout1)
    val childLayout2 = LinearLayout(context)
        .apply {
          addView(View(context))
          addView(View(context))
        }
    root.addView(childLayout2)
    assertThat(root.scan()).contains(
        """
          FrameLayout { 0x0px }
          $BLANK+-View { 0x0px }
          $BLANK+-LinearLayout { 0x0px }
          $BLANK| +-View { 0x0px }
          $BLANK| `-View { 0x0px }
          $BLANK`-LinearLayout { 0x0px }
          $BLANK  +-View { 0x0px }
          $BLANK  `-View { 0x0px }
        """.trimIndent()
    )
  }

  companion object {
    private const val BLANK = '\u00a0'
  }
}
