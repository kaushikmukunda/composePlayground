package com.km.composePlayground.scroller.common

import androidx.compose.ui.unit.Dp

/**
 * Given the width of a child of the horizontal scroller, where all children have the same width,
 * return the desired height of the horizontal scroller.
 */
typealias CalculateScrollerHeight = (childWidth: Dp) -> Dp