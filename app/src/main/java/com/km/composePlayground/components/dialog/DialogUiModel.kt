package com.km.composePlayground.components.dialog

import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.vector.VectorAsset
import com.km.composePlayground.base.UiModel

sealed class IconAsset {
    class ImageIcon(val asset: ImageAsset) : IconAsset()
    class VectorIcon(val asset: VectorAsset) : IconAsset()
}

class HeaderModel(
    val icon: IconAsset? = null,
    val title: String
)

class ContentModel(
    val content: String
)

class FooterModel(
    val positiveButtonConfig: DialogButtonConfig,
    val negativeButtonConfig: DialogButtonConfig
)

class DialogButtonConfig(
    val buttonText: String,
    // val logging info
    val accessibilityLabel: String? = null
)

interface DialogUiAction {

    fun onPositiveButtonClicked(dialogData: Any?)

    fun onNegativeButtonClicked(dialogData: Any?)

    fun onDismiss(dialogData: Any?)

}

class DialogUiModel(
    val uiAction: DialogUiAction,
    val header: HeaderModel?=null,
    val content: ContentModel,
    val footer: FooterModel,
    val dismissOnTapOutside: Boolean = true,
    val dialogData: Any? = null,
) : UiModel