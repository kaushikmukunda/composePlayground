package com.km.composePlayground.scroller.vertical

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.scroller.DynamicGridUiModel
import com.km.composePlayground.scroller.common.DecorationCalculator
import com.km.composePlayground.scroller.common.DecorationModifierCalculator
import com.km.composePlayground.scroller.common.EMPTY_DECORATION_CALCULATOR
import com.km.composePlayground.scroller.common.EMPTY_DECORATION_MODIFIER_CALCULATOR
import com.km.composePlayground.scroller.common.PositionInfo
import com.km.composePlayground.scroller.common.RenderBlockingUiModel
import com.km.composePlayground.scroller.common.ScrollerPositionInfo
import com.km.composePlayground.scroller.common.computeUiModelKeys
import com.km.composePlayground.scroller.horizontal.UiModelComposableMapper


private val NO_CONTENT_PADDING = PaddingValues(0.dp)

/**
 * Displays vertically scrollable content.
 *
 * @param uiModel The UiModel for rendering the scroller.
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
@SuppressLint("ModifierParameter")
@Composable
fun VerticalScrollerUi(
  uiModel: VerticalScrollerUiModel,
  mapper: UiModelComposableMapper,
  decorationCalculator: DecorationCalculator = EMPTY_DECORATION_CALCULATOR,
  decorationModifierCalculator: DecorationModifierCalculator = EMPTY_DECORATION_MODIFIER_CALCULATOR,
  contentPadding: PaddingValues = NO_CONTENT_PADDING,
  containerModifier: Modifier = Modifier
) {
  val content = uiModel.content.value

  val scrollerElementRenderer =
    remember(mapper, decorationCalculator, decorationModifierCalculator) {
      ElementRenderer(mapper, decorationCalculator, decorationModifierCalculator)
    }

  val filteredModels = filterRenderBlockingModels(content.itemList)
  val itemKeys =
    remember(content.itemList) {
      computeUiModelKeys(filteredModels, uiModel.hashCode().toString())
    }
  LazyColumn(modifier = containerModifier.fillMaxSize(), contentPadding = contentPadding) {
    // UiModels following a RenderBlockingUiModel should not be displayed
    filteredModels.forEachIndexed { index, listItem ->
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
        else ->
          item(key = itemKeys[index]) {
            val positionInfo =
              ScrollerPositionInfo(index == 0, index == content.itemList.size - 1)
            scrollerElementRenderer.render(listItem, positionInfo, Modifier)

            SideEffect { content.scrollingUiAction.onItemRendered(index) }
          }
      }
    }
  }
}

/**
 * UiModels below a [RenderBlockingUiModel] must not be rendered. Pre-process the list of models and
 * truncate the list to the first [RenderBlockingUiModel] found in the list. This could be either at
 * the top level or within a sectionUiModel.
 *
 * Ex: [UiModelA, NestedUiModelB[UiModelAA, UIModelBB, RenderBlockingUiModel, UiModelCC], UiModelC]
 * -> [UiModelA, NestedUiModelB[UiModelAA, UIModelBB, RenderBlockingUiModel]]
 */
@Composable
private fun filterRenderBlockingModels(uiModels: List<UiModel>): List<UiModel> {
  val filteredModels = mutableListOf<UiModel>()
  for (uiModel in uiModels) {
    when (uiModel) {
      is DynamicGridUiModel -> {
        val content = requireNotNull(uiModel.content.value)
        val filteredGridModels = filterRenderBlockingModels(content.itemList)
        if (filteredGridModels.isNotEmpty() && filteredGridModels.last() is RenderBlockingUiModel) {
          val filteredUiModel =
            DynamicGridUiModel(
              content.copy(
                itemList = filteredGridModels,
                desiredCellSize = content.desiredCellSize
              ),
//              loggingGroupVeMetadata = uiModel.loggingGroupVeMetadata
            )
          filteredModels.add(filteredUiModel)
          break
        } else {
          filteredModels.add(uiModel)
        }
      }
      is StaticGridUiModel -> {
        val content = requireNotNull(uiModel.content.value)
        val filteredGridModels = filterRenderBlockingModels(content.itemList)
        if (filteredGridModels.isNotEmpty() && filteredGridModels.last() is RenderBlockingUiModel) {
          val filteredUiModel =
            StaticGridUiModel(
              content.copy(itemList = filteredGridModels, spanCount = content.spanCount)
//              loggingGroupVeMetadata = uiModel.loggingGroupVeMetadata
            )
          filteredModels.add(filteredUiModel)
          break
        } else {
          filteredModels.add(uiModel)
        }
      }
      is SectionUiModel -> {
        val content = requireNotNull(uiModel.content.value)
        val filteredGridModels = filterRenderBlockingModels(content.itemList)
        if (filteredGridModels.isNotEmpty() && filteredGridModels.last() is RenderBlockingUiModel) {
          val filteredUiModel =
            SectionUiModel(
              content.copy(itemList = filteredGridModels),
//              loggingGroupVeMetadata = uiModel.loggingGroupVeMetadata
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
 * Convenience class that helps transform a [UiModel] to its Composable with decorations. This
 * abstracts out the rendering dependencies for scroller sections - linear, static grid, dynamic
 * grid.
 */
internal class ElementRenderer(
  private val mapper: UiModelComposableMapper,
  private val decorationCalculator: DecorationCalculator,
  private val decorationModifierCalculator: DecorationModifierCalculator
) {

  // Cache parentElementNode so as to not recreate elementNodes within the same section.
//  private val elementNodeMap = mutableMapOf<VeMetadata, PlayStoreUiElementNode>()

  @Composable
  fun render(
    uiModel: UiModel,
    positionInfo: PositionInfo,
    modifier: Modifier,
//    parentLoggingData: VeMetadata? = null
  ) //=
    /*tracePrefix.trace("RenderItem")*/ {
    val decorationModifier = Modifier
//        decorationModifierCalculator.getModifierForDecorations(
//          decorationCalculator.getDecorationsForUiModel(uiModel, positionInfo)
//        )

//      if (parentLoggingData != null) {
//        val parentNode = LocalUiElementNode.current
//        // Create a placeholder logging node for item groups.
//        val parentElementNode =
//          elementNodeMap.getOrPut(parentLoggingData) {
//            GenericUiElementNode(
//              parentLoggingData.uiElementType,
//              parentLoggingData.serverLogsCookie,
//              parentNode
//            )
//          }
//
//        withParentUiElementNode(parentElementNode) {
//          mapper.map(uiModel).invoke(modifier.then(decorationModifier))
//        }
//      } else {
    mapper.map(uiModel).invoke(modifier.then(decorationModifier))
//      }
  }
}