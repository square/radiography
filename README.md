# Radiography

Radiography provides a utility class to pretty print a view hierarchy.

## Usage

```
String prettyHierarchy = Xrays.create().scan(myView);
```

You can ignore parts of the view hierarchy (e.g. a debug view) by providing ids to skip. You can scan from the root of the view hierarchy with `Xrays.scanFromRoot()`.

```
prettyPrintButton.setOnClickListener(new View.OnClickListener() {
  @Override public void onClick(View prettyPrintButton) {
    int skippedId = prettyPrintButton.getId();
    Log.d("ViewHierarchy", Xrays.withSkippedIds(skippedId).scanFromRoot(prettyPrintButton));
  }
});
```

## Result example

![screenshot](.images/demo_screenshot.png)

```
window-focus:true
     DecorView { 1080x2160px }
     +-LinearLayout { 1080x2028px }
     | +-ViewStub { id:action_mode_bar_stub, GONE, 0x0px }
     | `-FrameLayout { 1080x1962px }
     |   `-ActionBarOverlayLayout { id:decor_content_parent, 1080x1962px }
     |     +-ContentFrameLayout { id:content, 1080x1808px }
     |     | `-ConstraintLayout { id:container, 1080x1808px }
     |     |   +-AppCompatEditText { id:username, 860x124px, text-length:4 }
     |     |   +-AppCompatEditText { id:password, 860x124px, focused, text-length:8, ime-target }
     |     |   +-MaterialButton { id:login, 533x132px, text-length:19 }
     |     |   +-MaterialButton { id:xray, 242x132px, text-length:4 }
     |     |   `-ProgressBar { id:loading, GONE, 0x0px }
     |     `-ActionBarContainer { id:action_bar_container, 1080x154px }
     |       +-Toolbar { id:action_bar, 1080x154px }
     |       | +-AppCompatTextView { 468x74px, text-length:15 }
     |       | `-ActionMenuView { 0x154px }
     |       `-ActionBarContextView { id:action_context_bar, GONE, 0x0px }
     +-View { id:navigationBarBackground, 1080x132px }
     `-View { id:statusBarBackground, 1080x66px }

```
