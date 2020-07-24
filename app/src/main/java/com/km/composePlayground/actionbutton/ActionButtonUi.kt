package com.km.composePlayground.actionbutton

import androidx.compose.Composable
import androidx.compose.Stable
import androidx.compose.remember
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
        val layoutSize = layoutSize()
        val wrapperModel = createWrapperModel(model, layoutSize)
        super.compose(
            model = wrapperModel,
            modifier = modifier.plus(Modifier.layoutSizeCache(layoutSize = layoutSize))
        )
    }

    private fun createWrapperModel(
        model: ActionButtonUiModel,
        layoutSize: LayoutSize
    ): ButtonUiModel {
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
        layoutSize: LayoutSize
    ): ButtonUiAction {
        return ButtonUiAction(
            onShown = model.uiAction.onShown,
            onTouch = model.uiAction.onTouch,
            onClick = {
                model.clickData as ActionButtonClickData
                model.clickData.adTrackData =
                    AdTrackData(layoutSize.height, layoutSize.width)
            }
        )
    }
}

@Composable
private fun Modifier.layoutSizeCache(layoutSize: LayoutSize): Modifier {
    return this + Modifier.onPositioned {
        layoutSize.update(it.size)
    }
}

@Composable
private fun layoutSize(): LayoutSize {
    return remember { LayoutSize(0, 0) }
}

private class LayoutSize(var width: Int, var height: Int) {
    fun update(size: IntSize) {
        width = size.width
        height = size.height
    }
}
