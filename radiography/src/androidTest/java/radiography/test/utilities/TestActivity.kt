package radiography.test.utilities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.squareup.radiography.test.R

class TestActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.test)

    intent.getStringExtra("textViewText")?.let { text ->
      findViewById<TextView>(R.id.intent_text).text = text
    }
  }

  companion object {
    fun Intent.withTextViewText(text: String): Intent = apply {
      putExtra("textViewText", text)
    }
  }
}
