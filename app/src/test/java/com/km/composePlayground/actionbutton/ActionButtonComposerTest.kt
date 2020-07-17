package com.km.composePlayground.actionbutton

import android.util.Log
import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.OnPositionedModifier
import com.km.composePlayground.button.ButtonUiAction
import com.km.composePlayground.button.ButtonUiModel
import com.km.composePlayground.button.ColorUtility
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


        modifier.all { it is OnPositionedModifier }.apply {
            Log.d("dbg", "Got on positioned modifier")
        }
    }
}