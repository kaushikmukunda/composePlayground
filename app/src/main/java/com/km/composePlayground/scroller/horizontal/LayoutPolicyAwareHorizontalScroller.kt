package com.km.composePlayground.scroller.horizontal

import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastSumBy
import com.km.composePlayground.scroller.common.DecorationCalculator
import com.km.composePlayground.scroller.common.DecorationModifierCalculator
import com.km.composePlayground.scroller.common.EMPTY_DECORATION_CALCULATOR
import com.km.composePlayground.scroller.common.EMPTY_DECORATION_MODIFIER_CALCULATOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Horizontal scroller that is configurable by [HorizontalScrollerLayoutPolicy].
 *
 * @param uiModel The UiModel for rendering the scroller.
 * @property layoutPolicy that determines specifications for laying out this horizontal scroller.
 * @param config Configures additional behavior of the scroller like centering content.
 * @param mapper Transforms a UiModel to its Compose representation.
 * @param decorationCalculator Class which calculates the decorations to be applied to an item based
 * on its UiModel
 * @param decorationModifierCalculator Class which calculates the modifiers to be applied to the
 * item composables based on the decorations to be applied to the item
 */
@Composable
fun LayoutPolicyAwareHorizontalScrollerUi(
  uiModel: LayoutPolicyHorizontalScrollerComposeUiModel,
  layoutPolicy: HorizontalScrollerLayoutPolicy,
  config: HorizontalScrollerConfig = DEFAULT_HORIZONTAL_SCROLLER_CONFIG,
  mapper: UiModelComposableMapper,
  decorationCalculator: DecorationCalculator = EMPTY_DECORATION_CALCULATOR,
  decorationModifierCalculator: DecorationModifierCalculator = EMPTY_DECORATION_MODIFIER_CALCULATOR,
  containerModifier: Modifier = Modifier,
) {
  BoxWithConstraints {
    val itemWidth = layoutPolicy.calculateChildWidth(maxWidth)
    val scrollerHeightModifier =
      layoutPolicy.calculateScrollerHeight(itemWidth)?.let { containerModifier.height(it) }
        ?: containerModifier
    val itemWidthModifier = itemWidth?.let { Modifier.width(it) } ?: Modifier

    HorizontalScrollerUiInternal(
      uiModel = uiModel,
      mapper = mapper,
      config = config,
      decorationCalculator = decorationCalculator,
      decorationModifierCalculator = decorationModifierCalculator,
      contentPadding =
      PaddingValues(
        start = layoutPolicy.contentStartPadding,
        end = layoutPolicy.contentEndPadding
      ),
      containerModifier = scrollerHeightModifier,
      itemModifier = itemWidthModifier,
      itemWidth = itemWidth,
      flingBehavior =
      if (uiModel.enableSnapping) getFlingBehaviorForSnapping(uiModel.lazyListState)
      else ScrollableDefaults.flingBehavior()
    )
  }
}

@Composable
private fun getFlingBehaviorForSnapping(lazyListState: LazyListState): FlingBehavior {
  val coroutineScope = rememberCoroutineScope()
  return remember(lazyListState) { PlayFlingBehavior(lazyListState, coroutineScope) }
}

// TODO(b/182983176): Compose framework team needs to fix reverse scrolling which seems
// to be janky compared with the forward scroll.
/** Custom Fling Behavior to support snapping. */
private class PlayFlingBehavior(
  private val lazyListState: LazyListState,
  private val coroutineScope: CoroutineScope
) : FlingBehavior {
  // Animation spec is used to translate fling velocity to scroll distance. Friction multiplier of
  // 4.5f feels right.
  private val defaultDecayAnimationSpec =
    exponentialDecay<Float>(frictionMultiplier = 4.5f, absVelocityThreshold = 0f)

  override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
    val targetOffset = defaultDecayAnimationSpec.calculateTargetValue(0f, initialVelocity)
    val targetPosition = getTargetPosition(targetOffset, lazyListState)

    coroutineScope.launch { lazyListState.animateScrollToItem(targetPosition, 0) }

    return 0f
  }
}

// Max items to fling is one and half screen width.
private const val MAX_FLING_SCREEN_WIDTH_RATIO = 1.5f

// Snap to next item if this item has been scrolled out of screen by 40%.
private const val ITEM_SNAPPING_THRESHOLD_RATIO = 0.4f

private fun getTargetPosition(targetOffset: Float, listState: LazyListState): Int {
  if (listState.layoutInfo.totalItemsCount == 0) {
    return 0
  }

  val totalItems = listState.layoutInfo.totalItemsCount
  val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
  val visibleItemsWidth = visibleItemsInfo.fastSumBy { it.size }
  val averageItemWidth = visibleItemsWidth / visibleItemsInfo.size
  val firstItemOffset = visibleItemsInfo[0].offset
  val firstItemSize = visibleItemsInfo[0].size
  val firstVisibleItemIndex = listState.firstVisibleItemIndex
  val isForwardScroll = targetOffset > 0

  // Handle small fling that only moves one item
  if (abs(targetOffset) < firstItemSize) {
    // Scroll to next item if we have scrolled beyond 40% of the first item.
    val forwardThreshold = abs(firstItemOffset) > firstItemSize * ITEM_SNAPPING_THRESHOLD_RATIO
    val reverseThreshold =
      abs(firstItemOffset) < firstItemSize * (1 - ITEM_SNAPPING_THRESHOLD_RATIO)
    val hasCrossedItemThreshold =
      (isForwardScroll && forwardThreshold) || (!isForwardScroll && reverseThreshold)

    return if (hasCrossedItemThreshold) {
      if (isForwardScroll) firstVisibleItemIndex + 1 else firstVisibleItemIndex
    } else {
      if (isForwardScroll) firstVisibleItemIndex else firstVisibleItemIndex + 1
    }
  }

  // Calculate number of items to scroll for larger flings.
  val numItemsToScroll =
    Math.min(
      visibleItemsInfo.size * MAX_FLING_SCREEN_WIDTH_RATIO,
      abs(targetOffset) / averageItemWidth
    )
      .toInt()
  // Do not scroll any further than the max items
  val forwardScrollPosition = min(totalItems - 1, firstVisibleItemIndex + numItemsToScroll)
  // Do not scroll any further than the first position
  val reverseScrollPosition = max(0, (firstVisibleItemIndex - numItemsToScroll))

  return if (isForwardScroll) forwardScrollPosition else reverseScrollPosition
}
