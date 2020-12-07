package com.km.composePlayground.scroller

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRowForIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.layout.WithConstraintsScope
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.DensityAmbient
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

/** Container class to hold item size in Dp. */
class DpSize(val width: Dp, val height: Dp)

/**
 * Configure the layout parameters for the scroller.
 */
interface HorizontalScrollerLayoutPolicy {

  /**
   * Compute the dimensions of the item provided the scroller layout constraints.
   *
   * @param scope Provides the layout constraints of the scroller.
   */
  @Composable
  fun getItemSize(scope: WithConstraintsScope): DpSize

  /** The padding to be set on the scroller. */
  fun getContentPadding(): PaddingValues

  /** If the entire content were to fit in a single screen, should it be centered? */
  fun shouldCenterContent(): Boolean

  /** Width of the fading edge that might be drawn at the edges of the scroller. */
  fun getFadingEdgeWidth(): Dp = 0.dp

}

/** Configure item decoration within the scroller. */
interface ItemDecoration {

  /**
   *  Returns a Modifier that decorates a given item represented by the UiModel.
   *
   *  @param uiModel The UiModel to decorate.
   *  @param itemIndex The index of this item represented by the UiModel.
   *  @param listSize The number of items in the scroller.
   */
  fun decorator(uiModel: UiModel, itemIndex: Int, listSize: Int): Modifier
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

@Composable
fun HorizontalScrollerUi(
  uiModel: ScrollerUiModel,
  layoutPolicy: HorizontalScrollerLayoutPolicy,
  mapper: UiModelMapper,
  itemDecoration: ItemDecoration = NoDecoration,
) = UniformUi(uiModel) { content ->
  WithConstraints {
    val alignment =
      if (layoutPolicy.shouldCenterContent()) Alignment.Center else Alignment.TopStart
    val itemSize = layoutPolicy.getItemSize(this)

    Box(alignment = alignment,
      modifier = Modifier
        .fillMaxWidth()
        .fadingEdge(this, layoutPolicy.getFadingEdgeWidth())
    ) {
      LazyRowForIndexed(
        items = content.items,
        contentPadding = layoutPolicy.getContentPadding(),
      ) { index, item ->
        content.uiAction.onItemRendered(index)

        val decoratorModifier = itemDecoration
          .decorator(uiModel = item, itemIndex = index, listSize = content.items.size)
        mapper.map(uiModel = item).invoke(
          decoratorModifier.size(
            width = itemSize.width,
            height = itemSize.height
          )
        )
      }
    }
  }
}

@Composable
private fun Modifier.fadingEdge(withConstraintsScope: WithConstraintsScope, fadingEdgeWidth: Dp): Modifier {
  val screenWidth = ContextAmbient.current.resources.configuration.screenWidthDp
  val scrollerWidth = withConstraintsScope.maxWidth
  return if (screenWidth < scrollerWidth.value) this
  else {
    val ratio = fadingEdgeWidth / scrollerWidth
    this.fadingEdgeForeground(color = Color.White, leftRatio = ratio, rightRatio = ratio)
  }
}

/** A simple implementation of fixed layout policy. */
class FixedLayoutPolicy(
  val desiredItemWidth: Dp,
  val childPeekAmount: Float = 0.1f,
  val baseWidthMultipler: Float = 1f
) :
  HorizontalScrollerLayoutPolicy {

  @Composable
  override fun getItemSize(scope: WithConstraintsScope): DpSize {
    val width = with(DensityAmbient.current) {
      val widthForChildrenPx =
        (scope.maxWidth - getContentPadding().start - getContentPadding().end).toIntPx()

      remember(desiredItemWidth, widthForChildrenPx) {
        CardCountHelper.getUnitCardWidth(
          (desiredItemWidth * baseWidthMultipler).toIntPx(),
          widthForChildrenPx,
          childPeekAmount
        )
      }.toDp()
    }

    val height: Dp = width.times(9 / 16f)

    return DpSize(width, height)
  }

  override fun getContentPadding() = PaddingValues(16.dp)

  override fun shouldCenterContent() = true

  override fun getFadingEdgeWidth(): Dp {
    return 10.dp
  }
}

val NoDecoration = object : ItemDecoration {
  override fun decorator(uiModel: UiModel, itemIndex: Int, listSize: Int) = Modifier
}

class DividerDecoration(
  val shouldDecorate: (uiModel: UiModel, index: Int, listSize: Int) -> Boolean) : ItemDecoration {
  override fun decorator(uiModel: UiModel, itemIndex: Int, listSize: Int): Modifier {
    return if (shouldDecorate(uiModel, itemIndex, listSize)) {
      val padding = 16.dp
      Modifier
        .padding(start = padding)
        .drawBehind {
          val top = center.y - size.height / 2
          val bot = center.y + size.height / 2
          val start = center.x - size.width / 2 - padding.toPx() / 2

          drawLine(
            color = Color.Blue,
            start = Offset(start, top),
            end = Offset(start, bot),
            strokeWidth = 1.dp.toPx()
          )
        }
    } else Modifier
  }
}

class SpacerDecoration(
  val spacerSize: Dp = 8.dp,
  val shouldDecorate: (UiModel, Int, Int) -> Boolean) : ItemDecoration {

  override fun decorator(uiModel: UiModel, itemIndex: Int, listSize: Int) =
    if (shouldDecorate(uiModel, itemIndex, listSize)) Modifier.padding(start = spacerSize)
    else Modifier
}