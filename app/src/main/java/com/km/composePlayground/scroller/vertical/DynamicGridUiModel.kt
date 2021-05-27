package com.km.composePlayground.scroller

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUiModel
import com.km.composePlayground.scroller.common.ItemSpanLookup
import com.km.composePlayground.scroller.horizontal.ScrollingUiAction
import com.km.composePlayground.scroller.vertical.SectionUiModelContent


/**
 * Represents a group (section) of UiModels to be arranged in the form of a grid in the vertical
 * scroller ui. Unlike [StaticGridUiModelContent] which has a fixed span count, the span count of
 * DynamicGridUiModelContent is calculated at render time. DynamicGridUiModel is part of the
 * contract for the vertical scroller system.
 *
 * Sections represented by this uiModel will not share rows with other sections even when the last
 * row is not filled.
 *
 * See go/phonesky-mvvm-scroller for more details.
 *
 * @property content The content that can change in this UiModel.
 * @property loggingGroupVeMetadata Optional logging information to construct a
 * [PlayStoreUiElementNode] which will be a common parent to all items represented by UiModels in
 * [DynamicGridUiModelContent]. Refer to [LoggingGroupUiModel] for more details.
 */
class DynamicGridUiModel(
  uiContent: DynamicGridUiModelContent,
//  override val loggingGroupVeMetadata: VeMetadata? = null
) : UniformUiModel<DynamicGridUiModelContent> {
  override val content = mutableStateOf(uiContent)
}

/**
 * The content of [DynamicGridUiModel]
 *
 * @property itemList Represents a list of items arranged in a grid as specified
 * @property desiredCellSize Represents the desired size of a single column in the grid in pixels.
 * The width of the column is calculated on the fly based on the available width for the content in
 * the vertical scroller.
 * For e.g., if the desired cell width is 80 pixels and the width of the scroller is 249 pixels, the
 * number of columns per row for the grid specified by the DynamicGridUiModel will be 3 and cell
 * size will be 83 pixels
 * @property spanLookup Specifies how many columns should a given item in the itemList consume
 * @param identity Specifies the unique identification of the grid. This identification is used to
 * bind UI for uiModels that have changed rather than rebinding all UIs.
 * Note: This identity is not used independently. It is combined with the identity of a uiModel
 * present in the [itemList] to create a unique identity for the item in the final flattened list.
 * If a new version of this content class should never be considered "same" as the previous version
 * populated in [DynamicGridUiModel], then the identity should be set as [NEVER_EQUAL_ID].
 * @param scrollingUiAction Ui interaction associated with the scroller.
 */
@Stable
class DynamicGridUiModelContent(
  itemList: List<UiModel>,
  val desiredCellSize: Int,
  val spanLookup: ItemSpanLookup,
  identity: String,
  scrollingUiAction: ScrollingUiAction = ScrollingUiAction {},
) : SectionUiModelContent(itemList, identity, scrollingUiAction) {

  fun copy(
    itemList: List<UiModel> = this.itemList,
    desiredCellSize: Int = this.desiredCellSize,
    spanLookup: ItemSpanLookup = this.spanLookup,
    identity: String = this.dataId,
    scrollingUiAction: ScrollingUiAction = this.scrollingUiAction
  ): DynamicGridUiModelContent {
    return DynamicGridUiModelContent(
      itemList = itemList,
      desiredCellSize = desiredCellSize,
      spanLookup = spanLookup,
      identity = identity,
      scrollingUiAction = scrollingUiAction
    )
  }
}
