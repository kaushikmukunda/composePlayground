package com.km.composePlayground.scroller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRowForIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.layout.WithConstraintsScope
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUi
import com.km.composePlayground.base.UniformUiModel
import com.km.composePlayground.modifiers.fadingEdgeForeground


/** Action associated with Scrolling Ui. */
fun interface ScrollingUiAction {
  /**
   * Notifies listener to what position an item is currently visible
   */
  fun onItemRendered(position: Int)
}

/** Maps UiModels to composables. */
fun interface UiModelMapper {

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
  fun getChildWidth(scope: WithConstraintsScope): Dp

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
fun interface ItemDecoration {

  /** Determine the decorations for a given item represented by the UiModel. */
  fun getDecorators(uiModel: UiModel, itemIndex: Int, listSize: Int): List<Decorator>

}

/**
 * Resolves the list of decorators to a Compose Ui consumable Modifier.
 *
 * Modifier chaining is not associative. When custom chaining of
 * a group of Modifiers is required, this interface should be overridden.
 */
fun interface DecorationResolver {

  fun resolve(decorators: List<Decorator>): Modifier

}

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
 * Displays horizontally scrollable content.
 *
 * @param uiModel The UiModel for rendering the scroller.
 * @param layoutPolicy The policy to apply to the scroller.
 * @param config Configures additional behavior of the scroller like snapping.
 * @param mapper Transforms a UiModel to its Compose representation.
 * @param itemDecoration The decoration to be applied to the individual items in the scroller.
 * @param decorationResolver Optional resolver that transforms decoration to a Modifier.
 */
@Composable
fun HorizontalScrollerUi(
  uiModel: ScrollerUiModel,
  layoutPolicy: HorizontalScrollerLayoutPolicy,
  config: HorizontalScrollerConfig = DefaultHorizontalScrollerConfig,
  mapper: UiModelMapper,
  itemDecoration: ItemDecoration = NoDecoration,
  decorationResolver: DecorationResolver = StandardDecorationResolver
) = UniformUi(uiModel) { content ->
  WithConstraints {
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
      LazyRowForIndexed(
        items = content.items,
        contentPadding = layoutPolicy.getContentPadding().toPaddingValues(),
      ) { index, item ->
        content.uiAction.onItemRendered(index)

        val decoratorModifier = decorationResolver.resolve(
          itemDecoration
            .getDecorators(
              uiModel = item,
              itemIndex = index,
              listSize = content.items.size
            )
        )
        mapper.map(uiModel = item)(decoratorModifier.width(width = itemWidth).fillMaxHeight())
      }
    }
  }
}

@Composable
private fun Modifier.fadingEdge(
  withConstraintsScope: WithConstraintsScope,
  fadingEdgeWidth: Dp
): Modifier {
  val screenWidth = ContextAmbient.current.resources.configuration.screenWidthDp
  val scrollerWidth = withConstraintsScope.maxWidth
  return if (screenWidth < scrollerWidth.value) this
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

  override fun getChildWidth(scope: WithConstraintsScope): Dp {
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

val NoDecoration = object : ItemDecoration {
  override fun getDecorators(uiModel: UiModel, itemIndex: Int, listSize: Int) =
    emptyList<Decorator>()
}

class DividerDecorator(
  private val sidePadding: Dp = 16.dp,
  private val verticalPadding: Dp = 8.dp)
  : Decorator {

  override fun decorate(): Modifier {
    return Modifier
      .padding(start = sidePadding)
      .drawBehind {
        val top = verticalPadding.toPx()
        val bot = size.height - verticalPadding.toPx()
        val start = -sidePadding.toPx()/2

        drawLine(
          color = Color.Green,
          start = Offset(start, top),
          end = Offset(start, bot),
          strokeWidth = 1.dp.toPx()
        )
      }
  }
}

class SpacerDecorator(private val spacerSize: Dp = 8.dp) : Decorator {

  override fun decorate() = Modifier.padding(start = spacerSize)
}

val StandardDecorationResolver = DecorationResolver { decorators ->
  var modifier: Modifier = Modifier
  decorators.forEach { decorator ->
    modifier = modifier.then(decorator.decorate())
  }
  modifier
}