package com.km.composePlayground.components.actionbutton

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.OnGloballyPositionedModifier
import com.km.composePlayground.components.button.ButtonUiAction
import com.km.composePlayground.components.button.ButtonUiModel
import com.km.composePlayground.components.button.ColorUtility
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ActionButtonComposerTest {

    var actionButtonComposer = ActionButtonComposer(ColorUtility())

    @Composable
    @Test
    fun test() {
        val modifier = Modifier
        actionButtonComposer.compose(model = ButtonUiModel(
            "foo",
            ButtonUiAction({}, {}, { _, _ -> }),
            object : ActionButtonClickData {
                override var adTrackData: AdTrackData = AdTrackData(0, 0)
            }
        ), modifier = modifier)


        modifier.all { it is OnGloballyPositionedModifier }.apply {
            Log.d("dbg", "Got on positioned modifier")
        }
    }
}