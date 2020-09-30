package com.km.composePlayground.buttongroup

import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.onPositioned
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.LayoutDirectionAmbient
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.km.composePlayground.button.*
import kotlin.math.floor

/** Composable button group container for injecting dependencies. */
@Stable
open class ButtonGroupComposer(private val buttonComposer: ButtonComposer) {

    @Composable
    fun compose(model: ButtonGroupUiModel, modifier: Modifier = Modifier) {
        ButtonGroupUi(buttonComposer, model, modifier)
    }
}

@OptIn(ExperimentalLayout::class)
@Composable
private fun ButtonGroupUi(
    buttonComposer: ButtonComposer,
    model: ButtonGroupUiModel,
    modifier: Modifier
) {
    if (model.numButtons == 0) {
        return
    }

    var layoutSize by state(structuralEqualityPolicy()) { IntSize(0, 0) }
    val layoutModifier = Modifier.onPositioned {
        layoutSize = it.size
    }

    // Wrap the FlowRow within a Box to accomodate the layout modifier as FlowRow does not accept one.
    val isLtr = LayoutDirectionAmbient.current == LayoutDirection.Ltr
    Box(
        gravity = getLayoutDirectionAwareContentGravity(model, isLtr),
        modifier = modifier.containerWidthModifier(model) + layoutModifier
    ) {
        val buttonSpacing = getButtonGroupSpacing(model)

        FlowRow(mainAxisSpacing = buttonSpacing, crossAxisSpacing = buttonSpacing) {
            val buttonWidthModifier = Modifier.buttonWidthConstraints(model, layoutSize)

            val leftButtonUiModel = createLeftButtonUiModel(model)
            val rightButtonUiModel = createRightButtonUiModel(model)

            // TODO(b/159812991): Once FlowRow supports RTL out of the box, refactor the manual mangling.
            if (isLtr) {
                leftButtonUiModel?.let {
                    buttonComposer.compose(model = it, modifier = buttonWidthModifier)
                }
                buttonComposer.compose(model = rightButtonUiModel, modifier = buttonWidthModifier)
            } else {
                buttonComposer.compose(model = rightButtonUiModel, modifier = buttonWidthModifier)
                leftButtonUiModel?.let {
                    buttonComposer.compose(model = it, modifier = buttonWidthModifier)
                }
            }
        }
    }
}

/**
 * The 50_50 group variants require button to take up all the available space. Apply a
 * fillMaxWidth modifier for these cases.
 */
@Composable
private fun Modifier.containerWidthModifier(model: ButtonGroupUiModel): Modifier {
    return when (model.buttonGroupVariant) {
        ButtonGroupVariant.OUTLINE_FILL_50_50,
        ButtonGroupVariant.FILL_OUTLINE_50_50,
        ButtonGroupVariant.OUTLINE_OUTLINE_50_50 -> this.fillMaxWidth()
        else -> this
    }
}

private fun getLayoutDirectionAwareContentGravity(
    model: ButtonGroupUiModel,
    isLtr: Boolean
): ContentGravity {
    val ltrContentGravity = getContentGravity(model)
    return if (isLtr) {
        ltrContentGravity
    } else if (ltrContentGravity == ContentGravity.CenterStart) {
        ContentGravity.CenterEnd
    } else {
        ContentGravity.CenterStart
    }
}

private fun getContentGravity(model: ButtonGroupUiModel): ContentGravity {
    return when (model.buttonGroupVariant) {
        ButtonGroupVariant.FILL_INVISIBLE,
        ButtonGroupVariant.OUTLINE_INVISIBLE -> ContentGravity.CenterStart

        ButtonGroupVariant.INVISIBLE_FILL -> ContentGravity.CenterEnd

        else ->
            if (model.buttonGroupSnapping == ButtonGroupSnapping.LEFT) {
                ContentGravity.CenterStart
            } else {
                ContentGravity.CenterEnd
            }
    }
}

@Composable
private fun Modifier.buttonWidthConstraints(
    model: ButtonGroupUiModel,
    layoutSize: IntSize
): Modifier {
    val maxWidth = 300.dp

    return when (model.buttonGroupVariant) {
        ButtonGroupVariant.OUTLINE_FILL_50_50,
        ButtonGroupVariant.FILL_OUTLINE_50_50,
        ButtonGroupVariant.OUTLINE_OUTLINE_50_50 -> {
            val buttonSpacing = getButtonGroupSpacing(model)
            val totalWidthDp = with(DensityAmbient.current) { layoutSize.width.toDp() }
            // Use floor as any overflow will cause the FlowRow to layout the second button on the
            // next row.
            val buttonWidth = ((totalWidthDp - buttonSpacing) / 2).floor()

            // ButtonWidth would be < 0.dp before the composable is positioned (onPositionedModifier).
            if (buttonWidth > 0.dp) {
                this.widthIn(min = buttonWidth, max = maxWidth)
            } else {
                this.widthIn(max = maxWidth)
            }
        }
        else -> this.widthIn(max = maxWidth)
    }
}

@Composable
private fun getButtonGroupSpacing(model: ButtonGroupUiModel): Dp {
    if (model.numButtons < 2 || model.secondButtonConfig?.buttonState == ButtonState.HIDDEN) {
        return 0.dp
    }

    return when (model.buttonGroupVariant) {
        ButtonGroupVariant.OUTLINE_FILL,
        ButtonGroupVariant.OUTLINE_FILL_50_50,
        ButtonGroupVariant.FILL_OUTLINE,
        ButtonGroupVariant.FILL_OUTLINE_50_50,
        ButtonGroupVariant.OUTLINE_OUTLINE_50_50 ->
            12.dp

        ButtonGroupVariant.INVISIBLE_FILL,
        ButtonGroupVariant.INVISIBLE_INVISIBLE,
        ButtonGroupVariant.FILL_INVISIBLE,
        ButtonGroupVariant.OUTLINE_INVISIBLE ->
            24.dp
    }
}

@Composable
private fun createRightButtonUiModel(model: ButtonGroupUiModel): ButtonUiModel {
    return if (model.numButtons == 1) {
        if (model.isSingleButtonSecondary) {
            generateSecondaryButtonModelForVariant(model, model.firstButtonConfig)
        } else {
            generatePrimaryButtonModelForVariant(model, model.firstButtonConfig)
        }
    } else {
        generatePrimaryButtonModelForVariant(model, requireNotNull(model.secondButtonConfig))
    }
}

@Composable
private fun createLeftButtonUiModel(model: ButtonGroupUiModel): ButtonUiModel? {
    if (model.numButtons < 2) {
        return null
    }

    return generateSecondaryButtonModelForVariant(model, model.firstButtonConfig)
}

private fun generatePrimaryButtonModelForVariant(
    groupModel: ButtonGroupUiModel,
    buttonConfig: ButtonConfig
): ButtonUiModel {
    var buttonStyle = ButtonStyle.FILLED
    var padding = ButtonPadding.DEFAULT

    when (groupModel.buttonGroupVariant) {
        ButtonGroupVariant.OUTLINE_FILL_50_50 -> {
            buttonStyle = ButtonStyle.FILLED
            padding = ButtonPadding.NONE
        }

        ButtonGroupVariant.OUTLINE_FILL,
        ButtonGroupVariant.INVISIBLE_FILL -> {
            buttonStyle = ButtonStyle.FILLED
            padding = ButtonPadding.DEFAULT
        }

        ButtonGroupVariant.INVISIBLE_INVISIBLE,
        ButtonGroupVariant.FILL_INVISIBLE,
        ButtonGroupVariant.OUTLINE_INVISIBLE -> {
            buttonStyle = ButtonStyle.LINK
            padding = ButtonPadding.NONE
        }

        ButtonGroupVariant.FILL_OUTLINE -> {
            buttonStyle = ButtonStyle.OUTLINE
            padding = ButtonPadding.DEFAULT
        }

        ButtonGroupVariant.FILL_OUTLINE_50_50,
        ButtonGroupVariant.OUTLINE_OUTLINE_50_50 -> {
            buttonStyle = ButtonStyle.OUTLINE
            padding = ButtonPadding.NONE
        }
    }

    return buttonUiModelFrom(buttonConfig, buttonStyle, padding)
}

private fun generateSecondaryButtonModelForVariant(
    groupModel: ButtonGroupUiModel,
    buttonConfig: ButtonConfig
): ButtonUiModel {
    var buttonStyle = ButtonStyle.FILLED
    var padding = ButtonPadding.DEFAULT

    when (groupModel.buttonGroupVariant) {
        ButtonGroupVariant.INVISIBLE_FILL,
        ButtonGroupVariant.INVISIBLE_INVISIBLE -> {
            buttonStyle = ButtonStyle.LINK
            padding = ButtonPadding.NONE
        }

        ButtonGroupVariant.OUTLINE_FILL_50_50,
        ButtonGroupVariant.OUTLINE_OUTLINE_50_50 -> {
            buttonStyle = ButtonStyle.OUTLINE
            padding = ButtonPadding.NONE
        }

        ButtonGroupVariant.FILL_OUTLINE,
        ButtonGroupVariant.FILL_INVISIBLE -> {
            buttonStyle = ButtonStyle.FILLED
            padding = ButtonPadding.DEFAULT
        }

        ButtonGroupVariant.FILL_OUTLINE_50_50 -> {
            buttonStyle = ButtonStyle.FILLED
            padding = ButtonPadding.NONE
        }

        ButtonGroupVariant.OUTLINE_FILL,
        ButtonGroupVariant.OUTLINE_INVISIBLE -> {
            buttonStyle = ButtonStyle.OUTLINE
            padding = ButtonPadding.DEFAULT
        }
    }

    return buttonUiModelFrom(buttonConfig, buttonStyle, padding)
}

private fun buttonUiModelFrom(
    buttonConfig: ButtonConfig,
    buttonStyle: ButtonStyle,
    padding: ButtonPadding
): ButtonUiModel {
    return ButtonUiModel(
        buttonText = buttonConfig.buttonText,
        uiAction = buttonConfig.uiAction,
        clickData = buttonConfig.clickData,
        buttonStyle = buttonStyle,
        buttonPadding = padding,
        buttonState = buttonConfig.buttonState,
        iconModel = buttonConfig.iconModel,
//    backend = buttonConfig.backend,
//    theme = buttonConfig.theme
    )
}

private fun Dp.floor(): Dp = Dp(floor(this.value))

private val ButtonGroupUiModel.numButtons: Int
    get() = if (this.secondButtonConfig == null) 1 else 2
