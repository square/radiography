package radiography.test

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.view.Window.Callback
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import radiography.Radiography
import radiography.ScanScopes.FocusedWindowScope
import radiography.ViewStateRenderers.DefaultsIncludingPii
import radiography.test.utilities.TestActivity
import radiography.test.utilities.TestActivity.Companion.withTextViewText
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

class RadiographyUiTest {

  @get:Rule
  val activityRule = ActivityTestRule(TestActivity::class.java, false, false)

  @Test fun when_noActivity_then_emptyHierarchy() {
    val hierarchy = Radiography.scan()

    assertThat(hierarchy).isEmpty()
  }

  @Test fun when_launchedActivity_then_hierarchyContainsActivity() {
    activityRule.launchActivity(Intent())

    val hierarchy = Radiography.scan()

    assertThat(hierarchy).contains(TestActivity::class.java.name)
  }

  @Test fun when_includingPii_then_hierarchyContainsText() {
    activityRule.launchActivity(Intent().withTextViewText("Yo"))

    val hierarchy = Radiography.scan(viewStateRenderers = DefaultsIncludingPii)
    assertThat(hierarchy).contains("Yo")
  }

  @Test fun when_showDialog_then_hierarchyHasTwoWindows() {
    activityRule.launchActivity(Intent())

    showDialog {
      AlertDialog.Builder(activityRule.activity)
          .create()
    }

    val hierarchy = Radiography.scan()

    assertThat(hierarchy.countSubstring("window-focus")).isEqualTo(2)
  }

  @Test fun when_onlyFocusedWindow_then_hierarchyHasOnlyDialog() {
    activityRule.launchActivity(Intent())

    showDialog {
      AlertDialog.Builder(activityRule.activity)
          .setTitle("Dialog title")
          .create()
    }

    val hierarchy = Radiography.scan(
        scanScope = FocusedWindowScope,
        viewStateRenderers = DefaultsIncludingPii
    )

    assertThat(hierarchy).contains("window-focus:true")
    assertThat(hierarchy).contains("Dialog title")

    assertThat(hierarchy.countSubstring("window-focus")).isEqualTo(1)
  }

  private fun String.countSubstring(substring: String) = windowed(substring.length)
      .filter { it == substring }
      .count()

  private fun showDialog(block: () -> Dialog) {
    lateinit var dialog: Dialog
    getInstrumentation().runOnMainSync {
      dialog = block()
      dialog.show()
    }
    dialog.waitForFocus()
  }

  /**
   * Waits for the activity to lose focus and the dialog to gain focus.
   */
  private fun Dialog.waitForFocus() {
    val dialogFocused = CountDownLatch(2)
    getInstrumentation().runOnMainSync {
      val activityHasWindowFocus = activityRule.activity.hasWindowFocus()
      val dialogHasWindowFocus = window!!.peekDecorView()?.hasWindowFocus() ?: false

      if (!activityHasWindowFocus) {
        dialogFocused.countDown()
      } else {
        val activityWindow = activityRule.activity.window
        val delegateCallback = activityWindow.callback
        activityWindow.callback = object : Callback by delegateCallback {
          override fun onWindowFocusChanged(hasFocus: Boolean) {
            delegateCallback.onWindowFocusChanged(hasFocus)
            if (!hasFocus) {
              dialogFocused.countDown()
            }
          }
        }
      }

      if (dialogHasWindowFocus) {
        dialogFocused.countDown()
      } else {
        val delegateCallback = window!!.callback
        window!!.callback = object : Callback by delegateCallback {
          override fun onWindowFocusChanged(hasFocus: Boolean) {
            delegateCallback.onWindowFocusChanged(hasFocus)
            if (hasFocus) {
              dialogFocused.countDown()
            }
          }
        }
      }
    }
    assertThat(dialogFocused.await(10, SECONDS)).isTrue()
  }
}
