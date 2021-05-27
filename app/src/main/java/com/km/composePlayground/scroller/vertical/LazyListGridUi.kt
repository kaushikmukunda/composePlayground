package com.km.composePlayground.scroller.vertical

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.scroller.DynamicGridUiModel
import com.km.composePlayground.scroller.common.GridSectionPositionInfo
import com.km.composePlayground.scroller.common.ItemSpanLookup
import com.km.composePlayground.scroller.common.LinearSectionPositionInfo
import com.km.composePlayground.scroller.common.computeUiModelKeys
import com.km.composePlayground.scroller.horizontal.ScrollingUiAction


/**
 * Renders a group (section) of UiModels to be arranged in the form of a grid. The specifications of
 * the grid are static i.e. they don't change as a result of resizing of the vertical scroller.
 */
internal fun LazyListScope.StaticGridUi(
  model: StaticGridUiModel,
  elementRenderer: ElementRenderer
) //=
  /*tracePrefix.trace("StaticGridUi")*/ {
  val content = requireNotNull(model.content.value)

  VerticalGridUi(
    itemList = content.itemList,
    elementRenderer = elementRenderer,
    getCellWidth = { maxWidth / content.spanCount },
    getNumColumns = { content.spanCount },
    spanLookup = content.spanLookup,
    scrollingUiAction = content.scrollingUiAction,
    identity = content.dataId,
//      parentLoggingData = model.loggingGroupVeMetadata
  )
}

/**
 * Renders a group (section) of UiModels to be arranged in the form of a grid in the vertical
 * scroller ui. Unlike [StaticGridUiModelContent] which has a fixed span count, the span count of
 * DynamicGridUiModelContent is calculated at render time.
 */
internal fun LazyListScope.DynamicGridUi(
  model: DynamicGridUiModel,
  elementRenderer: ElementRenderer
) //=
  /*tracePrefix.trace("DynamicGridUi")*/ {
  val content = requireNotNull(model.content.value)

  VerticalGridUi(
    itemList = content.itemList,
    elementRenderer = elementRenderer,
    getCellWidth = { density -> with(density) { content.desiredCellSize.toDp() } },
    getNumColumns = { constraints.maxWidth / content.desiredCellSize },
    spanLookup = content.spanLookup,
    scrollingUiAction = content.scrollingUiAction,
    identity = content.dataId,
//    parentLoggingData = model.loggingGroupVeMetadata
  )
}

/**
 * Renders a group of UiModels linearly within a LazyList (Row or Column). To be used in the context
 * of a [VerticalScrollerUi] which supports grid and linear arrangements alongside each other.
 */
internal fun LazyListScope.LinearUi(model: SectionUiModel, elementRenderer: ElementRenderer) //=
  /*tracePrefix.trace("LinearUi") */ {
  val content = requireNotNull(model.content.value)
  val itemKeys = computeUiModelKeys(content.itemList, content.dataId)
  val posInfo = LinearSectionPositionInfo(false, false)

  Log.d("dbg", "linearUi outer ")

  itemsIndexed(items = content.itemList /*key = { idx, _ -> itemKeys[idx] }*/) { idx, item ->
//      val positionInfo = LinearSectionPositionInfo(idx == 0, idx == content.itemList.size - 1)
    Log.d("dbg", "vscroller linearUi $item key ${itemKeys[idx]}")
    elementRenderer.render(uiModel = item, positionInfo = posInfo, modifier = Modifier)

//      SideEffect { content.scrollingUiAction.onItemRendered(idx) }
  }
}

private fun LazyListScope.VerticalGridUi(
  itemList: List<UiModel>,
  elementRenderer: ElementRenderer,
  getCellWidth: BoxWithConstraintsScope.(density: Density) -> Dp,
  getNumColumns: BoxWithConstraintsScope.() -> Int,
  spanLookup: ItemSpanLookup,
  scrollingUiAction: ScrollingUiAction,
  identity: String,
//  parentLoggingData: VeMetadata?
) {
  // Keeps track of start and end indexes of items from itemList that are placed in each Row.
  val gridRowIndexes = mutableListOf<GridRowIndexes>()

  fun getKeyForItem(index: Int): String {
    return if (index > 0) {
      if (index > gridRowIndexes.size) {
        "${identity}_emptyrow_$index"
      } else {
        // Include itemList hashcode to create a new key when the list changes
        "${identity}_${itemList.hashCode()}_row_${gridRowIndexes[index - 1].endIndex}"
      }
    } else {
      "${identity}_${itemList.hashCode()}_row_000.}"
    }
  }

  // No efficient way to compute actual number of rows, assume as many as items. This should be OK
  // as no redundant views are actually generated.
  items(count = itemList.size, key = ::getKeyForItem) { rowIdx ->
    BoxWithConstraints {
      fun canPlaceItemsInRow(rowIdx: Int): Boolean {
        val allItemsPlaced =
          gridRowIndexes.isNotEmpty() && gridRowIndexes.last().endIndex == itemList.size - 1
        val isAlreadyPlacedRow = rowIdx < gridRowIndexes.size
        return isAlreadyPlacedRow || !allItemsPlaced
      }

      fun getTotalItemsInRow(firstItemIdx: Int): Int {
        // cellIdx specifies the next cell to be populated in the row.
        var cellIdx = 0

        var itemIdx = firstItemIdx
        val numColumns = getNumColumns()
        while (cellIdx < numColumns && itemIdx < itemList.size) {
          val itemCellWidth = spanLookup(itemIdx)
          cellIdx += itemCellWidth
          // Not enough space available for this item in this Row, finalize Row
          if (cellIdx > numColumns) {
            break
          }
          itemIdx++
        }

        return itemIdx - firstItemIdx
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

        val totalItemsInRow = getTotalItemsInRow(startIdx)
        for (idx in 0 until totalItemsInRow) {
          val itemCellWidth = spanLookup(itemIdx)

          gridRowIndexes.addOrUpdate(rowIdx, startIdx, itemIdx)
          val item = itemList[itemIdx++]
          val positionInfo = GridSectionPositionInfo(idx == 0, idx == totalItemsInRow - 1)
          elementRenderer.render(
            uiModel = item,
            positionInfo = positionInfo,
            modifier = Modifier.width(cellWidth.times(itemCellWidth)),
//            parentLoggingData = parentLoggingData
          )

          SideEffect { scrollingUiAction.onItemRendered(itemIdx) }
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
