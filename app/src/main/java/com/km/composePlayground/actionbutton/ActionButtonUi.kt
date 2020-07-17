package com.km.composePlayground.actionbutton

import android.util.Log
import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.core.onPositioned
import androidx.ui.unit.IntSize
import com.km.composePlayground.button.ButtonComposer
import com.km.composePlayground.button.ButtonUiAction
import com.km.composePlayground.button.ButtonUiModel
import com.km.composePlayground.button.ColorUtility

/** Composable button container for injecting dependencies. */
@Stable
class ActionButtonComposer(colorUtility: ColorUtility) : ButtonComposer(colorUtility) {

    @Composable
    override fun compose(model: ActionButtonUiModel, modifier: Modifier) {
        var layoutSize by state(StructurallyEqual) { IntSize(0, 0) }
        val layoutModifier = Modifier.onPositioned {
            Log.d("dbg", "fire onpositioned ${it.size}")
            layoutSize = it.size
        }

        val wrapperModel = createWrapperModel(model, layoutSize)
        super.compose(model = wrapperModel, modifier = layoutModifier)
    }

    private fun createWrapperModel(model: ActionButtonUiModel, layoutSize: IntSize): ButtonUiModel {
        Log.d("dbg", "recomposing")
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

    private fun addClickInterceptor(
        model: ActionButtonUiModel,
        layoutSize: IntSize
    ): ButtonUiAction {
        return ButtonUiAction(
            onShown = model.uiAction.onShown,
            onTouch = model.uiAction.onTouch,
            onClick = {
                Log.d("DBG", "${model.buttonText} action button clicked $layoutSize")
                model.clickData as ActionButtonClickData
                model.clickData.adTrackData = AdTrackData(layoutSize.height, layoutSize.width)
            }
        )
    }
}