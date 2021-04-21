package com.km.composePlayground.scroller

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUi
import com.km.composePlayground.base.UniformUiModel
import com.km.composePlayground.modifiers.fadingEdgeForeground
import com.km.composePlayground.modifiers.rememberState
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

/** Configure the layout parameters for the scroller. */
interface HorizontalScrollerLayoutPolicy {

  /**
   * Given the width of the horizontal scroller in density-pixels, returns the width of a child.
   * The returned width will be equally applied to all children.
   *
   * @param scope Provides the layout constraints of the scroller.
   */
  fun getChildWidth(scope: BoxWithConstraintsScope): Dp

  /**
   * Given the width of a child, returns the desired height of the horizontal scroller or null if
   * this height is not a function of the child width, in which case the horizontal scroller height
   * will not be modified.
   */
  fun getScrollerHeight(itemWidth: Dp): Dp?

  /** The padding to be set on the scroller. */
  fun getContentPadding(): ScrollerPadding
}

/** Configure additional behavior of the scroller. */
interface HorizontalScrollerConfig {
  /** If the entire content were to fit in a single screen, should it be centered? */
  fun shouldCenterContent(): Boolean

  /** Width of the fading edge that might be drawn at the edges of the scroller. */
  fun getFadingEdgeWidth(): Dp

  /** Enable snapping of content cards to its edges. */
  fun shouldEnableSnapping(): Boolean
}

/** Interface that denotes a decoration to be applied to an item in scroller ui. */
fun interface Decorator {

  /** Returns a Compose Ui consumable Modifier. */
  fun decorate(): Modifier
}

/** Configure item decoration within the scroller. */
fun interface DecorationCalculator {

  /** Determine the decorations for a given item represented by the UiModel. */
  fun getDecorationsForUiModel(uiModel: UiModel): List<Decorator>

}

/** Placeholder calculator that provides no decorations. */
val EMPTY_DECORATION_CALCULATOR: DecorationCalculator = DecorationCalculator { emptyList() }

/**
 * Resolves the list of decorators to a Compose Ui consumable Modifier.
 *
 * Modifier chaining is not associative. When custom chaining of
 * a group of Modifiers is required, this interface should be overridden.
 */
fun interface DecorationModifierCalculator {

  fun getModifierForDecorations(decorationList: List<Decorator>): Modifier

}

/** Placeholder calculator that provides no decorations. */
val EMPTY_DECORATION_MODIFIER_CALCULATOR: DecorationModifierCalculator =
  DecorationModifierCalculator { Modifier }

/**
 * UiContent for the horizontal scroller.
 *
 * @property layoutPolicy Determine the layout and size of the item.
 * @property uiAction Ui actions supported by the scroller.
 * @property items The UiModels that are to be rendered in a list.
 * @property footeritem Optional The uiModel that ought to be appended to the list. Usually used
 *   for loading or error scenarios.
 */
@Stable
class HorizontalScrollerUiContent(
  val uiAction: ScrollingUiAction,
  val items: List<UiModel>,
)

/**
 * UiModel that establishes contract with HorizontalScrollerUi.
 *
 * @property mapper Maps UiModels to composables.
 */
@Stable
class ScrollerUiModel(
  horizontalScrollerUiContent: HorizontalScrollerUiContent,
) : UniformUiModel<HorizontalScrollerUiContent> {
  override val content = mutableStateOf(horizontalScrollerUiContent)
}

/**
 * Keeps track of the state of the scroller relevant to item animation.
 *
 * @property items The list of UiModels backing the Scroller.
 * @property maxIdxRendered The max index of the rendered model in the scroller. This is to ensure
 *   that only new items are animated.
 */
private class ScrollerAnimationState(
  initial: List<UiModel>,
  var maxIdxRendered: Int = 0
) {

  val items: MutableList<UiModel> = initial.toMutableList()

  fun updateState(index: Int, item: UiModel) {
    maxIdxRendered = max(maxIdxRendered, index)

    // If the new item does not match the backing list, replace or add to the list.
    // This occurs after the UI renders the replacement animation, this is safe to do.
    if (items.getOrNull(index) != item) {
      if (items.size == index) {
        items.add(item)
      } else {
        items[index] = item
      }
    }
  }
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

/**
 * Displays horizontally scrollable content.
 *
 * @param uiModel The UiModel for rendering the scroller.
 * @param layoutPolicy The policy to apply to the scroller.
 * @param config Configures additional behavior of the scroller like snapping.
 * @param mapper Transforms a UiModel to its Compose representation.
 * @param decorationCalculator The decoration to be applied to the individual items in the scroller.
 * @param decorationResolver Optional resolver that transforms decoration to a Modifier.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HorizontalScrollerUi(
  lazyListState: LazyListState = rememberLazyListState(),
  uiModel: ScrollerUiModel,
  layoutPolicy: HorizontalScrollerLayoutPolicy,
  config: HorizontalScrollerConfig = DefaultHorizontalScrollerConfig,
  mapper: UiModelComposableMapper,
  decorationCalculator: DecorationCalculator = NoDecoration,
  decorationResolver: DecorationModifierCalculator = StandardDecorationResolver
) = UniformUi(uiModel) { content ->
  BoxWithConstraints {
    val alignment =
      if (config.shouldCenterContent()) Alignment.Center else Alignment.TopStart
    val itemWidth = layoutPolicy.getChildWidth(this)
    val scrollerHeightModifier = layoutPolicy.getScrollerHeight(itemWidth)
      ?.let { Modifier.height(it) } ?: Modifier

    Box(
      contentAlignment = alignment,
      modifier = Modifier
        .fillMaxWidth()
        .then(scrollerHeightModifier)
        .fadingEdge(this, config.getFadingEdgeWidth())
    ) {
      // The initial set of items should not be animated. Initialize the maxIdxRendered to
      // the number of items displayed in row.
      val numItemsInRow = (this@BoxWithConstraints.maxWidth / itemWidth).toInt() - 1
      val scrollerAnimationState by rememberState {
        ScrollerAnimationState(maxIdxRendered = numItemsInRow, initial = content.items)
      }

      LazyRow(
        contentPadding = layoutPolicy.getContentPadding().toPaddingValues(),
        state = lazyListState,
        flingBehavior = getFlingBehavior2(lazyListState)
      ) {
        itemsIndexed(content.items) { index, item ->
          RenderItemAtIndex(
            scrollerAnimationState,
            index,
            item,
            decorationResolver,
            decorationCalculator,
            mapper,
            itemWidth
          )

          SideEffect {
            content.uiAction.onItemRendered(index)
//            Log.d("dbg", "rendering $index maxIdx ${scrollerAnimationState.maxIdxRendered}")
          }

          scrollerAnimationState.updateState(index, item)
        }
      }
    }
  }
}

@Composable
private fun RenderItemAtIndex(
  scrollerAnimationState: ScrollerAnimationState,
  index: Int,
  item: UiModel,
  decorationModifierCalculator: DecorationModifierCalculator,
  decorationCalculator: DecorationCalculator,
  mapper: UiModelComposableMapper,
  itemWidth: Dp
) {
  val oldItem = scrollerAnimationState.items.getOrNull(index)
  val itemConflict = oldItem != null && oldItem != item
  // Check if there existed a different item at this index. If yes, animate out that item and
  // animate in the new item. Encapsulate these inside a stacking composable (Box).
  if (itemConflict) {
//    Log.d("dbg", "inConflict $index $oldItem $item")
    Box {
      RenderOldItem(
        decorationModifierCalculator,
        decorationCalculator,
        oldItem!!,
        mapper,
        itemWidth
      )

      RenderNewItem(
        decorationModifierCalculator,
        decorationCalculator,
        item,
        index,
        mapper,
        itemWidth,
        itemConflict,
        scrollerAnimationState
      )
    }
  } else {
    RenderNewItem(
      decorationModifierCalculator,
      decorationCalculator,
      item,
      index,
      mapper,
      itemWidth,
      itemConflict,
      scrollerAnimationState
    )
  }
}

@Composable
private fun RenderOldItem(
  decorationResolver: DecorationModifierCalculator,
  decorationCalculator: DecorationCalculator,
  oldItem: UiModel,
  mapper: UiModelComposableMapper,
  itemWidth: Dp
) {
  val decorationModifier = decorationResolver.getModifierForDecorations(
    decorationCalculator.getDecorationsForUiModel(uiModel = oldItem)
  )

  RenderItemWithAnimation(
    mapper = mapper,
    modifier = decorationModifier
      .width(itemWidth)
      .fillMaxHeight(),
    item = oldItem,
    itemVisible = false,
    initiallyVisible = true
  )
}

@Composable
private fun RenderNewItem(
  decorationResolver: DecorationModifierCalculator,
  decorationCalculator: DecorationCalculator,
  item: UiModel,
  index: Int,
  mapper: UiModelComposableMapper,
  itemWidth: Dp,
  itemConflict: Boolean,
  scrollerAnimationState: ScrollerAnimationState
) {
  val decorationModifier = decorationResolver.getModifierForDecorations(
    decorationCalculator.getDecorationsForUiModel(uiModel = item)
  )

  RenderItemWithAnimation(
    mapper,
    decorationModifier
      .width(itemWidth)
      .fillMaxHeight(),
    item = item,
    itemVisible = true,
    initiallyVisible =
    if (itemConflict) !itemConflict else index <= scrollerAnimationState.maxIdxRendered,
    shouldDelay = itemConflict
  )
}

private const val ANIM_DELAY_MS = 100

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("ModifierParameter")
@Composable
private fun RenderItemWithAnimation(
  mapper: UiModelComposableMapper,
  modifier: Modifier,
  item: UiModel,
  itemVisible: Boolean,
  initiallyVisible: Boolean,
  shouldDelay: Boolean = false,
) {
  val delayMillis = if (shouldDelay) ANIM_DELAY_MS else 0
  AnimatedVisibility(
    visible = itemVisible,
    enter = fadeIn(animationSpec = tween(easing = LinearEasing, delayMillis = delayMillis)),
    exit = fadeOut(animationSpec = tween(easing = LinearEasing)),
    initiallyVisible = initiallyVisible
  ) {
    mapper.map(uiModel = item).invoke(modifier)
  }
}

private fun Modifier.fadingEdge(
  withConstraintsScope: BoxWithConstraintsScope,
  fadingEdgeWidth: Dp
): Modifier = composed {
  val screenWidth = LocalContext.current.resources.configuration.screenWidthDp
  val scrollerWidth = withConstraintsScope.maxWidth
  if (screenWidth < scrollerWidth.value) this
  else {
    val ratio = fadingEdgeWidth / scrollerWidth
    this.fadingEdgeForeground(color = Color.White, leftRatio = ratio, rightRatio = ratio)
  }
}

/** A default implementation of the HorizontalScrollerConfig. */
val DefaultHorizontalScrollerConfig = object : HorizontalScrollerConfig {
  override fun shouldCenterContent() = true

  override fun getFadingEdgeWidth(): Dp = 10.dp

  override fun shouldEnableSnapping() = true

}

/** A simple implementation of fixed layout policy. */
class FixedLayoutPolicy(
  val desiredItemWidth: Dp,
  val childPeekAmount: Float = 0.1f,
  val baseWidthMultipler: Float = 1f
) :
  HorizontalScrollerLayoutPolicy {

  override fun getChildWidth(scope: BoxWithConstraintsScope): Dp {
    val widthForChildren =
      (scope.maxWidth - getContentPadding().start - getContentPadding().end)
    return Dp(
      CardCountHelper.getUnitCardWidth(
        (desiredItemWidth.value * baseWidthMultipler).toInt(),
        widthForChildren.value.toInt(),
        childPeekAmount
      ).toFloat()
    )

  }

  override fun getScrollerHeight(itemWidth: Dp): Dp? {
    return itemWidth.times(9 / 16f)
  }

  override fun getContentPadding() = ScrollerPadding(start = 16.dp, end = 16.dp)
}

val NoDecoration = DecorationCalculator { emptyList() }

class DividerDecorator(
  private val sidePadding: Dp = 16.dp,
  private val verticalPadding: Dp = 8.dp
) : Decorator {

  @SuppressLint("ModifierFactoryExtensionFunction")
  override fun decorate(): Modifier {
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
    modifier = modifier.then(decorator.decorate())
  }
  modifier
}