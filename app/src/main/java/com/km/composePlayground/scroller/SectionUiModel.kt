package com.km.composePlayground.scroller

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUiModel


/**
 * Represents a list of items stacked vertically in a vertical scroller. This class should be
 * wrapped in a UniformUiModel when being passed as an item in VerticalScrollerUiModel.
 * SectionUiModelContent is part of the contract layer for the vertical scroller render system.
 *
 * SectionUiModel cannot contain other SectionUiModels recursively.
 *
 * See go/phonesky-mvvm-scroller for more details.
 *
 * @param uiContent Initial [SectionUiModelContent] for the UiModel
 * @property loggingGroupVeMetadata VeMetadata for the common parent logging node. If populated,
 * all direct children UIs corresponding to UiModel populated in [SectionUiModelContent] will
 * share a common parent logging node constructed using this metadata.
 */
@Stable
class SectionUiModel(
  uiContent: SectionUiModelContent,
//  override val loggingGroupVeMetadata: VeMetadata? = null,
) : UniformUiModel<SectionUiModelContent> {
  //, LoggingGroupUiModel {
  override val content = mutableStateOf(uiContent)
}

/**
 * @property itemList Represents a list of items to be arranged as per the section's specification
 * @param identity Specifies the unique identification of the section. This identification is
 * used to bind UI for uiModels that have changed rather than rebinding all UIs.
 * Note: This identity is not used independently. It is combined with the identity of a uiModel
 * present in the [itemList] to create a unique identity for the item in the final flattened list.
 * If a new version of this content class should never be considered "same" as the previous version
 * populated in [SectionUiModel], then the identity should be set as [NEVER_EQUAL_ID].
 * @property scrollingUiAction Ui interaction associated with the scroller.
 */
@Stable
open class SectionUiModelContent(
  val itemList: List<UiModel>,
  identity: String,
  val scrollingUiAction: ScrollingUiAction = ScrollingUiAction {},
) //: Identifiable {
//  override val dataId: String = identity
//}
