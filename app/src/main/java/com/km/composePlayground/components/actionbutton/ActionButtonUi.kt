package com.km.composePlayground.components.actionbutton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.km.composePlayground.components.button.ButtonComposer
import com.km.composePlayground.components.button.ButtonUiAction
import com.km.composePlayground.components.button.ButtonUiModel
import com.km.composePlayground.components.button.ColorUtility

/** Composable button container for injecting dependencies. */
@Stable
class ActionButtonComposer(colorUtility: ColorUtility) : ButtonComposer(colorUtility) {

    @Composable
    override fun compose(model: ActionButtonUiModel, modifier: Modifier) {
        val layoutSize = layoutSize()
        val wrapperModel = createWrapperModel(model, layoutSize)
        super.compose(
            model = wrapperModel,
            modifier = modifier.then(Modifier.layoutSizeCache(layoutSize = layoutSize))
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
                model.uiAction.onClick.invoke(it)
            }
        )
    }
}

@Composable
private fun Modifier.layoutSizeCache(layoutSize: LayoutSize): Modifier {
    return this.then(Modifier.onGloballyPositioned {
        layoutSize.update(it.size)
    })
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
