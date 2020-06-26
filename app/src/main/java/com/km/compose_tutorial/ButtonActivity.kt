package com.km.compose_tutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.graphics.BlendMode
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.res.imageResource
import androidx.ui.res.vectorResource
import androidx.ui.unit.dp
import com.km.compose_tutorial.button.*
import com.km.compose_tutorial.buttongroup.ButtonGroupComposer
import com.km.compose_tutorial.buttongroup.ButtonGroupUiModel
import com.km.compose_tutorial.buttongroup.ButtonGroupVariant
import com.km.compose_tutorial.buttongroup.ButtonDataModel

class ButtonActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        ButtonGroupScreenContent()
//        ButtonScreenContent()
      }
    }
  }
}

@Composable
private fun ButtonGroupScreenContent() {
  val buttonGroupComposer = ButtonGroupComposer(ButtonComposer(ColorUtility()))

  Column(modifier = Modifier.padding(all = 17.dp)) {
    buttonGroupComposer.compose(
      model = ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.OUTLINE_INVISIBLE,
        firstButtonModel = ButtonDataModel(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonModel = ButtonDataModel(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        numButtons = 2
      )
    )

    Spacer(modifier = Modifier.preferredHeight(8.dp))

    buttonGroupComposer.compose(
      model = ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.INVISIBLE_FILL,
        firstButtonModel = ButtonDataModel(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonModel = ButtonDataModel(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        numButtons = 2
      ),
      modifier = Modifier.width(300.dp)
    )

    Spacer(modifier = Modifier.preferredHeight(8.dp))

    buttonGroupComposer.compose(
      model = ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.FILL_OUTLINE_50_50,
        firstButtonModel = ButtonDataModel(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonModel = ButtonDataModel(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        numButtons = 2
      ),
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.preferredHeight(8.dp))

    buttonGroupComposer.compose(
      model = ButtonGroupUiModel(
        buttonGroupVariant = ButtonGroupVariant.INVISIBLE_INVISIBLE,
        firstButtonModel = ButtonDataModel(
          buttonText = "button1",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        secondButtonModel = ButtonDataModel(
          buttonText = "button2",
          uiAction = ButtonUiAction({}, {}, { _, _ -> }),
          clickData = Any()
        ),
        numButtons = 2
      ),
      modifier = Modifier.rtl
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
            colorFilter = ColorFilter(Color.Red, BlendMode.srcATop)
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

