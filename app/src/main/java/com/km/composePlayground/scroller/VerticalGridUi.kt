package com.km.composePlayground.scroller

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUi

@Composable
fun StaticGridUi(model: StaticGridUiModel,
                 modifier: Modifier = Modifier) = UniformUi(model) { content ->
  BoxWithConstraints {
    val cellWidth = 100.dp

    VerticalGridUi(
      itemList = content.itemList,
      cellWidth = cellWidth,
      numColumns = content.spanCount,
      spanLookup = content.spanLookup)
  }
}


@Composable
fun DynamicGridUi(model: DynamicGridUiModel,
                  modifier: Modifier = Modifier) = UniformUi(model) { content ->
  BoxWithConstraints {
    val cellWidth = with(AmbientDensity.current) { content.desiredCellSize.toDp() }
    val numColumns = 4 //maxWidth / cellWidth

    VerticalGridUi(
      itemList = content.itemList,
      cellWidth = cellWidth,
      numColumns = numColumns.toInt(),
      spanLookup = content.spanLookup)
  }
}

@Composable
private fun VerticalGridUi(itemList: List<UiModel>,
                           cellWidth: Dp,
                           numColumns: Int,
                           spanLookup: ItemSpanLookup,
                           modifier: Modifier = Modifier) {
  val gridRowIndexes = remember { mutableListOf<GridRowIndexes>() }

  fun canPlaceItemsInRow(rowIdx: Int): Boolean {
    val allItemsPlaced = gridRowIndexes.isNotEmpty() &&
      gridRowIndexes.last().endIdx == itemList.size - 1
    val isAlreadyPlacedRow = rowIdx < gridRowIndexes.size
    return isAlreadyPlacedRow || !allItemsPlaced
  }


  fun isEndOfList(itemIdx: Int) = itemIdx >= itemList.size

  LazyColumn(modifier = Modifier.fillMaxSize()) {
    // No efficient way to compute actual number of rows
    items(itemList.size) { rowIdx ->
      if (!canPlaceItemsInRow(rowIdx)) return@items

      Row(modifier = Modifier.fillMaxWidth()) {
        // cellIdx specifies the next cell to be placed in the grid.
        var cellIdx = 0
        // The first item from the list to be placed is the element after the last element placed
        // in the previous row.
        val startIdx = if (rowIdx == 0) 0 else gridRowIndexes[rowIdx - 1].endIdx + 1
        // itemIdx specifies the next item from the list that needs to be placed.
        var itemIdx = startIdx

        while (cellIdx < numColumns && !isEndOfList(itemIdx)) {
          val itemCellWidth = spanLookup(itemIdx)
          cellIdx += itemCellWidth

          // Not enough space available for this item in this Row, finalize Row
          if (cellIdx > numColumns) {
            break
          }

          gridRowIndexes.addOrUpdate(rowIdx, startIdx, itemIdx)
//          Log.d("dbg", "rendering $itemIdx")
          TextItem(
            itemList[itemIdx++] as TextModel,
            modifier = modifier
              .width(cellWidth.times(itemCellWidth))
              .padding(end = 8.dp, bottom = 8.dp))
        }
      }
    }
  }
}

private fun MutableList<GridRowIndexes>.addOrUpdate(idx: Int, startIdx: Int, endIdx: Int) {
  if (this.isEmpty() || this.size <= idx) {
    this.add(idx, GridRowIndexes(startIdx, endIdx))
  } else {
    this[idx].stIdx = startIdx
    this[idx].endIdx = endIdx
  }
}

private class GridRowIndexes(var stIdx: Int, var endIdx: Int)


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