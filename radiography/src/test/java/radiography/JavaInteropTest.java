package radiography;

import android.widget.TextView;
import kotlin.Unit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import radiography.ScannableView.AndroidView;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static radiography.ScanScopes.AllWindowsScope;
import static radiography.ScanScopes.FocusedWindowScope;
import static radiography.ViewFilters.NoFilter;
import static radiography.ViewFilters.androidViewFilterFor;
import static radiography.ViewStateRenderers.DefaultsIncludingPii;
import static radiography.ViewStateRenderers.ViewRenderer;
import static radiography.ViewStateRenderers.androidViewStateRendererFor;
import static radiography.ViewStateRenderers.textViewRenderer;

/**
 * Doesn't actually test anything at runtime, just lets us validate how the API looks from Java
 * consumers.
 */
@SuppressWarnings("unused")
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class JavaInteropTest {

  @Test public void createScanScopeFromJava() {
    @SuppressWarnings("ConstantConditions")
    ScanScope javaScanScope = () -> singletonList(new AndroidView(null));
  }

  @Test public void createViewFilterFromJava() {
    ViewFilter javaViewFilter = androidViewFilterFor(TextView.class,
        view -> view.getText() != null);
  }

  @Test public void createViewRendererFromJava() {
    ViewStateRenderer javaViewRenderer = androidViewStateRendererFor(TextView.class,
        (appendable, textView) -> {
          CharSequence error = textView.getError();
          if (error != null) {
            appendable.append(error);
          }
          return Unit.INSTANCE;
        });
  }

  @Test public void scanFromJava() {
    Radiography.scan();
    Radiography.scan(FocusedWindowScope);
    Radiography.scan(
        AllWindowsScope,
        asList(
            ViewRenderer,
            textViewRenderer()
        ));
    Radiography.scan(
        AllWindowsScope,
        DefaultsIncludingPii,
        NoFilter
    );
  }
}
