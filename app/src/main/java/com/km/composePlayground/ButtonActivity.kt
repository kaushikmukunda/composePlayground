package com.km.composePlayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

import com.km.composePlayground.actionbutton.ActionButtonClickData
import com.km.composePlayground.actionbutton.ActionButtonComposer
import com.km.composePlayground.actionbutton.AdTrackData
import com.km.composePlayground.actionbuttongroup.ActionButtonGroupComposer
import com.km.composePlayground.actionbuttongroup.ActionButtonGroupComposer2
import com.km.composePlayground.actionbuttongroup.ActionButtonGroupUiModel
import com.km.composePlayground.button.*
import com.km.composePlayground.buttongroup.ButtonConfig
import com.km.composePlayground.buttongroup.ButtonGroupComposer
import com.km.composePlayground.buttongroup.ButtonGroupUiModel
import com.km.composePlayground.buttongroup.ButtonGroupVariant

class ButtonActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        //        ButtonGroupScreenContent()
//        ButtonScreenContent()
        ActionButtonGroupContent()
//        ActionButtonGroupContent2()
      }
    }
  }
}

@Composable
private fun ActionButtonGroupContent2() {
  val actionbuttonGroupComposer = ActionButtonGroupComposer2(ButtonGroupComposer(ButtonComposer(ColorUtility())))
  actionbuttonGroupComposer.compose(
    ActionButtonGroupUiModel(
      ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.INVISIBLE_FILL,
        firstButtonConfig = ButtonConfig(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonConfig = ButtonConfig(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        )
      )
    ),
    modifier = Modifier.width(300.dp)
  )
}

@Composable
private fun ActionButtonGroupContent() {
  Column {
    val actionbuttonGroupComposer = ActionButtonGroupComposer(ActionButtonComposer(ColorUtility()))
    actionbuttonGroupComposer.compose(
      ActionButtonGroupUiModel(
        ButtonGroupUiModel(
          buttonGroupVariant = ButtonGroupVariant.INVISIBLE_FILL,
          firstButtonConfig = ButtonConfig(
            buttonText = "button1",
            uiAction = ButtonUiAction({}, {}, { _, _ -> }),
            clickData = object : ActionButtonClickData {
              override var adTrackData: AdTrackData = AdTrackData(0, 0)
            }
          ),
          secondButtonConfig = ButtonConfig(
            buttonText = "button2",
            uiAction = ButtonUiAction({}, {}, { _, _ -> }),
            clickData = object : ActionButtonClickData {
              override var adTrackData: AdTrackData = AdTrackData(0, 0)
            }
          )
        )
      ),
      modifier = Modifier.width(300.dp)
    )
  }
}

@Composable
private fun ButtonGroupScreenContent() {
  val buttonGroupComposer = ButtonGroupComposer(ButtonComposer(ColorUtility()))

  Column(modifier = Modifier.padding(all = 17.dp)) {
    buttonGroupComposer.compose(
      model = ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.OUTLINE_INVISIBLE,
        firstButtonConfig = ButtonConfig(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonConfig = ButtonConfig(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        )
      )
    )

    Spacer(modifier = Modifier.preferredHeight(8.dp))

    buttonGroupComposer.compose(
      model = ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.INVISIBLE_FILL,
        firstButtonConfig = ButtonConfig(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonConfig = ButtonConfig(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        )
      ),
      modifier = Modifier.width(300.dp)
    )

    Spacer(modifier = Modifier.preferredHeight(8.dp))

    buttonGroupComposer.compose(
      model = ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.FILL_OUTLINE_50_50,
        firstButtonConfig = ButtonConfig(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonConfig = ButtonConfig(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        )
      ),
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.preferredHeight(8.dp))

    buttonGroupComposer.compose(
      model = ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.INVISIBLE_INVISIBLE,
        firstButtonConfig = ButtonConfig(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonConfig = ButtonConfig(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        )
      )
    )
  }
}

@Composable
private fun ButtonScreenContent() {
  Column(modifier = Modifier.padding(all = 16.dp)) {
    ButtonComposer(ColorUtility())
      .compose(
        model = ButtonUiModel(
          buttonText = "link",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any(),
          iconModel = IconModel(
            IconAsset.ImageIcon(imageResource(id = android.R.drawable.ic_btn_speak_now)),
            IconPlacement.START,
            iconPadding = 0.dp
          )
        )
      )

    Spacer(modifier = Modifier.padding(8.dp))

    ButtonComposer(ColorUtility())
      .compose(
        model = ButtonUiModel(
          buttonText = "link",
          buttonStyle = ButtonStyle.LINK,
          buttonVariant = ButtonVariant.SMALL,
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any(),
          iconModel = IconModel(
            IconAsset.VectorIcon(vectorResource(id = R.drawable.ic_open_in_new)),
            IconPlacement.END,
            iconPadding = 0.dp,
            colorFilter = ColorFilter(Color.Red, BlendMode.SrcAtop)
          )
        )
      )

    Spacer(modifier = Modifier.preferredHeight(8.dp))

    ButtonComposer(ColorUtility())
      .compose(
        model = ButtonUiModel(
          buttonText = "lin",
          buttonStyle = ButtonStyle.LINK,
          buttonVariant = ButtonVariant.SMALL,
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        )
      )

    Spacer(modifier = Modifier.preferredHeight(8.dp))

    ButtonComposer(ColorUtility())
      .compose(
        model = ButtonUiModel(
          buttonText = "link",
          buttonStyle = ButtonStyle.OUTLINE,
          buttonVariant = ButtonVariant.SMALL,
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any(),
          iconModel = IconModel(
            IconAsset.VectorIcon(vectorResource(id = R.drawable.ic_open_in_new)),
            IconPlacement.END
          )
        )
      )
  }
}

