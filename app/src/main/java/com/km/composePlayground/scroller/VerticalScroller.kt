package com.km.composePlayground.scroller

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
fun VerticalScrollerUi(
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
    // UiModels following a RenderBlockingUiModel should not be displayed
    filterRenderBlockingModels(content.itemList).forEachIndexed { index, listItem ->
      when (listItem) {
        // onItemRendered for the Grid sections are not wrapped in the SideEffect as LazyListScope
        // does not have Composable scope.
        is DynamicGridUiModel -> {
          DynamicGridUi(listItem, scrollerElementRenderer)
          content.scrollingUiAction.onItemRendered(index)
        }
        is StaticGridUiModel -> {
          StaticGridUi(listItem, scrollerElementRenderer)
          content.scrollingUiAction.onItemRendered(index)
        }
        is SectionUiModel -> {
          LinearUi(listItem, scrollerElementRenderer)
          content.scrollingUiAction.onItemRendered(index)
        }
        else -> item {
          scrollerElementRenderer.Render(listItem, Modifier)

          SideEffect {
            content.scrollingUiAction.onItemRendered(index)
          }
        }
      }
    }
  }
}

/**
 * UiModels below a [RenderBlockingUiModel] must not be rendered. Pre-process the list of models
 * and truncate the list to the first [RenderBlockingUiModel] found in the list. This could be
 * either at the top level or within a sectionUiModel.
 *
 * Ex: [UiModelA, NestedUiModelB[UiModelAA, UIModelBB, RenderBlockingUiModel, UiModelCC], UiModelC]
 *  -> [UiModelA, NestedUiModelB[UiModelAA, UIModelBB, RenderBlockingUiModel]]
 */
private fun filterRenderBlockingModels(uiModels: List<UiModel>): List<UiModel> {
  val filteredModels = mutableListOf<UiModel>()
  for (uiModel in uiModels) {
    when (uiModel) {
      is DynamicGridUiModel -> {
        val content = uiModel.content.value
        val filteredGridModels = filterRenderBlockingModels(content.itemList)
        if (filteredGridModels.isNotEmpty() && filteredGridModels.last() is RenderBlockingUiModel) {
          val filteredUiModel = DynamicGridUiModel(
            DynamicGridUiModelContent(
              itemList = filterRenderBlockingModels(content.itemList),
              desiredCellSize = content.desiredCellSize,
              spanLookup = content.spanLookup,
              identity = content.dataId,
              scrollingUiAction = content.scrollingUiAction
            )
          )
          filteredModels.add(filteredUiModel)
          break
        } else {
          filteredModels.add(uiModel)
        }
      }
      is StaticGridUiModel -> {
        val content = uiModel.content.value
        val filteredGridModels = filterRenderBlockingModels(content.itemList)
        if (filteredGridModels.isNotEmpty() && filteredGridModels.last() is RenderBlockingUiModel) {
          val filteredUiModel = StaticGridUiModel(
            StaticGridUiModelContent(
              itemList = filterRenderBlockingModels(content.itemList),
              spanCount = content.spanCount,
              spanLookup = content.spanLookup,
              identity = content.dataId,
              scrollingUiAction = content.scrollingUiAction
            )
          )
          filteredModels.add(filteredUiModel)
          break
        } else {
          filteredModels.add(uiModel)
        }
      }
      is SectionUiModel -> {
        val content = uiModel.content.value
        val filteredGridModels = filterRenderBlockingModels(content.itemList)
        if (filteredGridModels.isNotEmpty() && filteredGridModels.last() is RenderBlockingUiModel) {
          val filteredUiModel = SectionUiModel(
            SectionUiModelContent(
              itemList = filterRenderBlockingModels(content.itemList),
              identity = content.dataId,
              scrollingUiAction = content.scrollingUiAction
            )
          )
          filteredModels.add(filteredUiModel)
        } else {
          filteredModels.add(uiModel)
        }
      }
      is RenderBlockingUiModel -> {
        filteredModels.add(uiModel)
        break
      }
      else -> filteredModels.add(uiModel)
    }
  }

  return filteredModels
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