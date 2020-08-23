package com.km.composePlayground.button

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ripple.RippleIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.onCommit
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.R

/** Composable button container for injecting dependencies. */
@Stable
open class ButtonComposer constructor(private val colorUtility: ColorUtility) {

    @Composable
    open fun compose(model: ButtonUiModel, modifier: Modifier = Modifier) {
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
        onClick = { model.uiAction.onClick(model.clickData) }.clickListener(),
        enabled = model.isEnabled(),
        contentColor = getButtonTextColor(model, colorUtility),
        disabledContentColor = disabledContentColor,
        backgroundColor = backgroundColor,
        // Button contains custom logic to switch between backgroundColor and disabledBackgroundColor
        // based on enabled state. The button background color also depends on buttonStyle. By passing
        // the same value, this wrapper maintains control over background color.
        disabledBackgroundColor = backgroundColor,
        contentPadding = InnerPadding(
            start = buttonWidthPadding,
            end = buttonWidthPadding
        ),
        border = getBorder(model, colorUtility),
        elevation = 0.dp,
        // TODO(b/162462372): Disabling problematic modifiers for now
        modifier = modifier.minSizeModifier(model).touchModifier(model) // .rippleModifier(model)
    ) {
        Row(
            verticalGravity = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            positionalIconComposable(model.iconModel, IconPlacement.START)
            Text(text = model.buttonText)
            positionalIconComposable(model.iconModel, IconPlacement.END)
        }
    }
}

@Composable
private fun positionalIconComposable(model: IconModel?, position: IconPlacement) {
    model?.let {
        if (it.iconPlacement == position) {
            iconUi(it)
        }
    }
}

@Composable
private fun iconUi(model: IconModel) {
    val paddingModifier =
        if (model.iconPlacement == IconPlacement.START) {
            Modifier.padding(end = model.iconPadding)
        } else {
            Modifier.padding(start = model.iconPadding)
        }
    val iconModifier = paddingModifier.size(model.iconWidth, model.iconHeight)
    val contentScale = ContentScale.Fit
    val iconAsset = model.icon
    when (iconAsset) {
        is IconAsset.ImageIcon -> {
            Image(
                asset = iconAsset.asset,
                colorFilter = model.colorFilter,
                contentScale = contentScale,
                modifier = iconModifier
            )
        }
        is IconAsset.VectorIcon -> Image(
            asset = iconAsset.asset,
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
private fun getBorder(model: ButtonUiModel, colorUtility: ColorUtility): BorderStroke? {
    return if (model.buttonStyle == ButtonStyle.OUTLINE && model.isEnabled()) {
        BorderStroke(
            dimensionResource(R.dimen.componentized_button_outline),
            Color.Black
        )
    } else {
        null
    }
}

@Composable
private fun Modifier.minSizeModifier(model: ButtonUiModel): Modifier {
    val stdButtonMinWidth = dimensionResource(R.dimen.componentized_standard_button_min_width)
    // A non-zero width is required to override the default constraint.
    val smallButtonMinWidth = 1.dp
    val stdButtonMinHeight = dimensionResource(R.dimen.componentized_standard_button_height)
    val smallButtonMinHeight = dimensionResource(R.dimen.componentized_small_button_height)

    val minWidth: Dp
    val minHeight: Dp
    if (model.buttonVariant == ButtonVariant.STANDARD) {
        minWidth =
            if (model.buttonStyle != ButtonStyle.LINK) stdButtonMinWidth else smallButtonMinWidth
        minHeight = stdButtonMinHeight
    } else {
        minWidth = smallButtonMinWidth
        minHeight = smallButtonMinHeight
    }

    return this.then(Modifier.defaultMinSizeConstraints(minWidth, minHeight))
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
    return this.then(
        Modifier.pointerInteropFilter { motionEvent ->
            model.uiAction.onTouch.invoke(model.clickData, motionEvent)
            false
        }
    )
}

@Composable
private inline fun (() -> Unit).clickListener(): () -> Unit {
    return {
        Log.d("dbg", "log clickable")
        this()
    }
}
