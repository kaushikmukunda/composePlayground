package com.km.composePlayground.scroller.vertical

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUiModel
import com.km.composePlayground.scroller.horizontal.ScrollingUiAction


/**
 * VerticalScrollerUiModelContent is part of the contract layer for the vertical scroller system. It
 * represents a list of items stacked vertically in a vertical scrolling list. The actual contract
 * of the vertical scroller is UniformUiModel<VerticalScrollerUiModel>
 *
 * The list of uiModels in ScrollingListUiModel can contain:
 *   - UiModel: This represents uiModel (which extends UiModel) for a single item in the Compose
 *     LazyColumn.
 *   - SectionUiModel: This represents a group (section) of items in the LazyColumn, stacked
 *     vertically.
 *   - GridUiModel: This represents a group (section) of items in the LazyColumn, arranged in a
 *     grid.
 *   - DynamicGridUiModel: This represents a group (section) of items in the LazyColumn,
 *     arranged in a grid whose cell size is decided based on the Column width.
 *
 * See go/phonesky-mvvm-scroller for more details.
 */
@Stable
class VerticalScrollerUiModel(
  uiContent: VerticalScrollerUiModelContent
) : UniformUiModel<VerticalScrollerUiModelContent> {
  override val content = mutableStateOf(uiContent)
}

/**
 * @property itemList Represents a liveData of the list of uiModels rendered in the vertical
 * scroller
 * @property scrollingUiAction Represents interactions with the scroller. Ex: A callback is
 * triggered when a uiModel in the list is being rendered by the renderer. This callback will be
 * triggered with the position of the item being rendered passed in. This callback can then be used
 * by the adapter to fetch more data or perform any other additional tasks if needed.
 */
@Stable
class VerticalScrollerUiModelContent(
  val itemList: List<UiModel>,
  val scrollingUiAction: ScrollingUiAction = ScrollingUiAction {}
)
