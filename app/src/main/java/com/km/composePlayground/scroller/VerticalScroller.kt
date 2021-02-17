package com.km.composePlayground.scroller

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUi

private val NO_CONTENT_PADDING = PaddingValues(0.dp)

/**
 * Displays vertically scrollable content.
 *
 * @param uiModel The UiModel for rendering the scroller.
 * @param mapper Transforms a UiModel to its Compose representation.
 * @param decorationCalculator Class which calculates the decorations to be applied to an item
 *     based on its UiModel
 * @param decorationModifierCalculator Class which calculates the modifiers to be applied to the
 *     item composables based on the decorations to be applied to the item
 * @param contentPadding a padding around the whole content. This will add padding for the
 *   content after it has been clipped, which is not possible via [modifier] param. Note that it is
 *   **not** a padding applied for each item's content.
 * @param containerModifier Optional modifier to be applied to the scroll container.
 */
@SuppressLint("ModifierParameter")
@Composable
fun VerticalScroller(
  uiModel: VerticalScrollerUiModel,
  mapper: UiModelComposableMapper,
  decorationCalculator: DecorationCalculator = EMPTY_DECORATION_CALCULATOR,
  decorationModifierCalculator: DecorationModifierCalculator = EMPTY_DECORATION_MODIFIER_CALCULATOR,
  contentPadding: PaddingValues = NO_CONTENT_PADDING,
  containerModifier: Modifier = Modifier
) = UniformUi(uiModel) { content ->
  val scrollerElementRenderer =
    remember(mapper, decorationCalculator, decorationModifierCalculator) {
      ElementRenderer(mapper, decorationCalculator, decorationModifierCalculator)
    }

  LazyColumn(modifier = containerModifier.fillMaxSize(), contentPadding = contentPadding) {
    // UiModels following a RenderBlockingUiModel should not be displayed. As such we filter the
    // nested list in two phases - truncate any models below a RenderBlockingUiModel at the top-level
    // and then once again within Sections - DynamicGrid, StaticGrid, Section.
    filterRenderBlockingModels(content.itemList).forEachIndexed { index, listItem ->
      val filteredModel = filterRenderableContent(listItem)
      when (filteredModel) {
        is DynamicGridUiModel -> DynamicGridUi(filteredModel, scrollerElementRenderer)
        is StaticGridUiModel -> StaticGridUi(filteredModel, scrollerElementRenderer)
        is SectionUiModel -> LinearUi(filteredModel, scrollerElementRenderer)
        else -> item {
          val decorationModifier = decorationModifierCalculator
            .getModifierForDecorations(decorationCalculator.getDecorationsForUiModel(listItem))
          mapper.map(listItem).invoke(decorationModifier)
        }
      }

      content.scrollingUiAction.onItemRendered(index)
    }
  }
}

private fun filterRenderableContent(uiModel: UiModel): UiModel {
  return when (uiModel) {
    is DynamicGridUiModel -> {
      val content = uiModel.content.value
      DynamicGridUiModel(
        DynamicGridUiModelContent(
          itemList = filterRenderBlockingModels(content.itemList),
          desiredCellSize = content.desiredCellSize,
          spanLookup = content.spanLookup,
          identity = content.identity,
          scrollingUiAction = content.scrollingUiAction
        )
      )
    }
    is StaticGridUiModel -> {
      val content = uiModel.content.value
      StaticGridUiModel(
        StaticGridUiModelContent(
          itemList = filterRenderBlockingModels(content.itemList),
          spanCount = content.spanCount,
          spanLookup = content.spanLookup,
          identity = content.identity,
          scrollingUiAction = content.scrollingUiAction
        )
      )
    }
    is SectionUiModel -> {
      val content = uiModel.content.value
      SectionUiModel(
        SectionUiModelContent(
          itemList = filterRenderBlockingModels(content.itemList),
          identity = content.identity,
          scrollingUiAction = content.scrollingUiAction
        )
      )
    }
    else -> uiModel
  }
}

private fun filterRenderBlockingModels(uiModels: List<UiModel>): List<UiModel> {
  val firstBlockingUiModel = uiModels.find { uiModel -> uiModel is RenderBlockingUiModel }
  return firstBlockingUiModel?.let {
    uiModels.subList(0, uiModels.indexOf(firstBlockingUiModel) + 1)
  } ?: uiModels

}

/**
 * Convenience class that helps transform a [UiModel] to its Composable with decorations.
 * This abstracts out the rendering dependencies for scroller sections - linear, static grid,
 * dynamic grid.
 */
internal class ElementRenderer(
  private val mapper: UiModelComposableMapper,
  private val decorationCalculator: DecorationCalculator,
  private val decorationModifierCalculator: DecorationModifierCalculator
) {

  @Composable
  fun Render(uiModel: UiModel, modifier: Modifier) {
    val decorationModifier = decorationModifierCalculator
      .getModifierForDecorations(decorationCalculator.getDecorationsForUiModel(uiModel))

    mapper.map(uiModel).invoke(modifier.then(decorationModifier))
  }
}