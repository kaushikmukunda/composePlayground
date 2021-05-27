package com.km.composePlayground.scroller.vertical

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUiModel
import com.km.composePlayground.scroller.common.ItemSpanLookup
import com.km.composePlayground.scroller.horizontal.ScrollingUiAction

/**
 * Represents a group (section) of UiModels to be arranged in the form of a grid. The specifications
 * of the grid are static i.e. they don't change as a result of resizing of the vertical scroller.
 * StaticGridUiModelContent is part of the contract for the vertical scroller system.
 *
 * See go/phonesky-mvvm-scroller for more details.
 *
 * Sections represented by this uiModel will not share rows with other sections even when the last
 * row is not filled.
 * @property content MutableLiveData of the content that can change in this UiModel. The content
 * is [SectionUiModelContent]
 * @property loggingGroupVeMetadata Optional logging information to construct a
 * [PlayStoreUiElementNode] which will be a common parent to all items represented by UiModels in
 * [SectionUiModelContent].
 */
@Stable
class StaticGridUiModel(
  uiContent: StaticGridUiModelContent,
//  override val loggingGroupVeMetadata: VeMetadata? = null
) : UniformUiModel<StaticGridUiModelContent> {
  override val content = mutableStateOf(uiContent)
}

/**
 * @property itemList Represents a list of items stacked vertically in the vertical scroller
 * @property spanCount Represents the number of columns per row in the grid.
 * @property spanLookup Specifies how many columns should a given UiModel in the itemList consume
 * @param identity Specifies the unique identification of the grid. This identification is used to
 * bind UI for uiModels that have changed rather than rebinding all UIs.
 * Note: This identity is not used independently. It is combined with the identity of a uiModel
 * present in the [itemList] to create a unique identity for the item in the final flattened list.
 * If a new version of this content class should never be considered "same" as the previous version
 * populated in [StaticGridUiModel], then the identity should be set as [NEVER_EQUAL_ID].
 * @param scrollingUiAction Ui interaction associated with the scroller.
 */
@Stable
class StaticGridUiModelContent(
  itemList: List<UiModel>,
  val spanCount: Int,
  val spanLookup: ItemSpanLookup,
  identity: String,
  scrollingUiAction: ScrollingUiAction = ScrollingUiAction {},
) : SectionUiModelContent(itemList, identity, scrollingUiAction) {

  fun copy(
    itemList: List<UiModel> = this.itemList,
    spanCount: Int = this.spanCount,
    spanLookup: ItemSpanLookup = this.spanLookup,
    identity: String = this.dataId,
    scrollingUiAction: ScrollingUiAction = this.scrollingUiAction
  ): StaticGridUiModelContent {
    return StaticGridUiModelContent(
      itemList = itemList,
      spanCount = spanCount,
      spanLookup = spanLookup,
      identity = identity,
      scrollingUiAction = scrollingUiAction
    )
  }
}
