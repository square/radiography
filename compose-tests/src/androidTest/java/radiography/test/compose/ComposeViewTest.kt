package radiography.test.compose

import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.ui.LayoutModifier
import androidx.compose.ui.Measurable
import androidx.compose.ui.MeasureScope
import androidx.compose.ui.MeasureScope.MeasureResult
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.ui.test.android.createAndroidComposeRule
import androidx.ui.test.runOnIdle
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import radiography.ScannableView
import radiography.ScannableView.AndroidView
import radiography.ScannableView.ComposeView
import radiography.compose.ExperimentalRadiographyComposeApi
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalRadiographyComposeApi::class)
class ComposeViewTest {

  @get:Rule
  val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test fun androidView_children_includes_ComposeViews() {
    composeRule.setContent {
      Text("hello")
    }

    val textComposable = findRootComposeView().allDescendentsDepthFirst
        .single { it.displayName == "Text" }
    assertThat(textComposable.children.asIterable()).isEmpty()
  }

  @Test fun composeView_children_includes_AndroidView() {
    composeRule.setContent {
      Column {
        androidx.compose.ui.viewinterop.AndroidView(::TextView)
      }
    }

    val columnView = findRootComposeView().allDescendentsDepthFirst
        .single { it.displayName == "Column" }
    val androidViews = columnView.allDescendentsDepthFirst
        .filterIsInstance<AndroidView>()
        .toList()
    assertThat(androidViews.count { it.view is TextView }).isEqualTo(1)
  }

  @Test fun composeView_reports_children() {
    composeRule.setContent {
      Column {
        Text("hello")
        Button(onClick = {}) {
          Box()
        }
      }
    }

    val columnView = findRootComposeView()
        .allDescendentsDepthFirst
        .first { it.displayName == "Column" }
    val columnChildren = columnView.children.toList()
    assertThat(columnChildren).hasSize(2)
    assertThat(columnChildren[0].displayName).isEqualTo("Text")
    assertThat(columnChildren[1].displayName).isEqualTo("Button")
    assertThat(columnChildren[1].allDescendentsDepthFirst.count { it.displayName == "Box" })
        .isEqualTo(1)
  }

  @Test fun composeView_reports_LayoutModifiers() {
    composeRule.setContent {
      Box(TestModifier)
    }

    val boxView =
      findRootComposeView().allDescendentsDepthFirst.single { it.displayName == "Box" } as ComposeView
    assertThat(boxView.modifiers).contains(TestModifier)
  }

  @Test fun composeView_reports_size() {
    var density: Density? = null
    composeRule.setContent {
      density = DensityAmbient.current
      Box(Modifier.size(30.dp, 40.dp))
    }

    val (widthPx, heightPx) = runOnIdle {
      with(density!!) {
        Pair(30.dp.toIntPx(), 40.dp.toIntPx())
      }
    }

    val boxView =
      findRootComposeView().allDescendentsDepthFirst.single { it.displayName == "Box" } as ComposeView
    assertThat(boxView.width).isEqualTo(widthPx)
    assertThat(boxView.height).isEqualTo(heightPx)
  }

  private object TestModifier : LayoutModifier {
    override fun MeasureScope.measure(
      measurable: Measurable,
      constraints: Constraints
    ): MeasureResult = layout(1, 2) {}
  }

  private fun findRootComposeView(): ComposeView =
    findRootAndroidView().allDescendentsDepthFirst
        .filterIsInstance<ComposeView>()
        .first()

  private fun findRootAndroidView(): AndroidView {
    val latch = CountDownLatch(1)
    var androidView: AndroidView? = null
    composeRule.activityRule.scenario.onActivity {
      val contentView = it.findViewById<View>(android.R.id.content)
      androidView = AndroidView(contentView)
    }
    latch.await(1, TimeUnit.SECONDS)
    return androidView!!
  }

  private val ScannableView.allDescendentsDepthFirst: Sequence<ScannableView>
    get() = children.flatMap { sequenceOf(it) + it.allDescendentsDepthFirst }
}
