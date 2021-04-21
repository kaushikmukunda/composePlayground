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
    scrollingUiAction = content.scrollingUiAction,
    identity = content.dataId
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
    scrollingUiAction = content.scrollingUiAction,
    identity = content.dataId
  )
}

private fun LazyListScope.VerticalGridUi(
  itemList: List<UiModel>,
  elementRenderer: ElementRenderer,
  getCellWidth: BoxWithConstraintsScope.(density: Density) -> Dp,
  getNumColumns: BoxWithConstraintsScope.() -> Int,
  spanLookup: ItemSpanLookup,
  scrollingUiAction: ScrollingUiAction,
  identity: String
) {

  // Keeps track of start and end indexes of items from itemList that are placed in each Row.
  val gridRowIndexes = mutableListOf<GridRowIndexes>()

  // No efficient way to compute actual number of rows
  items(
    count = itemList.size,
    key = { idx ->
      if (idx > 0) {
        if (idx > gridRowIndexes.size) {
//          Log.d("dbg", "emptyRow key $idx")
          "${identity}_emptyrow_$idx"
        } else {
//          Log.d("dbg", "valid row key $idx ${identity}_row_${gridRowIndexes[idx - 1].endIndex}")
          "${identity}_${itemList.hashCode()}_row_${gridRowIndexes[idx - 1].endIndex}"
        }
      } else {
//        Log.d("dbg", "first row key")
        "${identity}_${itemList.hashCode()}_row_000.}"
      }
    }
  ) { rowIdx ->
    BoxWithConstraints {
      fun canPlaceItemsInRow(rowIdx: Int): Boolean {
        val allItemsPlaced = gridRowIndexes.isNotEmpty() &&
          gridRowIndexes.last().endIndex == itemList.size - 1
        val isAlreadyPlacedRow = rowIdx < gridRowIndexes.size
        return isAlreadyPlacedRow || !allItemsPlaced
      }

      fun getTotalItems(startIdx: Int): Int {
        var cellIdx = 0
        var itemIdx = startIdx
        val numColumns = getNumColumns()
        while (cellIdx < numColumns && itemIdx < itemList.size) {
          val itemCellWidth = spanLookup(itemIdx)
          cellIdx += itemCellWidth
          if (cellIdx > numColumns) {
            break
          }
          itemIdx++
        }

        return itemIdx - startIdx
      }

      val cellWidth = getCellWidth(LocalDensity.current)

      if (!canPlaceItemsInRow(rowIdx)) {
        return@BoxWithConstraints
      }

      Row(modifier = Modifier.fillMaxWidth()) {
        // The first item from the list to be placed is the element after the last element placed
        // in the previous row.
        val startIdx = if (rowIdx == 0) 0 else gridRowIndexes[rowIdx - 1].endIndex + 1
        // itemIdx specifies the next item from the list that needs to be placed.
        var itemIdx = startIdx
        val totalItemsInRow = getTotalItems(startIdx)

        for (i in 0 until totalItemsInRow) {
          val itemCellWidth = spanLookup(itemIdx)

          gridRowIndexes.addOrUpdate(rowIdx, startIdx, itemIdx)
          Log.d("dbg", "rendering $itemIdx $totalItemsInRow")
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
    this[idx].apply {
      startIndex = startIdx
      endIndex = endIdx
    }
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