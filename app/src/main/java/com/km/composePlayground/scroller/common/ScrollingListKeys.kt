package com.km.composePlayground.scroller.common

import androidx.compose.ui.util.fastMap
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.components.common.Identifiable

/**
 * Compute keys for each of the [UiModel] in the provided list. Retrieve the model's identifiable if
 * it exists, else default to hashCode.
 *
 * @param uiModels The list of UiModel to compute the keys for.
 * @param prefix Optional string to use as a prefix for the item key.
 */
fun computeUiModelKeys(uiModels: List<UiModel>, prefix: String = ""): List<String> {
  return uiModels.fastMap { uiModel ->
    val modelKey = if (uiModel is Identifiable) uiModel.dataId else uiModel.hashCode()
    "${prefix}_$modelKey"
  }
}
