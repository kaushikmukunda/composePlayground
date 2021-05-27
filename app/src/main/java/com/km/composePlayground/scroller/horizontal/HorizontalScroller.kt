package com.km.composePlayground.scroller.horizontal

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUi
import com.km.composePlayground.modifiers.fadingEdgeForeground
import com.km.composePlayground.scroller.common.Decoration
import com.km.composePlayground.scroller.common.DecorationCalculator
import com.km.composePlayground.scroller.common.DecorationModifierCalculator
import com.km.composePlayground.scroller.common.EMPTY_DECORATION_CALCULATOR
import com.km.composePlayground.scroller.common.EMPTY_DECORATION_MODIFIER_CALCULATOR
import com.km.composePlayground.scroller.common.ScrollerPositionInfo
import com.km.composePlayground.scroller.common.computeUiModelKeys
import kotlinx.coroutines.launch
import java.lang.Integer.min
import kotlin.math.abs
import kotlin.math.max


/** Action associated with Scrolling Ui. */
fun interface ScrollingUiAction {
  /**
   * Notifies listener to what position an item is currently visible
   */
  fun onItemRendered(position: Int)
}

/** Maps UiModels to composables. */
fun interface UiModelComposableMapper {

  fun map(uiModel: UiModel): @Composable() (Modifier) -> Unit
}

/** Describes padding to be applied to the start and end of the scoller content. */
class ScrollerPadding(val start: Dp, val end: Dp) {

  fun toPaddingValues() = PaddingValues(start = start, end = end)

}

/** Configure additional behavior of the scroller. */
interface HorizontalScrollerConfig {
  /** If the entire content were to fit in a single screen, should it be centered? */
  fun shouldCenterContent(): Boolean = false

  /** If we don't want to allow scrolling behavior, set this to true */
  fun shouldClipItemsOffScreen(): Boolean = false
}

/** Interface that denotes a decoration to be applied to an item in scroller ui. */
fun interface Decorator {

  /** Returns a Compose Ui consumable Modifier. */
  fun decorate(): Modifier
}

@Composable
private fun getFlingBehavior(lazyListState: LazyListState): FlingBehavior {
  return remember {
    object : FlingBehavior {
      override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val firstItemOffset = lazyListState.layoutInfo.visibleItemsInfo[0].offset
        val firstItemSize = lazyListState.layoutInfo.visibleItemsInfo[0].size

        Log.d(
          "dbg", "first visible ${lazyListState.firstVisibleItemIndex} scroll offset" +
            " ${lazyListState.firstVisibleItemScrollOffset} itemOffset $firstItemOffset" +
            " size $firstItemSize velocity: $initialVelocity"
        )
        // forward scroll +ve vel
        // backward scroll -ve vel

        val scrollDistance = if (abs(firstItemOffset) > firstItemSize / 2) {
          // Scroll to End position - 10%
          val targetOffset = firstItemSize.toFloat()
          Log.d("dbg", "scrolling to next element ${targetOffset - abs(firstItemOffset)}")
          (targetOffset - abs(firstItemOffset)) * if (initialVelocity > 0) 1 else -1
        } else {
          // Scroll to Start position - 10%
          val targetOffset = 0.1f * firstItemSize
          Log.d(
            "dbg",
            "scrolling to start of this element ${targetOffset + abs(firstItemOffset)}"
          )
          (targetOffset + abs(firstItemOffset)) * if (initialVelocity > 0) -1 else 1
        }

        Log.d("dbg", "scrollDistance $scrollDistance")
        scrollBy(scrollDistance)
        Log.d("dbg", "***** ******")
        return 0f
      }
    }
  }
}

@Composable
private fun getFlingBehavior2(lazyListState: LazyListState): FlingBehavior {
  val coroutineScope = rememberCoroutineScope()
  val defaultDecayAnimationSpec = remember(LocalDensity.current) {
    // Friction multiplier of 4.5f feels right.
    exponentialDecay<Float>(frictionMultiplier = 4.5f, absVelocityThreshold = 0f)
  }
  return remember {
    object : FlingBehavior {
      override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val targetOffset = defaultDecayAnimationSpec.calculateTargetValue(0f, initialVelocity)
        val targetPosition = getTargetPosition(targetOffset, lazyListState)

        coroutineScope.launch {
          lazyListState.animateScrollToItem(targetPosition, 0)
        }

        return 0f
      }
    }
  }
}

// Max items to fling is one and half screen width
private const val MAX_FLING_WIDTH = 1.5f

// Snap to next item if this item has been scrolled out of screen by 40%
private const val ITEM_SNAPPING_THRESHOLD = 0.4f

private fun getTargetPosition(targetOffset: Float, listState: LazyListState): Int {
  if (listState.layoutInfo.totalItemsCount == 0) {
    return 0
  }

  val totalItems = listState.layoutInfo.totalItemsCount
  val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
  val visibleItemsWidth = visibleItemsInfo.fastSumBy { it.size }
  val averageItemWidth = visibleItemsWidth / visibleItemsInfo.size
  val firstItemOffset = listState.layoutInfo.visibleItemsInfo[0].offset
  val firstItemSize = listState.layoutInfo.visibleItemsInfo[0].size
  val firstVisibleItemIndex = listState.firstVisibleItemIndex
  val isForwardScroll = targetOffset > 0

  // Handle small fling that only moves one item
  if (abs(targetOffset) < firstItemSize) {
    // Scroll to next item if we have scrolled beyond 40% of the first item.
    val forwardThreshold = abs(firstItemOffset) > firstItemSize * ITEM_SNAPPING_THRESHOLD
    val reverseThreshold = abs(firstItemOffset) < firstItemSize * (1 - ITEM_SNAPPING_THRESHOLD)
    val hasCrossedItemThreshold =
      (isForwardScroll && forwardThreshold) || (!isForwardScroll && reverseThreshold)

    return if (hasCrossedItemThreshold) {
      if (isForwardScroll) firstVisibleItemIndex + 1 else firstVisibleItemIndex
    } else {
      if (isForwardScroll) firstVisibleItemIndex else firstVisibleItemIndex + 1
    }
  }

  val numItemsToScroll =
    (Math.min(
      visibleItemsInfo.size * MAX_FLING_WIDTH,
      abs(targetOffset) / averageItemWidth
    )).toInt()
  val forwardScrollPosition = min(totalItems - 1, firstVisibleItemIndex + numItemsToScroll)
  val reverseScrollPosition = max(0, (firstVisibleItemIndex - numItemsToScroll))

  Log.d("dbg", "$numItemsToScroll fwd:$forwardScrollPosition rev:$reverseScrollPosition")
  return if (isForwardScroll) forwardScrollPosition else reverseScrollPosition
}

// Corresponds to the fading_edge_overdraw resource http://shortn/_8GBaeCSedi
private val DEFAULT_FADING_EDGE_WIDTH = 10.dp

private val NO_CONTENT_PADDING = PaddingValues(0.dp)

// Delay animation of the new item if the old item is being animated out
private const val ANIM_DELAY_MS = 100

/** A default implementation of the HorizontalScrollerConfig. */
object DEFAULT_HORIZONTAL_SCROLLER_CONFIG : HorizontalScrollerConfig

/**
 * Displays horizontally scrollable content.
 *
 * @param uiModel The UiModel for rendering the scroller.
 * @param config Configures additional behavior of the scroller like centering content.
 * @param mapper Transforms a UiModel to its Compose representation.
 * @param decorationCalculator Class which calculates the decorations to be applied to an item based
 * on its UiModel
 * @param decorationModifierCalculator Class which calculates the modifiers to be applied to the
 * item composables based on the decorations to be applied to the item
 * @param contentPadding a padding around the whole content. This will add padding for the content
 * after it has been clipped, which is not possible via [modifier] param. Note that it is **not** a
 * padding applied for each item's content.
 * @param containerModifier Optional modifier to be applied to the scroll container.
 */
@Composable
fun HorizontalScrollerUi(
  uiModel: HorizontalScrollerComposeUiModel,
  config: HorizontalScrollerConfig = DEFAULT_HORIZONTAL_SCROLLER_CONFIG,
  mapper: UiModelComposableMapper,
  decorationCalculator: DecorationCalculator = EMPTY_DECORATION_CALCULATOR,
  decorationModifierCalculator: DecorationModifierCalculator = EMPTY_DECORATION_MODIFIER_CALCULATOR,
  contentPadding: PaddingValues = NO_CONTENT_PADDING,
  containerModifier: Modifier = Modifier,
) {
  BoxWithConstraints {
    HorizontalScrollerUiInternal(
      uiModel = uiModel,
      config = config,
      mapper = mapper,
      decorationCalculator = decorationCalculator,
      decorationModifierCalculator = decorationModifierCalculator,
      contentPadding = contentPadding,
      containerModifier = containerModifier,
    )
  }
}

/**
 * This API is to be only used by Scrollers that extend functionality like the
 * [LayoutPolicyAwareScroller]. Any modification to the item or the container should be applied via
 * the additional Modifier params - containerModifier, itemModifier
 */
@Composable
fun BoxWithConstraintsScope.HorizontalScrollerUiInternal(
  uiModel: HorizontalScrollerComposeUiModel,
  config: HorizontalScrollerConfig = DEFAULT_HORIZONTAL_SCROLLER_CONFIG,
  mapper: UiModelComposableMapper,
  decorationCalculator: DecorationCalculator,
  decorationModifierCalculator: DecorationModifierCalculator,
  contentPadding: PaddingValues = NO_CONTENT_PADDING,
  containerModifier: Modifier = Modifier,
  itemModifier: Modifier = Modifier,
  itemWidth: Dp? = null,
  flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
) {
//  tracePrefix.trace("UiInternal") {
  UniformUi(uiModel) { content ->
    val alignment = if (config.shouldCenterContent()) Alignment.Center else Alignment.TopStart
    Box(
      contentAlignment = alignment,
      modifier =
      containerModifier
        .fillMaxWidth()
        .fadingEdge(boxWithConstraintsScope = this, DEFAULT_FADING_EDGE_WIDTH)
    ) {
      // The initial set of items should not be animated. Initialize the maxIdxRendered to
      // the number of items displayed in row.
      val numItemsInRow =
        itemWidth?.let { (this@HorizontalScrollerUiInternal.maxWidth / itemWidth).toInt() - 1 }
          ?: 0
      val itemsToRender =
        if (config.shouldClipItemsOffScreen()) {
          content.items.subList(0, (numItemsInRow + 1).coerceAtMost(content.items.size))
        } else {
          content.items
        }
      val itemKeys =
        remember(itemsToRender) {
          computeUiModelKeys(itemsToRender, uiModel.hashCode().toString())
        }

      LazyRow(
        state = uiModel.lazyListState,
        contentPadding = contentPadding,
        flingBehavior = flingBehavior
      ) {
        itemsIndexed(items = itemsToRender, key = { index, _ -> itemKeys[index] }) { index, item
          ->
          RenderItemAtIndex(
            uiModel.scrollerAnimationState,
            decorationModifierCalculator,
            decorationCalculator,
            mapper,
            index,
            itemsToRender.size,
            item,
            itemModifier
          )

          SideEffect {
            content.uiAction.onItemRendered(index)
            uiModel.scrollerAnimationState.updateState(index, item)
          }
        }
      }
    }
  }
}

@Composable
private fun RenderItemAtIndex(
  scrollerAnimationState: ScrollerAnimationState,
  decorationModifierCalculator: DecorationModifierCalculator,
  decorationCalculator: DecorationCalculator,
  mapper: UiModelComposableMapper,
  index: Int,
  totalItems: Int,
  item: UiModel,
  itemModifier: Modifier
) //=
  /*tracePrefix.trace("RenderItem idx$index")*/ {
  val oldItem = scrollerAnimationState.items.getOrNull(index)
  val itemConflict = oldItem != null && oldItem != item
  val newItemDecorationModifier =
    decorationModifierCalculator.getModifierForDecorations(
      decorationCalculator.getDecorationsForUiModel(
        uiModel = item,
        ScrollerPositionInfo(index == 0, index == totalItems - 1)
      )
    )

  // Check if there existed a different item at this index. If yes, animate out that item and
  // animate in the new item. Encapsulate these inside a stacking composable (Box).
  if (itemConflict) {
    val oldItemDecorationModifier =
      decorationModifierCalculator.getModifierForDecorations(
        decorationCalculator.getDecorationsForUiModel(
          uiModel = requireNotNull(oldItem),
          ScrollerPositionInfo(index == 0, index == scrollerAnimationState.items.size - 1)
        )
      )
    Box {
      RenderOldItem(mapper, oldItem, oldItemDecorationModifier.then(itemModifier))

      RenderNewItem(
        mapper,
        item,
        newItemDecorationModifier.then(itemModifier),
        index,
        itemConflict,
        scrollerAnimationState
      )
    }
  } else {
    RenderNewItem(
      mapper,
      item,
      newItemDecorationModifier.then(itemModifier),
      index,
      itemConflict,
      scrollerAnimationState
    )
  }
}

@Composable
private fun RenderOldItem(mapper: UiModelComposableMapper, oldItem: UiModel, modifier: Modifier) {
  RenderItemWithAnimation(
    mapper = mapper,
    modifier = modifier,
    item = oldItem,
    itemVisible = false,
    initiallyVisible = true
  )
}

@Composable
private fun RenderNewItem(
  mapper: UiModelComposableMapper,
  item: UiModel,
  modifier: Modifier,
  index: Int,
  itemConflict: Boolean,
  scrollerAnimationState: ScrollerAnimationState
) {
  RenderItemWithAnimation(
    mapper = mapper,
    modifier = modifier,
    item = item,
    itemVisible = true,
    initiallyVisible = if (itemConflict) false else index <= scrollerAnimationState.maxIdxRendered,
    shouldDelay = itemConflict
  )
}

@ExperimentalAnimationApi
private val FadeOutTransition = fadeOut(animationSpec = tween(easing = LinearEasing))

@ExperimentalAnimationApi
private val FadeInInstantTransition = fadeIn(animationSpec = tween(easing = LinearEasing))

@ExperimentalAnimationApi
private val FadeInDelayedTransition =
  fadeIn(animationSpec = tween(easing = LinearEasing, delayMillis = ANIM_DELAY_MS))

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RenderItemWithAnimation(
  mapper: UiModelComposableMapper,
  modifier: Modifier,
  item: UiModel,
  itemVisible: Boolean,
  initiallyVisible: Boolean,
  shouldDelay: Boolean = false,
) {
  AnimatedVisibility(
    visible = itemVisible,
    enter = if (shouldDelay) FadeInDelayedTransition else FadeInInstantTransition,
    exit = FadeOutTransition,
    initiallyVisible = initiallyVisible
  ) { mapper.map(uiModel = item).invoke(modifier) }
}

private fun Modifier.fadingEdge(
  boxWithConstraintsScope: BoxWithConstraintsScope,
  fadingEdgeWidth: Dp
): Modifier = composed {
  if (fadingEdgeWidth <= 0.dp) return@composed this

  // Only apply a fading edge to scrollers that are not full screen width.
  val screenWidth = LocalContext.current.resources.configuration.screenWidthDp
  val scrollerWidth = boxWithConstraintsScope.maxWidth
  if (screenWidth <= scrollerWidth.value) this
  else {
    val ratio = fadingEdgeWidth / scrollerWidth
    // Existing view implementation uses White
    // See GradientColor#SOLID (http://shortn/_LlMjcA8evY)
    this.fadingEdgeForeground(color = Color.White, leftRatio = ratio, rightRatio = ratio)
  }
}

/** A default implementation of the HorizontalScrollerConfig. */
val DefaultHorizontalScrollerConfig = object : HorizontalScrollerConfig {
  override fun shouldCenterContent() = false

}

val NoDecoration = DecorationCalculator { _, _ -> emptyList() }

class DividerDecorator(
  private val sidePadding: Dp = 16.dp,
  private val verticalPadding: Dp = 8.dp
) : Decoration {

  @SuppressLint("ModifierFactoryExtensionFunction")
  fun decorate(): Modifier {
    return Modifier
      .padding(start = sidePadding)
      .drawBehind {
        val top = verticalPadding.toPx()
        val bot = size.height - verticalPadding.toPx()
        val start = -sidePadding.toPx() / 2

        drawLine(
          color = Color.Green,
          start = Offset(start, top),
          end = Offset(start, bot),
          strokeWidth = 1.dp.toPx()
        )
      }
  }
}

val StandardDecorationResolver = DecorationModifierCalculator { decorators ->
  var modifier: Modifier = Modifier
  decorators.forEach { decorator ->
    modifier = modifier
  }
  modifier
}