package com.km.composePlayground.scroller

/**
 * Positional information provided by a section about a UiModel being rendered.
 *
 * @property isStartCell Is this the first element in the scroller.
 * @property isEndCell Is this the last element in the scroller.
 *
 * Note: In case of nested scrollers, the positionInfo represents the position of the element within
 * the inner scroller.
 */
class PositionInfo(
  val isStartCell: Boolean,
  val isEndCell: Boolean,
)