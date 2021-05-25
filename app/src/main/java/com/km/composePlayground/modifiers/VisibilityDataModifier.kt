package com.km.composePlayground.modifiers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * A modifier which will update the provided [VisibilityData] to enable its visibility querying API.
 */
fun Modifier.visibilityCache(data: VisibilityData): Modifier {
  return this.then(Modifier.onGloballyPositioned { coordinates -> data.coords = coordinates })
}

/**
 * Provides a [remember] ed VisibilityData that will cache its internal state to allow lazy
 * evaluation of visibility APIs based on number of pixels displayed on the screen.
 */
@Composable
fun rememberVisibilityData(): VisibilityData {
  return remember { VisibilityData() }
}

/**
 * Wrapper object which provides methods for determining the number of pixels of a specific UI
 * Composable has displayed on the screen, or whether it is visible (> 0 pixels).
 *
 * Note: All methods on this object are computed lazily, as pushing the resolved data values is to
 * expensive with how many UI Composables require visibility tracking.
 */
class VisibilityData constructor() {

  /** Number of pixels that are currently visible on the screen. */
  val numVisiblePixels: Int
    get() {
      return coords?.run {
        // boundsInRoot fails if not attached to hierarchy
        if (!isAttached) {
          0
        } else {
          boundsInRoot().run { (width * height).toInt() }
        }
      }
        ?: 0
    }

  /** Returns true if there is at least 1 pixel visible on the screen. */
  val isVisible: Boolean
    get() = numVisiblePixels > 0

  /** Returns true if LayoutCoordinates have been provided (i.e. layout portion has completed.) */
  val hasCoordinates: Boolean
    get() = coords != null

  // Do not use State for storage to avoid triggering re-compositions on every x/y value change.
  // Store the raw layoutCoordinates to allow lazy evaluation of number of pixels on screen.
  internal var coords: LayoutCoordinates? = null

  override fun toString() =
    "VisibilityData(isAttached=${coords?.isAttached} boundsInRoot=${coords?.boundsInRoot()})"
}
