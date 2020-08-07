package com.km.composePlayground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.km.composePlayground.actionbutton.ActionButtonClickData
import com.km.composePlayground.actionbutton.ActionButtonComposer
import com.km.composePlayground.actionbutton.AdTrackData
import com.km.composePlayground.actionbuttongroup.ActionButtonGroupComposer
import com.km.composePlayground.actionbuttongroup.ActionButtonGroupUiModel
import com.km.composePlayground.button.ButtonComposer
import com.km.composePlayground.button.ButtonUiAction
import com.km.composePlayground.button.ColorUtility
import com.km.composePlayground.buttongroup.ButtonConfig
import com.km.composePlayground.buttongroup.ButtonGroupComposer
import com.km.composePlayground.buttongroup.ButtonGroupUiModel
import com.km.composePlayground.buttongroup.ButtonGroupVariant

class ConstrainLayoutTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(Recomposer.current()) {
            Column {
                Text("Button section:", modifier = Modifier.padding(bottom = 2.dp))
                ScreenContentWithConstraints()
                Text(
                    "ActionButton section:",
                    modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)
                )
                ScreenContentWithConstraintSet()
            }
        }
    }

    @Composable
    private fun ScreenContentWithConstraints() {
        val textId = "textRef"
        val buttonId = "buttonRef"

        ConstraintLayout(modifier = Modifier.fillMaxWidth(),
            constraintSet = ConstraintSet {
                val textRef = createRefFor(textId)
                val buttonRef = createRefFor(buttonId)

                constrain(textRef) {
                    start.linkTo(parent.start)
                    end.linkTo(buttonRef.start, 40.dp)
                    width = Dimension.fillToConstraints
                }

                constrain(buttonRef) {
                    start.linkTo(textRef.end)
                    end.linkTo(parent.end)
                }

            }) {
            TextSection(modifier = Modifier.layoutId(textId))
            ButtonSection(modifier = Modifier.layoutId(buttonId))
        }
    }

    @Composable
    private fun ScreenContentWithConstraintSet() {
        val textId = "textRef"
        val buttonId = "buttonRef"

        ConstraintLayout(modifier = Modifier.fillMaxWidth().padding(16.dp),
            constraintSet = ConstraintSet {
                val textRef = createRefFor(textId)
                val buttonRef = createRefFor(buttonId)

                constrain(textRef) {
                    start.linkTo(parent.start)
                    end.linkTo(buttonRef.start, 29.dp)
                    width = Dimension.fillToConstraints
                }

                constrain(buttonRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }

            }) {
            TextSection(modifier = Modifier.layoutId(textId))
            ActionButtonSection(modifier = Modifier.layoutId(buttonId))
        }
    }

    @Composable
    private fun TextSection(modifier: Modifier) {
        Column(modifier = modifier) {
            Text(
                "A really long string that should overflow button and ellipsize",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Another long string that should overflow button and ellipsize",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("line3")
        }
    }

    @Composable
    @OptIn(ExperimentalLayout::class)
    private fun ButtonSection(modifier: Modifier) {
        Box(modifier = modifier) {
            FlowRow {
                Button(onClick = {}, modifier = Modifier.padding(end = 16.dp)) { Text("button1") }
                Button(onClick = {}) { Text("button2") }
            }
        }
    }

    @Composable
    private fun ActionButtonSection(modifier: Modifier) {
        val actionbuttonGroupComposer = ActionButtonGroupComposer(
            ActionButtonComposer(ColorUtility())
        )
        actionbuttonGroupComposer.compose(
            ActionButtonGroupUiModel(
                ButtonGroupUiModel(
                    buttonGroupVariant = ButtonGroupVariant.OUTLINE_FILL,
                    firstButtonConfig = ButtonConfig(
                        buttonText = "button1",
                        uiAction = ButtonUiAction({}, {}, { _, _ -> }),
                        clickData = object : ActionButtonClickData {
                            override var adTrackData: AdTrackData = AdTrackData(0, 0)
                        }
                    )
//                    , secondButtonConfig = ButtonConfig(
//                        buttonText = "button2",
//                        uiAction = ButtonUiAction({}, {}, { _, _ -> }),
//                        clickData = object : ActionButtonClickData {
//                            override var adTrackData: AdTrackData = AdTrackData(0, 0)
//                        }
//                    )
                )
            ),
            modifier = modifier
        )
    }

    @Composable
    private fun ButtonGroupSection(modifier: Modifier) {
        val buttonGroupComposer = ButtonGroupComposer(ButtonComposer(ColorUtility()))
        buttonGroupComposer.compose(
            ButtonGroupUiModel(
                buttonGroupVariant = ButtonGroupVariant.INVISIBLE_INVISIBLE,
                firstButtonConfig = ButtonConfig(
                    buttonText = "button1",
                    uiAction = ButtonUiAction({}, {}, { _, _ -> }),
                    clickData = object : ActionButtonClickData {
                        override var adTrackData: AdTrackData = AdTrackData(0, 0)
                    }
                )
                , secondButtonConfig = ButtonConfig(
                    buttonText = "button2",
                    uiAction = ButtonUiAction({}, {}, { _, _ -> }),
                    clickData = object : ActionButtonClickData {
                        override var adTrackData: AdTrackData = AdTrackData(0, 0)
                    }
                )
            ),
            modifier = modifier
        )
    }
}