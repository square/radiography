package com.squareup.radiography.sample.compose

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog.Builder
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.squareup.radiography.sample.compose.R.drawable
import radiography.Radiography
import radiography.ScanScopes.FocusedWindowScope
import radiography.ViewFilters.skipComposeTestTagsFilter
import radiography.ViewStateRenderers.CheckableRenderer
import radiography.ViewStateRenderers.DefaultsIncludingPii
import radiography.ViewStateRenderers.DefaultsNoPii
import radiography.ViewStateRenderers.ViewRenderer
import radiography.ViewStateRenderers.androidViewStateRendererFor
import radiography.ViewStateRenderers.textViewRenderer
import radiography.compose.ExperimentalRadiographyComposeApi

internal const val TEXT_FIELD_TEST_TAG = "text-field"
internal const val LIVE_HIERARCHY_TEST_TAG = "live-hierarchy"

@OptIn(ExperimentalRadiographyComposeApi::class, ExperimentalAnimationApi::class)
@Composable fun ComposeSampleApp() {
  val context = ContextAmbient.current
  val liveHierarchy = remember { mutableStateOf<String?>(null) }

  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var rememberMe by remember { mutableStateOf(false) }

  MaterialTheme {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalGravity = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Image(imageResource(drawable.logo), Modifier.height(128.dp))

      TextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Username") },
          backgroundColor = Color.Transparent,
          modifier = Modifier.testTag(TEXT_FIELD_TEST_TAG)
      )
      TextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password") },
          backgroundColor = Color.Transparent,
          visualTransformation = PasswordVisualTransformation()
      )
      Row(verticalGravity = Alignment.CenterVertically) {
        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
        Spacer(Modifier.width(8.dp))
        Text("Remember me")
      }
      Button(onClick = {}) {
        Text("Sign In")
      }
      // Include a classic Android view in the composition.
      AndroidView(::TextView) {
        @SuppressLint("SetTextI18n")
        it.text = "By signing in, you agree to our Terms and Conditions."
      }

      liveHierarchy.value?.let {
        ScrollableRow(modifier = Modifier.weight(1f)) {
          ScrollableColumn {
            Text(
                liveHierarchy.value.orEmpty(),
                fontFamily = FontFamily.Monospace,
                fontSize = 6.sp,
                modifier = Modifier.testTag(LIVE_HIERARCHY_TEST_TAG)
            )
          }
        }
      }
      TextButton(onClick = { showSelectionDialog(context) }) {
        Text("Show string rendering dialog")
      }

      onCommit {
        liveHierarchy.value = Radiography.scan(
            viewStateRenderers = DefaultsIncludingPii,
            // Don't trigger infinite recursion.
            viewFilter = skipComposeTestTagsFilter(LIVE_HIERARCHY_TEST_TAG)
        )
      }
    }
  }
}

@OptIn(ExperimentalRadiographyComposeApi::class)
private fun showSelectionDialog(context: Context) {
  val renderings = listOf(
      "Default" to {
        Radiography.scan()
      },
      "Focused window" to {
        Radiography.scan(scanScope = FocusedWindowScope)
      },
      "Skip testTag(\"$TEXT_FIELD_TEST_TAG\")" to {
        Radiography.scan(viewFilter = skipComposeTestTagsFilter(TEXT_FIELD_TEST_TAG))
      },
      "Focused window and custom filter" to {
        Radiography.scan(
            scanScope = FocusedWindowScope,
            viewFilter = { view -> view !is LinearLayout }
        )
      },
      "Include PII" to {
        Radiography.scan(viewStateRenderers = DefaultsIncludingPii)
      },
      "Include PII ellipsized" to {
        Radiography.scan(
            viewStateRenderers = listOf(
                ViewRenderer,
                textViewRenderer(showTextValue = true, textValueMaxLength = 4),
                CheckableRenderer,
            )
        )
      },
      "Custom LinearLayout renderer" to {
        Radiography.scan(
            viewStateRenderers = DefaultsNoPii + androidViewStateRendererFor<LinearLayout> {
              append(if (it.orientation == LinearLayout.HORIZONTAL) "horizontal" else "vertical")
            })
      }
  )

  val items = renderings.map { it.first }
      .toTypedArray()
  Builder(context)
      .setTitle("Choose rendering")
      .setItems(items) { _, index ->
        val rendering = renderings[index].second()
        // Print each line as a separate logcat entry so the total output doesn't get truncated.
        rendering.lineSequence().forEach {
          Log.d("MainActivity", it)
        }
        showResult(context, rendering)
      }
      .show()
}

private fun showResult(
  context: Context,
  rendering: String
) {
  val renderingDialog = Builder(context)
      .setTitle("Rendering (also printed to Logcat)")
      .setMessage(rendering)
      .setPositiveButton("Ok") { _, _ ->
        showSelectionDialog(context)
      }
      .show()
  val messageView = renderingDialog.findViewById<TextView>(android.R.id.message)!!
  messageView.textSize = 9f
  messageView.typeface = Typeface.MONOSPACE
}
