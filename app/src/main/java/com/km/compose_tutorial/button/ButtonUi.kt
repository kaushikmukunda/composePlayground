package com.km.compose_tutorial.button

import androidx.compose.Composable
import androidx.compose.Stable
import androidx.compose.onCommit
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
import androidx.ui.foundation.Border
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.graphics.Color
import androidx.ui.graphics.ImageAsset
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.ripple.RippleIndication
import androidx.ui.res.colorResource
import androidx.ui.res.dimensionResource
import androidx.ui.unit.Dp
import androidx.ui.unit.dp
import com.km.compose_tutorial.R

/** Composable button container for injecting dependencies. */
@Stable
class ButtonComposer constructor(private val colorUtility: ColorUtility) {

    @Composable
    fun compose(model: ButtonUiModel, modifier: Modifier = Modifier) {
        ButtonUi(colorUtility, model, modifier)
    }
}

/**
 * Displays a button as defined by [ButtonUiModel].
 *
 * Separated from [ButtonComposer] to make it clear all the required dependencies in the
 * explicit function as input types affect Compose's ability to memoize recomposition calls.
 *
 * @param model The ui model for rendering the button.
 * @param colorUtility Used to request color resources.
 * @param modifier Top level modifier for customizing size of the button.
 */
@Composable
private fun ButtonUi(
    colorUtility: ColorUtility,
    model: ButtonUiModel,
    modifier: Modifier
) {
    if (model.buttonState == ButtonState.HIDDEN) {
        return
    }

    onCommit {
        model.uiAction.onShown.invoke()
    }

    val disabledContentColor = colorResource(R.color.disabled_text)

    val backgroundColor = getBackgroundColor(model, colorUtility)
    val buttonWidthPadding = getButtonWidthPadding(model)
    Button(
        onClick = { model.uiAction.onClick(model.clickData) },
        enabled = model.isEnabled(),
        contentColor = getButtonTextColor(model, colorUtility),
        disabledContentColor = disabledContentColor,
        backgroundColor = backgroundColor,
        // Button contains custom logic to switch between backgroundColor and disabledBackgroundColor
        // based on enabled state. The button background color also depends on buttonStyle. By passing
        // the same value, this wrapper maintains control over background color.
        disabledBackgroundColor = backgroundColor,
        padding = Button.DefaultInnerPadding.copy(
            start = buttonWidthPadding,
            end = buttonWidthPadding
        ),
        border = getBorder(model, colorUtility),
        elevation = 0.dp,
        modifier = modifier.minWidthModifier(model).rippleModifier(model).touchModifier(model)
    ) {
        Row(
            verticalGravity = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (model.iconModel?.iconPlacement == IconPlacement.START) {
                iconComposable(model = model.iconModel)
            }

            Text(text = model.buttonText)

            if (model.iconModel?.iconPlacement == IconPlacement.END) {
                iconComposable(model = model.iconModel)
            }
        }
    }
}

@Composable
private fun iconComposable(model: IconModel) {
    val paddingModifier =
        if (model.iconPlacement == IconPlacement.START) Modifier.padding(end = model.iconPadding)
        else Modifier.padding(start = model.iconPadding)
    val iconModifier = paddingModifier
        .size(model.iconWidth + model.iconPadding, model.iconHeight)
    val contentScale = ContentScale.Fit

    when (model.icon) {
        is IconAsset.ImageIcon -> Image(
            asset = model.icon.asset,
            colorFilter = model.colorFilter,
            contentScale = contentScale,
            modifier = iconModifier
        )
        is IconAsset.VectorIcon -> Image(
            asset = model.icon.asset,
            colorFilter = model.colorFilter,
            contentScale = contentScale,
            modifier = iconModifier
        )
    }
}

@Composable
private fun getButtonTextColor(model: ButtonUiModel, colorUtility: ColorUtility): Color {
    return when (model.buttonStyle) {
        ButtonStyle.FILLED -> colorResource(R.color.colorPrimary)
        ButtonStyle.OUTLINE, ButtonStyle.LINK -> colorResource(R.color.colorPrimaryDark)
    }
}

@Composable
private fun getBackgroundColor(model: ButtonUiModel, colorUtility: ColorUtility): Color {
    val disabledBackgroundColor = colorResource(R.color.disabled_bg)

    return if (model.buttonStyle == ButtonStyle.FILLED) {
        if (model.isEnabled()) getButtonColor(model) else disabledBackgroundColor
    } else if (model.buttonStyle == ButtonStyle.OUTLINE && !model.isEnabled()) {
        disabledBackgroundColor
    } else {
        Color.Transparent
    }
}

@Composable
private fun getButtonColor(model: ButtonUiModel): Color {
    return colorResource(R.color.button_normal)
}

@Composable
private fun getButtonWidthPadding(model: ButtonUiModel): Dp {
    if (model.buttonStyle == ButtonStyle.LINK) {
        return 0.dp
    }

    val paddingRes = when (model.buttonPadding) {
        ButtonPadding.NONE -> R.dimen.componentized_none_button_width_padding
        ButtonPadding.DEFAULT -> R.dimen.componentized_default_button_width_padding
        ButtonPadding.COMPACT -> R.dimen.componentized_compact_button_width_padding
        ButtonPadding.LOOSE -> R.dimen.componentized_loose_button_width_padding
    }

    return dimensionResource(paddingRes)
}

@Composable
private fun getBorder(model: ButtonUiModel, colorUtility: ColorUtility): Border? {
    return if (model.buttonStyle == ButtonStyle.OUTLINE && model.isEnabled()) {
        Border(
            dimensionResource(R.dimen.componentized_button_outline),
            Color.Black
        )
    } else {
        null
    }
}

// TODO(b/158513509): Link button should have 0 horizontal padding. Button component currently
// sets a minWidth.
@Composable
private fun Modifier.minWidthModifier(model: ButtonUiModel): Modifier {
    val modifier = if (model.buttonStyle == ButtonStyle.LINK) {
        Modifier.widthIn(minWidth = 0.dp)
    } else if (model.buttonVariant == ButtonVariant.STANDARD) {
        val minWidth = dimensionResource(R.dimen.componentized_standard_button_min_width)
        Modifier.defaultMinSizeConstraints(minWidth = minWidth)
    } else {
        Modifier
    }

    return this + modifier
}

// TODO(b/158674989): Update ripple with stateful color when support available
@Composable
private fun Modifier.rippleModifier(model: ButtonUiModel): Modifier {
    val colorRes = CorpusResourceUtils.getStatefulPrimaryColorResId(model)
    return this + Modifier.clickable(indication = RippleIndication(color = Color.White)) {
        /* do nothing */
    }
}

@Composable
private fun Modifier.touchModifier(model: ButtonUiModel): Modifier {
    return this + Modifier.pressIndicationMotionEventGestureFilter { motionEvent ->
        model.uiAction.onTouch.invoke(model.clickData, motionEvent)
    }
}

