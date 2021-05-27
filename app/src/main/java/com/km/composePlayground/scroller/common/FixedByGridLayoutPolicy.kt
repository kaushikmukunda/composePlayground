package com.km.composePlayground.scroller.common

import androidx.annotation.FloatRange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.scroller.horizontal.HorizontalScrollerLayoutPolicy

/**
 * [HorizontalScrollerLayoutPolicy] that distributes the width of the horizontal scroller equally
 * among the visible children while taking into account the additionally passed parameters.
 *
 * See [FixedByGridLayoutPolicyFactory] below for all param descriptions.
 */
class FixedByGridLayoutPolicy
constructor(
  override val contentStartPadding: Dp = 16.dp,
  override val contentEndPadding: Dp = 16.dp,
  @FloatRange(from = 0.0, to = 1.0) val childPeekingFraction: Float = 0.1f,
  private val desiredChildWidth: Dp,
  private val childWidthMultiplier: Int,
  private val calculateScrollerHeight: CalculateScrollerHeight? = null
) : HorizontalScrollerLayoutPolicy {

  override fun calculateChildWidth(scrollerWidth: Dp): Dp? {
    val availableWidthForChildren = scrollerWidth - contentStartPadding - contentEndPadding

    return Dp(
      CardCountHelper.getUnitCardWidth(
        desiredChildWidth.value.toInt(),
        availableWidthForChildren.value.toInt(),
        childPeekingFraction
      ) * childWidthMultiplier.toFloat()
    )

  }

  override fun calculateScrollerHeight(childWidth: Dp?): Dp? {
    return calculateScrollerHeight?.invoke(requireNotNull(childWidth))
  }
}