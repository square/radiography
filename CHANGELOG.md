Change Log
==========

Version 2.2.0
-------------

_2021-1-26_

* Don't render curly braces when they would be empty. (#108)
* Upgrade Compose to 1.0.0-alpha09. (#117)
* Lazily reflect the mKeyedTags field and don't crash if it's not there. (#122)

Version 2.1.0
-------------

_2020-10-14_

* Introduce support for rendering across subcompositions. (#104)
* Fix for bug where layout IDs to skip were queried incorrectly. (#94 – thanks @samruston!)
* Fix grammatical errors in documentation. (#92 – thanks @androiddevnotesfork!)
* Upgrade Compose to 1.0.0-alpha05. (#106)

Version 2.0.0
-------------

_2020-09-04_

* Use fancy drawing characters for rendering tree lines. (#83)
* Don't include "text-length" when full text included. (#82)
* Move internal code to internal package. (#87)
* Rename `showTextValue` parameter to `renderTextValue`. (#90)
* Update Compose to 1.0.0-alpha02. (#79)

Version 2.0.0-beta.1
--------------------

_2020-09-02_

* Correctly render nodes whose descriptions are more than one line. (#42)
* Support rendering trees nested deeper than 64 levels. (#41)
* Refactor `ViewStateRenderer` and `ViewFilter` API to be subclassable and more accessible from
  Java. (#44)
* Use the × character for formatting dimensions instead of the letter x. (#49)
* Add a sample app that uses Compose. (#53)
* Introduce support for rendering Compose hierarchies. (#33)
* Make SAM interfaces fun interfaces so they can be given as lambdas in Kotlin 1.4. (#47)
* Introduce ScanScope, a more flexible way to define what to scan. (#70)

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
