# ![Radiography logo](assets/icon_32.png) Radiography

[![Maven Central](https://img.shields.io/maven-central/v/com.squareup.radiography/radiography.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.squareup.radiography%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
![Android CI](https://github.com/square/radiography/workflows/Android%20CI/badge.svg)

Radiography provides a utility class to pretty print a view hierarchy.

## Usage

Add the `radiography` dependency to your app's build.gradle file:

```gradle
dependencies {
  implementation 'com.squareup.radiography:radiography:2.0.0'
}
```

`Radiography.scan()` returns a pretty string rendering of the view hierarchy of all windows managed by the current process.

```kotlin
// Render the view hierarchy for all windows.
val prettyHierarchy = Radiography.scan()

// Include the text content from TextView instances.
val prettyHierarchy = Radiography.scan(viewStateRenderers = DefaultsIncludingPii)

// Append custom attribute rendering
val prettyHierarchy = Radiography.scan(viewStateRenderers = DefaultsNoPii +
    androidViewStateRendererFor<LinearLayout> {
      append(if (it.orientation == LinearLayout.HORIZONTAL) "horizontal" else "vertical")
    })
```

You can print a subset of the view hierarchies.

```kotlin
// Extension function on View, renders starting from that view.
val prettyHierarchy = someView.scan()

// Render only the view hierarchy from the focused window, if any.
val prettyHierarchy = Radiography.scan(viewFilter = FocusedWindowViewFilter)

// Filter out views with specific ids.
val prettyHierarchy = Radiography.scan(viewFilter = SkipIdsViewFilter(R.id.debug_drawer))

// Combine view filters.
val prettyHierarchy = Radiography.scan(viewFilter = FocusedWindowViewFilter and MyCustomViewFilter())
```

## Result example

![screenshot](assets/sample_screenshot.png)

```
com.squareup.radiography.sample/com.squareup.radiography.sample.MainActivity:
window-focus:false
 DecorView { 1080×2160px }
 +-LinearLayout { 1080×2028px }
 | +-ViewStub { id:action_mode_bar_stub, GONE, 0×0px }
 | `-FrameLayout { id:content, 1080×1962px }
 |   `-ConstraintLayout { id:main, 1080×1962px }
 |     +-ImageView { id:logo, 1080×352px }
 |     +-EditText { id:username, 580×124px, text-length:4 }
 |     +-EditText { id:password, 580×124px, focused, text-length:4, ime-target }
 |     +-CheckBox { id:remember_me, 343×88px, text-length:11 }
 |     +-Button { id:signin, 242×132px, text-length:7 }
 |     +-Group { id:group, 0×0px }
 |     `-Button { id:show_dialog, 601×132px, text-length:23 }
 +-View { id:navigationBarBackground, 1080×132px }
 `-View { id:statusBarBackground, 1080×66px }
```

This sample app lives in this repo in the `sample` directory.

## Jetpack Compose support

Radiography will automatically render Composables found in your view tree if the Compose Tooling
library is on the classpath. If you're using Compose, this is likely already the case (the
`@Preview` annotation lives in the Tooling library). On the other hand, if you're not using Compose,
Radiography won't bloat your app with transitive dependencies on any Compose artifacts.

Compose releases frequently. If you are using Radiography with an unsupported version of Compose,
or you don't depend on the Tooling library, then Radiography will include a message in the result
asking you to upgrade Radiography or add the Tooling library, but this is optional.

### Compose usage

Compose support is experimental right now, since Compose is still changing a lot on a regular basis.

Composables can be filtered just like views, but instead of using view ID, you can either specify
a test tag (as in the string passed to `Modifier.testTag()`), or match on any layout ID (as in
`Modifier.layoutId()`).

```kotlin
// Filter out views with specific test tags.
val prettyHierarchy = Radiography.scan(viewFilter = skipTestTagsFilter("debug drawer"))

// Filter out views with specific layout IDs.
val prettyHierarchy = Radiography.scan(viewFilter = skipLayoutIdsFilter { layoutId ->
   layoutId == DebugDrawerLayoutId
})
```

The `DefaultsNoPii` and `DefaultsIncludingPii` renderers already configure the Compose renderers as
well. Additional compose-specific renderers can be found in the `ComposeLayoutRenderers` object.
Custom Compose renderers cannot be created at this time, since we're still figuring out exactly
what the best public API for that looks like. If you need a custom Compose renderer, please file
an issue on this repo!

### Compose example output

![screenshot](assets/compose_sample_screenshot.png)

```
com.squareup.radiography.sample/com.squareup.radiography.sample.MainActivity:
window-focus:false
 DecorView { 1080×2160px }
 +-LinearLayout { 1080×2028px }
 | +-ViewStub { id:action_mode_bar_stub, GONE, 0×0px }
 | `-FrameLayout { 1080×1962px }
 |   `-FitWindowsLinearLayout { id:action_bar_root, 1080×1962px }
 |     +-ViewStubCompat { id:action_mode_bar_stub, GONE, 0×0px }
 |     `-ContentFrameLayout { id:content, 1080×1962px }
 |       `-AndroidComposeView { 1080×1962px, focused }
 |         `-Providers { 1080×1962px }
 |           `-ComposeSampleApp { 992×1874px }
 |             +-Image { 240×352px }
 |             +-TextField { 770×154px, test-tag:"text-field" }
 |             | +-Box { 158×44px, layout-id:"Label" }
 |             | | `-ProvideTextStyle { 158×44px, text-length:8 }
 |             | `-ProvideTextStyle { 682×59px, layout-id:"TextField" }
 |             |   `-BaseTextField { 682×59px, text-length:4 }
 |             |     `-Layout { 682×59px }
 |             +-TextField { 770×154px }
 |             | +-Box { 155×44px, layout-id:"Label" }
 |             | | `-ProvideTextStyle { 155×44px, text-length:8 }
 |             | `-ProvideTextStyle { 682×59px, layout-id:"TextField" }
 |             |   `-BaseTextField { 682×59px, text-length:4, FOCUSED }
 |             |     `-Layout { 682×59px }
 |             +-Row { 387×67px }
 |             | +-Checkbox { 55×55px, value:"Unchecked" }
 |             | +-Spacer { 22×0px }
 |             | `-Text { 298×59px, text-length:11 }
 |             +-Button { 226×99px }
 |             | `-Providers { 138×55px }
 |             |   `-Text { 138×52px, text-length:7 }
 |             +-AndroidView {  }
 |             | `-ViewBlockHolder { 919×53px }
 |             |   `-TextView { 919×53px, text-length:53 }
 |             +-ScrollableRow { 1320×588px }
 |             | `-ScrollableColumn { 1320×821px }
 |             |   `-Text { 1320×821px, test-tag:"live-hierarchy", text-length:2320 }
 |             `-TextButton { 615×99px }
 |               `-Providers { 571×55px }
 |                 `-Text { 571×52px, text-length:28 }
 +-View { id:navigationBarBackground, 1080×132px }
 `-View { id:statusBarBackground, 1080×66px }
```

This sample can be found in the `sample-compose` directory.

## FAQ

### What is Radiography useful for?

Radiography is useful whenever you want to look at the view hierarchy and don't have the ability to connect the hierarchy viewer tool. You can add the view hierarchy string as metadata to crash reports, add a debug drawer button that will print it to Logcat, and use it to improve Espresso errors ([here's an example](https://twitter.com/Piwai/status/1291771701584252928)).

### Is Radiography production ready?

The code that retrieves the root views is based on Espresso's [RootsOracle](https://github.com/android/android-test/blob/master/espresso/core/java/androidx/test/espresso/base/RootsOracle.java) so it's unlikely to break in newer Android versions. We've been using Radiography for crash reports in production since 2015 without any issue.

### Why use custom attribute string rendering instead of View.toString() ?

The output of `View.toString()` is useful but harder to read:

```
// View.toString():
Button { VFED..C.. ........ 0,135-652,261 #7f010001 app:id/show_dialog }
// Radiography:
Button { id:show_dialog, 652x126px, text-length:28 }
```

If you'd rather rely on `View.toString()`, you can provide a custom state renderer.

```kotlin
val prettyHierarchy = Radiography.scan(viewStateRenderers = listOf(androidViewStateRendererFor<View> {
  append(
      it.toString()
          .substringAfter(' ')
          .substringBeforeLast('}')
  )
}))
```

### How are compositions rendered?

_Disclaimer: Compose is changing frequently, so many of these details may change without warning,
and none of this is required to use Radiography!_

The API for configuring how composables are rendered is slightly different than for regular views,
since composables simply _are not_ `View`s. What might define a UI "component" or "widget" logically
isn't made up of any single, nicely-encapsulated object. It is likely a few layers of convenience
`@Composable` functions, with some `Modifier`s applied at various levels, and state is stored in
the slot table via `remember{}`, not in instance fields.

Radiography uses the Compose Tooling library to parse a composition's slot table into a tree of
objects which represent "groups" – each group can represent some data stored in the slot table,
a function call, or an emitted layout node or Android view. Groups may include source locations in
certain cases, and contain information about modifiers and function parameters. This is a really
powerful API, but there's a _lot_ of data there, and much of it is not helpful for a description
that should be easy to read to get a general sense of the state of your UI.

Radiography filters this detailed tree of groups down to only contain the actual `LayoutNode`s and
Android `View`s emitted by the composition. It identifies each such node by the name of the function
call that is highest in the subtree below the parent emitted node. The node's modifiers are used to
extract interesting data about the composable. Most of the interesting data is stored in a single
modifier, the `SemanticsModifier`. This is the modifier used to store "semantics" information which
includes accessibility descriptions, actions, and flags, and is also used for writing UI tests.

## License

<pre>
Copyright 2020 Square Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
