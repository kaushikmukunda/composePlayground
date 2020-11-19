package com.km.composePlayground.scroller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRowForIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.layout.WithConstraintsScope
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUi
import com.km.composePlayground.base.UniformUiModel


/** Action associated with Scrolling Ui. */
fun interface ScrollingUiAction {
    /**
     * Notifies listener to what position an item is currently visible
     */
    fun triggerPagination(position: Int)
}

/** Maps UiModels to composables. */
fun interface UiModelMapper {
    @Composable
    fun map(uiModel: UiModel, modifier: Modifier)
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

}

interface ItemDecoration {

    @Composable
    fun decorator(uiModel: UiModel, itemIndex: Int): Modifier
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

        Box(alignment = alignment, modifier = Modifier.fillMaxWidth()) {
            LazyRowForIndexed(
                items = content.items,
                contentPadding = layoutPolicy.getContentPadding(),
            ) { index, item ->
                // Only notify on first composition of a particular item
                onActive {
                    content.uiAction.triggerPagination(index)
                }

                val decoratorModifier = itemDecoration.decorator(uiModel = item, itemIndex = index)
                mapper.map(
                    uiModel = item,
                    modifier = decoratorModifier.size(
                        width = itemSize.width,
                        height = itemSize.height
                    )
                )
            }
        }
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

    override fun getContentPadding() = PaddingValues(8.dp)

    override fun shouldCenterContent() = true

}

val NoDecoration = object: ItemDecoration {
    @Composable
    override fun decorator(uiModel: UiModel, itemIndex: Int) = Modifier
}

class DividerDecoration(val shouldDecorate: (UiModel, Int) -> Boolean): ItemDecoration {
    @Composable
    override fun decorator(uiModel: UiModel, itemIndex: Int): Modifier {
        return if (shouldDecorate(uiModel, itemIndex)) {
            val padding = 16.dp
            Modifier
                .padding(start = padding)
                .drawBehind {
                    val top = center.y - size.height / 2
                    val bot = center.y + size.height / 2
                    val start = center.x - size.width / 2 - padding.toPx()/2

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