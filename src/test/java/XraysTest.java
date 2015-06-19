import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.radiography.Xrays;
import com.squareup.radiography.diagnostic.TextViewDiagnostic;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class) @Config(manifest = Config.NONE)
public class XraysTest {

  private static final char BLANK = '\u00a0';
  private Context context;
  private Xrays xrays;

  @Before
  public void setUp() {
    context = Robolectric.application;
    xrays = Xrays.create();
  }

  @Test public void viewDetailsReported() {
    View view = new View(context);
    view.setVisibility(View.INVISIBLE);
    view.setRight(30);
    view.setBottom(30);
    view.setEnabled(false);
    view.setSelected(true);
    assertThat(xrays.scan(view)).contains("INVISIBLE")
        .contains("30x30px")
        .contains("disabled")
        .contains("selected");
  }

  @Test public void nullView() {
    assertThat(xrays.scan(null)).contains("null");
  }

  @Test public void checkableChecked() {
    CheckBox view = new CheckBox(context);
    view.setChecked(true);
    assertThat(xrays.scan(view)).contains("checked");
  }

  @Test public void textView() {
    TextView view = new TextView(context);
    view.setText("Baguette");
    String scan = xrays.scan(view);
    assertThat(scan).contains("text-length:8").doesNotContain("text:");
  }

  @Test public void textViewContents() {
    TextView view = new TextView(context);
    view.setText("Baguette Avec Fromage");

    Xrays xrays = new Xrays.Builder() //
        .clearDiagnostics() //
        .withDiagnostic(new TextViewDiagnostic(true)) //
        .build();
    String scan = xrays.scan(view);
    assertThat(scan).contains("text-length:21").contains("text:\"Baguette Avec Fromage\"");
  }

  @Test public void textViewContentsEllipsized() {
    TextView view = new TextView(context);
    view.setText("Baguette Avec Fromage");

    Xrays xrays = new Xrays.Builder() //
        .clearDiagnostics() //
        .withDiagnostic(new TextViewDiagnostic(true, 11)) //
        .build();
    String scan = xrays.scan(view);
    assertThat(scan).contains("text-length:21").contains("text:\"Baguette A…\"");
  }

  @Test public void recoversFromException() {
    FrameLayout layout = new FrameLayout(context);
    layout.addView(new View(context) {
      @Override public boolean isEnabled() {
        throw new UnsupportedOperationException("Leave me alone");
      }
    });
    assertThat(xrays.scan(layout)).contains("FrameLayout").contains("Leave me alone");
  }

  @Test public void skipIds() {
    FrameLayout layout = new FrameLayout(context);
    Button view = new Button(context);
    view.setId(42);
    layout.addView(view);
    assertThat(Xrays.withSkippedIds(42).scan(layout)).contains("FrameLayout").doesNotContain("Button");
  }

  @Test public void nestedViews() {
    FrameLayout root = new FrameLayout(context);
    root.addView(new View(context));
    LinearLayout childLayout1 = new LinearLayout(context);
    root.addView(childLayout1);
    childLayout1.addView(new View(context));
    childLayout1.addView(new View(context));
    LinearLayout childLayout2 = new LinearLayout(context);
    root.addView(childLayout2);
    childLayout2.addView(new View(context));
    childLayout2.addView(new View(context));
    System.out.println(xrays.scan(root));
    assertThat(xrays.scan(root)).contains(""
            + "FrameLayout { 0x0px }\n"
            + BLANK
            + "+-View { 0x0px }\n"
            + BLANK
            + "+-LinearLayout { 0x0px }\n"
            + BLANK
            + "| +-View { 0x0px }\n"
            + BLANK
            + "| `-View { 0x0px }\n"
            + BLANK
            + "`-LinearLayout { 0x0px }\n"
            + BLANK
            + "  +-View { 0x0px }\n"
            + BLANK
            + "  `-View { 0x0px }"
    );
  }
}
