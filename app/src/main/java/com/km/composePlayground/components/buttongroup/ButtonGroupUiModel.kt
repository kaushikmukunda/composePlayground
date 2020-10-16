package com.km.composePlayground.components.buttongroup

import androidx.compose.runtime.Immutable
import com.km.composePlayground.components.button.*

/** Allowed variants of button group. */
enum class ButtonGroupVariant {
  /**
   * Primary button will use the filled style and secondary button will use the outline style.
   * https://screenshot.googleplex.com/Wi3X9mcctnF
   */
  OUTLINE_FILL,
  /**
   * Primary button will use the filled style and secondary button will use the outline style. The
   * buttons will scale to fill the entire button group view width.
   * https://screenshot.googleplex.com/JSDPGvFa0pz
   */
  OUTLINE_FILL_50_50,
  /**
   * Primary button will use the filled style and secondary button will use the invisible style.
   * https://screenshot.googleplex.com/joHDyL33YJw
   */
  INVISIBLE_FILL,
  /**
   * Primary button and secondary button will use the invisible style.
   * https://screenshot.googleplex.com/gARrrxGr428
   */
  INVISIBLE_INVISIBLE,
  /**
   * Primary button will use the outline style and secondary button will use the filled style.
   * https://screenshot.googleplex.com/wWg6xg5OrAL
   */
  FILL_OUTLINE,
  /**
   * Primary button will use the outline style and secondary button will use the filled style. The
   * buttons will scale to fill the entire button group view width.
   * https://screenshot.googleplex.com/pg0NO8ApiTh
   */
  FILL_OUTLINE_50_50,
  /**
   * Both primary button and secondary button will use the outline style. The buttons will scale
   * to fill the entire button group view width. https://screenshot.googleplex.com/hxa6WqFZaby
   */
  OUTLINE_OUTLINE_50_50,
  /**
   * Primary button will use the invisible style and secondary button will use the fill style.
   * https://screenshot.googleplex.com/JDgsdJRLsJQ
   */
  FILL_INVISIBLE,
  /**
   * Primary button will use the invisible style and secondary button will use the outline style.
   * https://screenshot.googleplex.com/JspeO23FC0x
   */
  OUTLINE_INVISIBLE;
}

/** The Alignment of the Button Group. This does not determine ordering of the buttons. */
enum class ButtonGroupSnapping {
  LEFT,
  RIGHT
}

/**
 * This is a subset of the [ButtonUiModel] without the styling. Those params are determined by
 * the group variant.
 *
 * @property buttonText The label to display on the button.
 * @property uiAction Ui interactions supported by the button.
 * @property clickData Opaque object that is not used by [ButtonUi]. Only used by [ButtonUiAction]
 *     to send it back to listener during click event.
 * @property buttonState State of the button.
 * @property buttonPadding Optional padding for the button.
 * @property iconModel Optional icon to be shown next to the button label.
 * @property vxStyle [VxStyle] that determines the color.
 * @property theme Optional theme to be used for button if it is placed on a light/dark background.
 * @property accessibilityLabel Text to be read by Talkback when focused on button. If null,
 *   will default to buttonText.
 * @property loggingData Optional [VeMetadata] to be used for impression and click logging.
 */
@Immutable
class ButtonConfig(
  val buttonText: String,
  val uiAction: ButtonUiAction,
  val clickData: Any?,
  val buttonState: ButtonState = ButtonState.ENABLED,
  val buttonPadding: ButtonPadding? = null,
  val iconModel: IconModel? = null,
//  val vxStyle: VxStyle = VxStyle.MULTI,
//  val accessibilityLabel: String? = null,
//  val loggingData: VeMetadata = VeMetadata(PlayStore.PlayStoreUiElement.Type.OTHER)
)

/**
 * Ui model that represents ButtonGroupUI composable.
 *
 * @property buttonGroupVariant The variant styling to be applied to the buttons.
 * @property buttonGroupSnapping Aligns the button group to the left edge or right edge of the view.
 * @property buttonVariant The button size for the individual buttons.
 * @property isSingleButtonSecondary Indicates that there is only one button and should be styled as
 *     secondary button.
 * @property firstButtonConfig The button metadata for the first button.
 * @property secondButtonConfig The button metadata for the second button.
 */
@Immutable
class ButtonGroupUiModel(
  val buttonGroupVariant: ButtonGroupVariant = ButtonGroupVariant.OUTLINE_FILL,
  val buttonGroupSnapping: ButtonGroupSnapping = ButtonGroupSnapping.LEFT,
  val buttonVariant: ButtonVariant = ButtonVariant.STANDARD,
  val isSingleButtonSecondary: Boolean = false,
  val firstButtonConfig: ButtonConfig,
  val secondButtonConfig: ButtonConfig? = null
)
