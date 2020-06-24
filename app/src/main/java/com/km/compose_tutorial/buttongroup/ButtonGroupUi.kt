package com.km.compose_tutorial.buttongroup

import androidx.compose.*
import androidx.ui.core.*
import androidx.ui.foundation.Box
import androidx.ui.layout.*
import androidx.ui.unit.*
import com.km.compose_tutorial.button.*
import kotlin.math.floor


/** Composable button container for injecting dependencies. */
@Stable
class ButtonGroupComposer constructor(private val buttonComposer: ButtonComposer) {

  @Composable
  fun compose(model: ButtonGroupUiModel, modifier: Modifier = Modifier) {
    ButtonGroupUi(buttonComposer, model, modifier)
  }
}

@OptIn(ExperimentalLayout::class)
@Composable
private fun ButtonGroupUi(buttonComposer: ButtonComposer,
                          model: ButtonGroupUiModel,
                          modifier: Modifier) {
  if (model.numButtons == 0) {
    return
  }

  var layoutSize by state(StructurallyEqual) { IntSize(0, 0) }
  val layoutModifier = Modifier.onPositioned {
    layoutSize = it.size
  }

  // Wrap the FlowRow within a Box to accomodate the layout modifier. FlowRow does not accept a
  // modifier. This is required to resize the Button for the 50_50 group variant.
  Box(modifier = modifier + layoutModifier) {
    val isLtr = ConfigurationAmbient.current.localeLayoutDirection == LayoutDirection.Ltr
    val buttonSpacing = getButtonGroupSpacing(model)

    FlowRow(
      // This allows the flowRow to take up the entire available space
      mainAxisSize = SizeMode.Expand,
      mainAxisAlignment = getLayoutDirectionAwareButtonAlignment(model, isLtr),
      mainAxisSpacing = buttonSpacing,
      crossAxisSpacing = buttonSpacing
    ) {
      val buttonWidthModifier = Modifier.buttonWidthConstraints(model, IntSize(layoutSize.width, layoutSize.height))

      val leftButton = getLeftButtonModel(model)
      val rightButton = getRightButtonModel(model)

      // FlowRow does not support RTL out of the box currently and we implement it manually
      if (isLtr) {
        leftButton?.let {
          buttonComposer.compose(model = it, modifier = buttonWidthModifier)
        }
        buttonComposer.compose(model = rightButton, modifier = buttonWidthModifier)
      } else {
        buttonComposer.compose(model = rightButton, modifier = buttonWidthModifier)
        leftButton?.let {
          buttonComposer.compose(model = it, modifier = buttonWidthModifier)
        }
      }
    }
  }
}

private fun getLayoutDirectionAwareButtonAlignment(model: ButtonGroupUiModel, isLtr: Boolean): MainAxisAlignment {
  val ltrButtonAlignment = getButtonAlignment(model)
  return if (isLtr) ltrButtonAlignment else {
    if (ltrButtonAlignment == MainAxisAlignment.Start) {
      MainAxisAlignment.End
    } else {
      MainAxisAlignment.Start
    }
  }
}

private fun getButtonAlignment(model: ButtonGroupUiModel): MainAxisAlignment {
  return when (model.buttonGroupVariant) {
    ButtonGroupVariant.FILL_INVISIBLE,
    ButtonGroupVariant.OUTLINE_INVISIBLE -> MainAxisAlignment.Start

    ButtonGroupVariant.INVISIBLE_FILL -> MainAxisAlignment.End

    else -> if (model.buttonGroupSnapping == ButtonGroupSnapping.LEFT) {
      MainAxisAlignment.Start
    } else {
      MainAxisAlignment.End
    }
  }
}

@Composable
private fun Modifier.buttonWidthConstraints(model: ButtonGroupUiModel,
                                            layoutSize: IntSize): Modifier {
  val maxWidth = 300.dp

  return when (model.buttonGroupVariant) {
    ButtonGroupVariant.OUTLINE_FILL_50_50,
    ButtonGroupVariant.FILL_OUTLINE_50_50,
    ButtonGroupVariant.OUTLINE_OUTLINE_50_50 -> {
      val buttonSpacing = getButtonGroupSpacing(model)
      val totalWidthDp =
        Dp((layoutSize.width) / ContextAmbient.current.resources.displayMetrics.density)
      // Use floor as any overflow will cause the FlowRow to layout the second button on the
      // next row.
      val buttonWidth = ((totalWidthDp - buttonSpacing) / 2).floor()

      // ButtonWidth would be < 0.dp before the composable is positioned (onPositionedModifier).
      if (buttonWidth > 0.dp) {
        this.widthIn(minWidth = buttonWidth, maxWidth = maxWidth)
      } else {
        this.widthIn(maxWidth = maxWidth)
      }
    }
    else -> this.widthIn(maxWidth = maxWidth)
  }
}

@Composable
private fun getButtonGroupSpacing(model: ButtonGroupUiModel): Dp {
  if (model.numButtons < 2 || model.secondButtonModel.buttonState == ButtonState.HIDDEN) {
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
private fun getRightButtonModel(model: ButtonGroupUiModel): ButtonUiModel {
  return if (model.numButtons == 1) {
    if (model.isSingleButtonSecondary) {
      generateSecondaryButtonModelForVariant(model, model.firstButtonModel)
    } else {
      generatePrimaryButtonModelForVariant(model, model.firstButtonModel)
    }
  } else {
    generatePrimaryButtonModelForVariant(model, model.secondButtonModel)
  }
}

@Composable
private fun getLeftButtonModel(model: ButtonGroupUiModel): ButtonUiModel? {
  if (model.numButtons < 2) {
    return null
  }

  return generateSecondaryButtonModelForVariant(model, model.firstButtonModel)
}

private fun generatePrimaryButtonModelForVariant(
  groupModel: ButtonGroupUiModel,
  buttonModel: ButtonDataModel
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

  return buttonUiModelFrom(buttonModel, buttonStyle, padding)
}


private fun generateSecondaryButtonModelForVariant(
  groupModel: ButtonGroupUiModel,
  buttonModel: ButtonDataModel
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

  return buttonUiModelFrom(buttonModel, buttonStyle, padding)
}

private fun buttonUiModelFrom(
  buttonModel: ButtonDataModel,
  buttonStyle: ButtonStyle,
  padding: ButtonPadding
): ButtonUiModel {
  return ButtonUiModel(
    buttonText = buttonModel.buttonText,
    uiAction = buttonModel.uiAction,
    clickData = buttonModel.clickData,
    buttonStyle = buttonStyle,
    buttonPadding = padding,
    buttonState = buttonModel.buttonState,
    iconModel = buttonModel.iconModel
  )
}

private fun Dp.floor(): Dp = Dp(floor(this.value))


