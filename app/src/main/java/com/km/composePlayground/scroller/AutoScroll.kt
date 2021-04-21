package com.km.composePlayground.scroller

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.km.composePlayground.modifiers.rememberState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

@Composable
fun LazyListState.enableAutoScroll(delayDurationMs: Long) {
  // All items visible, do not scroll
  if (layoutInfo.visibleItemsInfo.size == layoutInfo.totalItemsCount) {
    return
  }

  var hasScrolled by rememberState { false }

  // Listen for a scroll event
  LaunchedEffect(this) {
    interactionSource.interactions.collect { interaction ->
      if (interaction is DragInteraction) {
        hasScrolled = true
      }
    }
  }

  LaunchedEffect(this) {
    while (true) {
      delay(delayDurationMs)

      // Stop auto scroll if a scroll is detected
      if (hasScrolled) {
        break
      }

      try {
        val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
        val isLastItemFullyVisible =
          (lastVisibleItem.offset + lastVisibleItem.size) <= layoutInfo.viewportEndOffset
        val lastElementVisible = lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
          isLastItemFullyVisible
        // If last item is visible, loop back to first item
        val nextIdx = if (lastElementVisible) 0 else (firstVisibleItemIndex + 1)

        animateScrollToItem(nextIdx)
      } catch (e: CancellationException) {
        // No-op as the scrollToItem can be cancelled if the user scrolls manually
      }
    }
  }
}