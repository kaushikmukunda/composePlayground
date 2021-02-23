package com.km.composePlayground.components.dialog

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.linkText.MarkdownText

sealed class IconAsset {
  class ImageIcon(val asset: ImageBitmap) : IconAsset()
  class VectorIcon(val asset: ImageVector) : IconAsset()
}

class HeaderModel(
  val icon: IconAsset? = null,
  val title: String
)

class ContentModel(
  val content: MarkdownText
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

  fun onLinkClicked(url: String, dialogData: Any?)

  fun onPositiveButtonClicked(dialogData: Any?)

  fun onNegativeButtonClicked(dialogData: Any?)

  fun onDismiss(dialogData: Any?)

}

class DialogUiModel(
  val uiAction: DialogUiAction,
  val header: HeaderModel? = null,
  val content: ContentModel,
  val footer: FooterModel,
  val dismissOnTapOutside: Boolean = true,
  val dialogData: Any? = null,
) : UiModel