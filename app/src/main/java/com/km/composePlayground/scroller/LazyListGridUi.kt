package com.km.composePlayground.scroller

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel

fun LazyListScope.StaticGridUi(
    model: StaticGridUiModel,
    mapper: UiModelComposableMapper,
    decorationResolver: DecorationResolver) {
  val content = model.content.value

  VerticalGridUi(
      itemList = content.itemList,
      mapper = mapper,
      decorationResolver = decorationResolver,
      getCellWidth = { maxWidth / content.spanCount },
      getNumColumns = { content.spanCount },
      spanLookup = content.spanLookup
  )
}

fun LazyListScope.DynamicGridUi(
    model: DynamicGridUiModel,
    mapper: UiModelComposableMapper,
    decorationResolver: DecorationResolver) {
  val content = model.content.value

  VerticalGridUi(
      itemList = content.itemList,
      mapper = mapper,
      decorationResolver = decorationResolver,
      getCellWidth = { density -> with(density) { content.desiredCellSize.toDp() } },
      getNumColumns = { constraints.maxWidth / content.desiredCellSize },
      spanLookup = content.spanLookup,
  )
}

private fun LazyListScope.VerticalGridUi(
    itemList: List<UiModel>,
    mapper: UiModelComposableMapper,
    decorationResolver: DecorationResolver,
    getCellWidth: BoxWithConstraintsScope.(density: Density) -> Dp,
    getNumColumns: BoxWithConstraintsScope.() -> Int,
    spanLookup: ItemSpanLookup) {

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

      val cellWidth = getCellWidth(AmbientDensity.current)
      val numColumns = getNumColumns()

      if (!canPlaceItemsInRow(rowIdx)) {
        return@BoxWithConstraints
      }

      Row(modifier = Modifier.fillMaxWidth()) {
        // cellIdx specifies the next cell to be placed in the grid.
        var cellIdx = 0
        // The first item from the list to be placed is the element after the last element placed
        // in the previous row.
        val startIdx = if (rowIdx == 0) 0 else gridRowIndexes[rowIdx - 1].endIndex + 1
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
          Log.d("dbg", "rendering $itemIdx")
          val item = itemList[itemIdx++]
          renderItem(
              item,
              mapper,
              Modifier
                  .width(cellWidth.times(itemCellWidth))
                  .then(decorationResolver.getDecorationModifier(item))
          )
        }
      }
    }
  }
}

@Composable
internal fun renderItem(item: UiModel,
                       mapper: UiModelComposableMapper,
                       modifier: Modifier) {
  mapper.map(item).invoke(modifier)
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