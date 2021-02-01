package com.km.composePlayground

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.components.actionbutton.ActionButtonClickData
import com.km.composePlayground.components.actionbutton.ActionButtonComposer
import com.km.composePlayground.components.actionbutton.AdTrackData
import com.km.composePlayground.components.actionbuttongroup.ActionButtonGroupComposer
import com.km.composePlayground.components.actionbuttongroup.ActionButtonGroupUiModel
import com.km.composePlayground.components.button.ButtonComposer
import com.km.composePlayground.components.button.ButtonUiAction
import com.km.composePlayground.components.button.ColorUtility
import com.km.composePlayground.components.buttongroup.ButtonConfig
import com.km.composePlayground.components.buttongroup.ButtonGroupComposer
import com.km.composePlayground.components.buttongroup.ButtonGroupUiModel
import com.km.composePlayground.components.buttongroup.ButtonGroupVariant
import com.km.composePlayground.modifiers.LayoutSize
import com.km.composePlayground.modifiers.layoutSize
import com.km.composePlayground.modifiers.layoutSizeCache
import com.km.composePlayground.modifiers.rememberState
import kotlin.math.max

class ConstrainLayoutTestActivity : AppCompatActivity() {

    private val installBarModel = mutableStateOf(InstallBarModel())

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
                Text(
                    "InstallBar section:",
                    modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)
                )
                InstallBarSection(installBarModel.value)
                ConstraintLessRow()
            }
        }
    }

    @Composable
    private fun ConstraintLessRow() {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "smaller string",
//                "A really long string that should overflow button and ellipsize",
                modifier = Modifier.weight(0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Button(onClick = {},
//                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text="click me",
                    maxLines = 1,
                )
            }
        }
    }

    @Composable
    private fun ScreenContentWithConstraints() {
        val leftSectionId = "left"
        val rightSectionId = "right"

        ConstraintLayout(modifier = Modifier.fillMaxWidth(),
            constraintSet = ConstraintSet {
                val leftRef = createRefFor(leftSectionId)
                val rightRef = createRefFor(rightSectionId)

                constrain(leftRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }

                constrain(rightRef) {
                    top.linkTo(parent.top)
                    linkTo(start = leftRef.end, end = parent.end, startMargin = 16.dp, bias = 0f)
                    width = Dimension.fillToConstraints
                }

            }) {
            Box(modifier = Modifier.layoutId(leftSectionId)) {
                Text("left", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(modifier = Modifier.layoutId(rightSectionId)) {
                Text(
                    "A very long line of text that should overflow as this is just too long to fit in a line",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
    private fun InstallBarSection(model: InstallBarModel) {
        val textId = "textRef"
        val buttonId = "buttonRef"
        val layoutSize = layoutSize()

        ConstraintLayout(modifier = Modifier
            .layoutSizeCache(layoutSize)
            .heightIn(min = getMinHeight(layoutSize))
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 20.dp)
            .indication(
                indication = rememberRipple(color = Color.Red),
                interactionState = InteractionState().apply {
                    addInteraction(Interaction.Pressed)
                }
            )
            .clickable() { Log.d("dbg", "clicked") }
            .border(width = 1.dp, color = Color.Red),
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
            InstallBarTextSection(model, modifier = Modifier.layoutId(textId))
            InstallBarButtonSection(modifier = Modifier.layoutId(buttonId))
        }
    }

    @Composable
    private fun getMinHeight(layoutSize: LayoutSize): Dp {
        val minHeight = rememberState { 0 }
        minHeight.value = max(minHeight.value, layoutSize.height)

        return with(DensityAmbient.current) {
            Log.d(
                "dbg",
                "prev max: ${minHeight.value.toDp()} layoutSize: ${layoutSize.height.toDp()}"
            )
            minHeight.value.toDp()
        }
    }

    @Composable
    @OptIn(ExperimentalLayout::class)
    private fun InstallBarButtonSection(modifier: Modifier) {
        Box(modifier = modifier) {
            FlowRow {
                Button(onClick = {
                    installBarModel.value = InstallBarModel(false, false)
                }, modifier = Modifier.padding(end = 16.dp)) { Text("install") }
                Button(onClick = {
                    installBarModel.value = InstallBarModel(true, true)
                }) { Text("cancel") }
            }
        }
    }

    @Composable
    private fun InstallBarTextSection(model: InstallBarModel, modifier: Modifier) {
        Column(modifier = modifier) {
            Text(
                "A really long string that should overflow button and ellipsize",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (model.showLine2) {
                Text(
                    "Another long string that should overflow button and ellipsize",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (model.showLine3) {
                Text("line3")
                Text("line4")
                Text("line5")
                Text("line6")
                Text("line7")
                Text("line8")
            }
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
                ), secondButtonConfig = ButtonConfig(
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

class InstallBarModel(val showLine2: Boolean = true, val showLine3: Boolean = true)
