package com.km.composePlayground.scroller.horizontal


import androidx.compose.ui.unit.Dp

/** Determines a policy to layout a horizontally scrollable list. */
interface HorizontalScrollerLayoutPolicy {

  /** Padding in Dp that is added at the start of the horizontal scroller before the first child. */
  val contentStartPadding: Dp

  /** Padding in Dp that is added at the end of the horizontal scroller after the last child. */
  val contentEndPadding: Dp

  /**
   * Given the width of the horizontal scroller in Dp, returns the width of a child in Dp. The
   * returned width will be equally applied to all children. If the returned width is null, the
   * children will be measured based on their layout params in their respective layout xml files.
   */
  fun calculateChildWidth(scrollerWidth: Dp): Dp?

  /**
   * Given the width of a child in Dp, returns the desired height of the horizontal scroller or null
   * if this height is not a function of the child width, in which case the horizontal scroller
   * height will not be modified.
   *
   * @param childWidth width in Dp, if available.
   */
  fun calculateScrollerHeight(childWidth: Dp?): Dp?
}
