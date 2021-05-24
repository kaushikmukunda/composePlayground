package com.km.composePlayground.scroller

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

/**
 * This extension function allows a scroll by 1 item to occur every {@param delayDurationInMillis]}.
 * When the last item is reached, the scroller scrolls back to the first item.
 *
 * If a user triggers a manual scroll, the auto scrolling stops entirely.
 */
@Composable
fun LazyListState.enableAutoScroll(delayDurationInMillis: Long) {
  var hasScrolled = remember(this) { false }

  // Listen for a scroll event
  LaunchedEffect(this) {
    interactionSource.interactions.collect { interaction: Interaction ->
      if (interaction is DragInteraction) {
        hasScrolled = true
      }
    }
  }

  LaunchedEffect(this) {
    while (true) {
      delay(delayDurationInMillis)

      // Stop auto scroll if all items are visible or a scroll is detected.
      // AutoScroll can be enabled before or after a LazyList is composed. When scroll is enabled
      // before, the list is empty and allItemsVisible will return true. Hence, check for
      // totalItemsCount to be non-zero.
      if ((layoutInfo.totalItemsCount > 0 && areAllItemsFullyVisible()) || hasScrolled) {
        break
      }

      try {
        val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
        val isLastItemFullyVisible = isItemFullyVisible(layoutInfo.visibleItemsInfo.last())
        val isLastListElementVisible =
          lastVisibleItem.index == layoutInfo.totalItemsCount - 1 && isLastItemFullyVisible
        // If last item is visible, loop back to first item
        // TODO(b/182983176): Verify if compose scrolling fix fixes the jerky reverse scroll.
        val nextIdx = if (isLastListElementVisible) 0 else (firstVisibleItemIndex + 1)

        animateScrollToItem(nextIdx)
      } catch (e: CancellationException) {
        // No-op as the scrollToItem can be cancelled if the user scrolls manually
      }
    }
  }
}

private fun LazyListState.areAllItemsFullyVisible(): Boolean {
  if (layoutInfo.visibleItemsInfo.size < layoutInfo.totalItemsCount) {
    return false
  }

  return layoutInfo.visibleItemsInfo.fastAll { isItemFullyVisible(it) }
}

private fun LazyListState.isItemFullyVisible(itemInfo: LazyListItemInfo): Boolean {
  return itemInfo.offset >= layoutInfo.viewportStartOffset &&
    (itemInfo.offset + itemInfo.size) <= layoutInfo.viewportEndOffset
}
