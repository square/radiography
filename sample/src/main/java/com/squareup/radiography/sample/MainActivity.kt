package com.squareup.radiography.sample

import android.R.id
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import radiography.FocusedWindowViewFilter
import radiography.Radiography
import radiography.SkipIdsViewFilter
import radiography.ViewFilter

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)

    findViewById<View>(R.id.show_dialog).setOnClickListener { showSelectionDialog() }
  }

  private fun showSelectionDialog() {
    val renderings = listOf(
        "Default" to {
          Radiography.scan()
        },
        "Focused window" to {
          Radiography.scan(viewFilter = FocusedWindowViewFilter)
        },
        "Start from R.id.main" to {
          Radiography.scan(rootView = findViewById(R.id.main))
        },
        "Skip R.id.show_dialog" to {
          Radiography.scan(viewFilter = SkipIdsViewFilter(R.id.show_dialog))
        },
        "Focused window and custom filter" to {
          Radiography.scan(viewFilter = FocusedWindowViewFilter and object : ViewFilter {
            override fun matches(view: View) = view !is LinearLayout
          })
        },
        "Include text" to {
          Radiography.scan(includeTextViewText = true)
        },
        "Include text ellipsized" to {
          Radiography.scan(includeTextViewText = true, textViewTextMaxLength = 4)
        }
    )

    val items = renderings.map { it.first }
        .toTypedArray()
    AlertDialog.Builder(this)
        .setTitle("Choose rendering")
        .setItems(items) { _, index ->
          val rendering = renderings[index].second()
          Log.d("MainActivity", rendering)
          showResult(rendering)
        }
        .show()
  }

  private fun showResult(rendering: String) {
    val renderingDialog = AlertDialog.Builder(this)
        .setTitle("Rendering (also printed to Logcat)")
        .setMessage(rendering)
        .setPositiveButton("Ok") { _, _ ->
          showSelectionDialog()
        }
        .show()
    val messageView = renderingDialog.findViewById<TextView>(id.message)
    messageView.textSize = 9f
    messageView.typeface = Typeface.MONOSPACE
  }
}
