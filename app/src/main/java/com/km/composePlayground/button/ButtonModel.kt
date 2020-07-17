package com.km.composePlayground.button

import android.view.MotionEvent
import androidx.compose.Immutable
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.unit.Dp
import androidx.ui.unit.dp

/** Actions associated with [ButtonUi]. */
class ButtonUiAction(
    val onShown: () -> Unit,
    val onClick: (Any?) -> Unit,
    val onTouch: (Any?, MotionEvent) -> Unit
)

/** Preset padding for button. */
enum class ButtonPadding {
    NONE,
    DEFAULT,
    COMPACT,
    LOOSE
}

/** The type of button. See http://shortn/_lfmC8xpzFo */
enum class ButtonVariant {
    STANDARD,
    SMALL
}

/** The style of button. See http://shortn/_lfmC8xpzFo */
enum class ButtonStyle {
    FILLED,
    OUTLINE,
    LINK
}

/** The state of button. See http://shortn/_lfmC8xpzFo */
enum class ButtonState {
    ENABLED,
    DISABLED,
    HIDDEN
}

enum class IconPlacement {
    START,
    END
}

private val DEFAULT_ICON_PADDING = 8.dp
private val DEFAULT_ICON_SIZE = 18.dp

sealed class IconAsset {
    class ImageIcon(val asset: ImageAsset) : IconAsset()
    class VectorIcon(val asset: VectorAsset) : IconAsset()
}

class IconModel(
    val icon: IconAsset,
    val iconPlacement: IconPlacement,
    val iconPadding: Dp = DEFAULT_ICON_PADDING,
    val iconWidth: Dp = DEFAULT_ICON_SIZE,
    val iconHeight: Dp = DEFAULT_ICON_SIZE,
    val colorFilter: ColorFilter? = null
)

/**
 * Customizable properties exposed by [ButtonUi].
 *
 * @property buttonText The label to display on the button.
 * @property uiAction Ui interactions supported by the button.
 * @property clickData Opaque object that is not used by [ButtonUi]. Only used by [ButtonUiAction]
 *  to send it back to listener during click event.
 * @property buttonVariant Button variant type.
 * @property buttonPadding Button padding.
 * @property buttonState State of the button.
 * @property iconModel Optional icon to be shown next to the button label.
 * @property backend Used to determine the color of the button.
 */
@Immutable
class ButtonUiModel(
    val buttonText: String,
    val uiAction: ButtonUiAction,
    val clickData: Any?,
    val buttonVariant: ButtonVariant = ButtonVariant.STANDARD,
    val buttonStyle: ButtonStyle = ButtonStyle.FILLED,
    val buttonPadding: ButtonPadding = ButtonPadding.DEFAULT,
    val buttonState: ButtonState = ButtonState.ENABLED,
    val iconModel: IconModel? = null
) {
    fun isEnabled(): Boolean = buttonState == ButtonState.ENABLED
}
