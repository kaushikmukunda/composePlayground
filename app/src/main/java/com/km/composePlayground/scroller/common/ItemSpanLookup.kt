package com.km.composePlayground.scroller.common

/**
 * Calculates the number of cells in the grid which an item will occupy.
 *
 * This is a function which takes in the position of the item in the grid and returns the number
 * of cells an item will occupy in the grid.
 */
typealias ItemSpanLookup = (position: Int) -> Int
