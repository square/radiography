Change Log
==========

Version 2.0.0-alpha.2
---------------------

_2020-08-12_

* Only access the view hierarchy from the main thread. (#30)
* Remove build config class. (#36)
* Change ViewFilter input from View to Any. (#37)
* Rename StateRenderer to ViewStateRenderer. (#38)
* Generate JVM overloads for methods with default args. (#38)
* Make ViewStateRenderers properties static and fix case. (#38)
* Make ViewFilter.and an extension function. (#38)

Version 2.0.0-alpha.1
---------------------

_2020-08-11_

* First public release!
* Convert the code to Kotlin. (#8, #9)
* Rewrite the API to be more Kotlin-friendly. (#16)
* Introduce `StateRenderer` to allow custom rendering of arbitrary view attributes. (#23)
* Drop the `com.squareup` package prefix, so the package is now just `radiography`. (#15)
* Add a sample app. (#18)
* Fix: Crash when `skippedIds` is empty. (#16)
* Fix: `FocusedWindowViewFilter` wasn't filtering correctly. (#18)

Version starts at 2.0.0 to differentiate from some internal releases.
