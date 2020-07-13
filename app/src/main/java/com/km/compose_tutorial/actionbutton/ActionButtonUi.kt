package com.km.compose_tutorial.actionbutton

import android.util.Log
import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.core.onPositioned
import androidx.ui.unit.IntSize
import com.km.compose_tutorial.button.ButtonComposer
import com.km.compose_tutorial.button.ButtonUiAction
import com.km.compose_tutorial.button.ButtonUiModel
import com.km.compose_tutorial.button.ColorUtility

/** Composable button container for injecting dependencies. */
@Stable
class ActionButtonComposer(colorUtility: ColorUtility) : ButtonComposer(colorUtility) {

    @Composable
    override fun compose(model: ButtonUiModel, modifier: Modifier) {
        var layoutSize by state(StructurallyEqual) { IntSize(0, 0) }
        val layoutModifier = Modifier.onPositioned {
            layoutSize = it.size
        }

        super.compose(model = createWrapperModel(model, layoutSize), modifier = layoutModifier)
    }

    private fun createWrapperModel(model: ButtonUiModel, layoutSize: IntSize): ButtonUiModel {
        return ButtonUiModel(
            buttonText = model.buttonText,
            uiAction = addClickInterceptor(model, layoutSize),
            clickData = model.clickData,
            buttonVariant = model.buttonVariant,
            buttonStyle = model.buttonStyle,
            buttonPadding = model.buttonPadding,
            buttonState = model.buttonState,
            iconModel = model.iconModel
        )
    }

    private fun addClickInterceptor(model: ButtonUiModel, layoutSize: IntSize): ButtonUiAction {
        return ButtonUiAction(
            onShown = model.uiAction.onShown,
            onTouch = model.uiAction.onTouch,
            onClick = {
                Log.d("DBG", "${model.buttonText} action button clicked $layoutSize")
                it as ActionButtonClickData
                it.adTrackData = AdTrackData(layoutSize.height, layoutSize.width)
            }
        )
    }
}