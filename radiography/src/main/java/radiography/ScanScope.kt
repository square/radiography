package radiography

/**
 * Defines the scope of the scan output by returning a list of root [ScannableView]s that will to
 * scan.
 *
 * Some commons scopes are:
 *  - [ScanScopes.AllWindowsScope]
 *  - [ScanScopes.FocusedWindowScope]
 *  - [ScanScopes.singleViewScope]
 */
public fun interface ScanScope {

  /**
   * Returns the [ScannableView]s that scanning should start from.
   *
   * This method may be called from any thread.
   */
  public fun findRoots(): List<ScannableView>
}
