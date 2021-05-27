package com.km.composePlayground.scroller.common

import com.km.composePlayground.base.UiModel


/** Contains information about the position of an item within the scroller it is hosted in. */
sealed class PositionInfo

/**
 * The [PositionInfo] when an item is directly hosted in a Horizontal or Vertical scroller, i.e,
 * outside of a section.
 *
 * @param isFirstItem Is this the first item in the scroller.
 * @param isLastItem Is the the last item in the scroller.
 */
data class ScrollerPositionInfo(val isFirstItem: Boolean, val isLastItem: Boolean) : PositionInfo()

/**
 * The [PositionInfo] when an item is hosted in a Linear section within a Vertical scroller.
 *
 * @param isFirstItemInSection Is this the first item in the section.
 * @param isLastItemInSection Is the the last item in the section.
 */
data class LinearSectionPositionInfo(
  val isFirstItemInSection: Boolean,
  val isLastItemInSection: Boolean
) : PositionInfo()

/**
 * The [PositionInfo] when an item is hosted in a Grid section within a Vertical scroller.
 *
 * @param isFirstItemInRow Is this the first item in the grid row.
 * @param isLastItemInRow Is the the last item in the grid row.
 */
data class GridSectionPositionInfo(val isFirstItemInRow: Boolean, val isLastItemInRow: Boolean) :
  PositionInfo()

/** Class to calculate the decorations to apply to an item rendered with the passed in [UiModel] */
fun interface DecorationCalculator {
  /**
   * Note: The DecorationCalculator when used in RecyclerView will not populate the positionInfo. It
   * is only populated for Compose based scrollers.
   */
  fun getDecorationsForUiModel(uiModel: UiModel, positionInfo: PositionInfo?): List<Decoration>
}

/** Placeholder calculator that provides no decorations. */
val EMPTY_DECORATION_CALCULATOR: DecorationCalculator = DecorationCalculator { _, _ -> emptyList() }
