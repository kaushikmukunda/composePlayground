package com.km.composePlayground.scroller

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.km.composePlayground.base.UiModel

internal fun LazyListScope.StaticGridUi(
  model: StaticGridUiModel,
  elementRenderer: ElementRenderer
) {
  val content = model.content.value

  VerticalGridUi(
    itemList = content.itemList,
    elementRenderer = elementRenderer,
    getCellWidth = { maxWidth / content.spanCount },
    getNumColumns = { content.spanCount },
    spanLookup = content.spanLookup,
    scrollingUiAction = content.scrollingUiAction
  )
}

internal fun LazyListScope.DynamicGridUi(
  model: DynamicGridUiModel,
  elementRenderer: ElementRenderer
) {
  val content = model.content.value

  VerticalGridUi(
    itemList = content.itemList,
    elementRenderer = elementRenderer,
    getCellWidth = { density -> with(density) { content.desiredCellSize.toDp() } },
    getNumColumns = { constraints.maxWidth / content.desiredCellSize },
    spanLookup = content.spanLookup,
    scrollingUiAction = content.scrollingUiAction
  )
}

private fun LazyListScope.VerticalGridUi(
  itemList: List<UiModel>,
  elementRenderer: ElementRenderer,
  getCellWidth: BoxWithConstraintsScope.(density: Density) -> Dp,
  getNumColumns: BoxWithConstraintsScope.() -> Int,
  spanLookup: ItemSpanLookup,
  scrollingUiAction: ScrollingUiAction
) {

  // Keeps track of start and end indexes of items from itemList that are placed in each Row.
  val gridRowIndexes = mutableListOf<GridRowIndexes>()

  // No efficient way to compute actual number of rows
  items(itemList.size) { rowIdx ->
    BoxWithConstraints {
      fun canPlaceItemsInRow(rowIdx: Int): Boolean {
        val allItemsPlaced = gridRowIndexes.isNotEmpty() &&
          gridRowIndexes.last().endIndex == itemList.size - 1
        val isAlreadyPlacedRow = rowIdx < gridRowIndexes.size
        return isAlreadyPlacedRow || !allItemsPlaced
      }

      fun isEndOfList(itemIdx: Int) = itemIdx >= itemList.size

      val cellWidth = getCellWidth(LocalDensity.current)
      val numColumns = getNumColumns()

      if (!canPlaceItemsInRow(rowIdx)) {
        return@BoxWithConstraints
      }

      Row(modifier = Modifier.fillMaxWidth()) {
        // The first item from the list to be placed is the element after the last element placed
        // in the previous row.
        val startIdx = if (rowIdx == 0) 0 else gridRowIndexes[rowIdx - 1].endIndex + 1
        // itemIdx specifies the next item from the list that needs to be placed.
        var itemIdx = startIdx
        // cellIdx specifies the next cell to be populated in the row.
        var cellIdx = 0

        while (cellIdx < numColumns && !isEndOfList(itemIdx)) {
          val itemCellWidth = spanLookup(itemIdx)
          cellIdx += itemCellWidth

          // Not enough space available for this item in this Row, finalize Row
          if (cellIdx > numColumns) {
            break
          }

          gridRowIndexes.addOrUpdate(rowIdx, startIdx, itemIdx)
          Log.d("dbg", "rendering $itemIdx")
          val item = itemList[itemIdx++]
          elementRenderer.Render(
            uiModel = item,
            modifier = Modifier.width(cellWidth.times(itemCellWidth))
          )

          SideEffect {
            scrollingUiAction.onItemRendered(itemIdx)
          }
        }
      }
    }
  }
}

private fun MutableList<GridRowIndexes>.addOrUpdate(idx: Int, startIdx: Int, endIdx: Int) {
  if (this.isEmpty() || this.size <= idx) {
    this.add(idx, GridRowIndexes(startIdx, endIdx))
  } else {
    this[idx].startIndex = startIdx
    this[idx].endIndex = endIdx
  }
}

/** Container class to hold start and end indexes of items placed in each Row */
private class GridRowIndexes(var startIndex: Int, var endIndex: Int)


//@Composable
//private fun LazyRowOrColumn(orientation: Int,
//                            modifier: Modifier = Modifier,
//                            content: LazyListScope.() -> Unit) {
//  if (orientation == 0) { // Horizontal
//    LazyRow(modifier = modifier.fillMaxHeight(), content = content)
//  } else {
//    LazyColumn(modifier = modifier.fillMaxWidth(), content = content)
//  }
//}
//
//@Composable
//private fun RowOrColumn(orientation: Int, content: @Composable() () -> Unit) {
//  if (orientation == 0) { // Horizontal
//    Column(
//      verticalArrangement = Arrangement.SpaceEvenly,
//      modifier = Modifier.fillMaxHeight()) {
//      content.invoke()
//    }
//  } else {
//    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
//      content.invoke()
//    }
//  }
//}