package radiography

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import radiography.Xrays.Builder
import org.fest.assertions.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class XraysTest {
  @Suppress("DEPRECATION")
  private val context: Context = RuntimeEnvironment.application
  private val xrays: Xrays = Xrays.create()

  @Test fun viewDetailsReported() {
    val view = View(context)
        .apply {
          visibility = View.INVISIBLE
          right = 30
          bottom = 30
          isEnabled = false
          isSelected = true
        }
    assertThat(xrays.scan(view))
        .contains("INVISIBLE")
        .contains("30x30px")
        .contains("disabled")
        .contains("selected")
  }

  @Test fun nullView() {
    assertThat(xrays.scan(null))
        .contains("null")
  }

  @Test fun checkableChecked() {
    val view = CheckBox(context)
    view.isChecked = true
    assertThat(xrays.scan(view))
        .contains("checked")
  }

  @Test fun textView() {
    val view = TextView(context)
    view.text = "Baguette"
    val scan = xrays.scan(view)
    assertThat(scan)
        .contains("text-length:8")
        .doesNotContain("text:")
  }

  @Test fun textViewContents() {
    val view = TextView(context)
    view.text = "Baguette Avec Fromage"
    val xrays = Builder().showTextFieldContent(true)
        .build()
    val scan = xrays.scan(view)
    assertThat(scan)
        .contains("text-length:21")
        .contains("text:\"Baguette Avec Fromage\"")
  }

  @Test fun textViewContentsEllipsized() {
    val view = TextView(context)
    view.text = "Baguette Avec Fromage"
    val xrays = Builder().showTextFieldContent(true)
        .textFieldMaxLength(11)
        .build()
    val scan = xrays.scan(view)
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
    assertThat(xrays.scan(layout))
        .contains("FrameLayout")
        .contains("Leave me alone")
  }

  @Test fun skipIds() {
    val layout = FrameLayout(context)
    val view = Button(context)
    view.id = 42
    layout.addView(view)
    assertThat(
        Xrays.withSkippedIds(42)
            .scan(layout)
    )
        .contains("FrameLayout")
        .doesNotContain("Button")
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
    assertThat(xrays.scan(root)).contains(
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
