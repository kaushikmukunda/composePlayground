package com.km.composePlayground.linkText

import androidx.compose.runtime.Immutable
import com.km.composePlayground.base.UiModel

/** Actions user can perform on a link text. */
fun interface LinkTextUiAction {
  /** Called when the link text is clicked. [LinkTextUiModel.clickData] is passed back. */
  fun onClick(clickData: Any?)
}

/**
 * UiModel to configure LinkText.
 *
 * @property uiAction Callback interface for user actions.
 * @property clickData Optional Opaque object that is not used by [LinkTextUi]. Only used by
 *   [LinkTextUiAction] to send it back to listener during click event.
 * @property textMarkdown The non-clickable portion of the display text.
 * @property linkText The clickable portion of the display text.
 * @property typography Text style for the display text.
 * @property vxStyle [VxStyle] that determines the color.
 * @property containerLoggingData Optional [VeMetadata] to be used for impression and click logging.
 *   This is used for the logging node of the container.
 * @property linkLoggingData Optional [VeMetadata] to be used for impression and click logging.
 *   This is used for the logging node of the clickable text.
 */
@Immutable
class LinkTextUiModel(
  val uiAction: LinkTextUiAction,
  val clickData: Any? = null,
  val textMarkdown: MarkdownText = MarkdownText(""),
  val linkTextMarkdown: MarkdownText = MarkdownText(""),
//  val typography: PhoneskySemanticTypographyName,
//  val vxStyle: VxStyle,
//  val containerLoggingData: VeMetadata = VeMetadata(PlayStore.PlayStoreUiElement.Type.OTHER),
//  val linkLoggingData: VeMetadata = VeMetadata(PlayStore.PlayStoreUiElement.Type.OTHER)
) : UiModel
