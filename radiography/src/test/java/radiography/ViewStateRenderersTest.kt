package radiography

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import radiography.ScannableView.AndroidView
import radiography.ViewStateRenderers.androidViewStateRendererFor

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ViewStateRenderersTest {
  @Suppress("DEPRECATION")
  private val context: Context = RuntimeEnvironment.application

  @Test fun `androidViewStateRendererFor ignores different view types`() {
    var rendered = false
    val renderer = androidViewStateRendererFor<TextView> {
      rendered = true
    }

    with(renderer) {
      AttributeAppendable(StringBuilder()).render(AndroidView(View(context)))
    }

    assertThat(rendered).isFalse()
  }

  @Test fun `androidViewStateRendererFor accepts view subtypes`() {
    var rendered = false
    val renderer = androidViewStateRendererFor<TextView> {
      rendered = true
    }

    with(renderer) {
      AttributeAppendable(StringBuilder()).render(AndroidView(EditText(context)))
    }

    assertThat(rendered).isTrue()
  }
}
