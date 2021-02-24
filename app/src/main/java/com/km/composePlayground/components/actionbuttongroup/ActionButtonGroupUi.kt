package com.km.composePlayground.components.actionbuttongroup

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.km.composePlayground.components.actionbutton.ActionButtonClickData
import com.km.composePlayground.components.actionbutton.ActionButtonComposer
import com.km.composePlayground.components.actionbutton.AdTrackData
import com.km.composePlayground.components.button.ButtonUiAction
import com.km.composePlayground.components.buttongroup.ButtonConfig
import com.km.composePlayground.components.buttongroup.ButtonGroupComposer
import com.km.composePlayground.components.buttongroup.ButtonGroupUiModel


/** Composable button group container for injecting dependencies. */
@Stable
class ActionButtonGroupComposer(actionButtonComposer: ActionButtonComposer) :
  ButtonGroupComposer(actionButtonComposer) {

  @Composable
  fun compose(model: ActionButtonGroupUiModel, modifier: Modifier = Modifier) {
    compose(model.buttonGroupUiModel, modifier)
  }
}

@Stable
class ActionButtonGroupComposer2(private val buttonGroupComposer: ButtonGroupComposer) {

  @Composable
  fun compose(model: ActionButtonGroupUiModel, modifier: Modifier = Modifier) {
    val buttonModifier = Modifier.onGloballyPositioned {
      Log.d("Dbg", "button positioned ${it.boundsInParent()} ${it.size}")
    }
    buttonGroupComposer.compose(createWrapperGroupUiModel(model), modifier = modifier)
  }

  private fun createWrapperGroupUiModel(model: ActionButtonGroupUiModel): ButtonGroupUiModel {
    return ButtonGroupUiModel(
      buttonGroupVariant = model.buttonGroupUiModel.buttonGroupVariant,
      buttonGroupSnapping = model.buttonGroupUiModel.buttonGroupSnapping,
      isSingleButtonSecondary = model.buttonGroupUiModel.isSingleButtonSecondary,
      firstButtonConfig = createActionButtonConfig(model.buttonGroupUiModel.firstButtonConfig)!!,
      secondButtonConfig = createActionButtonConfig(model.buttonGroupUiModel.secondButtonConfig)
    )
  }

  private fun createActionButtonConfig(buttonConfig: ButtonConfig?): ButtonConfig? {
    if (buttonConfig == null) return null
    return ButtonConfig(
      buttonText = buttonConfig.buttonText,
      uiAction = addClickInterceptor(buttonConfig, IntSize(0, 0)),
      clickData = buttonConfig.clickData,
      buttonState = buttonConfig.buttonState,
      iconModel = buttonConfig.iconModel
    )

  }

  private fun addClickInterceptor(
    buttonConfig: ButtonConfig,
    layoutSize: IntSize
  ): ButtonUiAction {
    return ButtonUiAction(
      onShown = buttonConfig.uiAction.onShown,
      onTouch = buttonConfig.uiAction.onTouch,
      onClick = {
        Log.d("DBG", "${buttonConfig.buttonText} action button clicked $layoutSize")
        it as ActionButtonClickData
        it.adTrackData = AdTrackData(layoutSize.height, layoutSize.width)
      }
    )
  }

}
