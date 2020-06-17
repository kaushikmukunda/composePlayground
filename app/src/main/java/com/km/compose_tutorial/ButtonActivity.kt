package com.km.compose_tutorial

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.ui.core.*
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.BlendMode
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.ImageAsset
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButton
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Favorite
import androidx.ui.res.colorResource
import androidx.ui.res.imageResource
import androidx.ui.res.vectorResource
import androidx.ui.unit.dp
import com.km.compose_tutorial.button.*

class ButtonActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ButtonScreenContent()
            }
        }

        val listOf = listOf("a", "b", "c")
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
                    buttonText = "link1234",
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
                    buttonText = "link1234",
                    buttonStyle = ButtonStyle.OUTLINE,
                    buttonVariant = ButtonVariant.SMALL,
                    uiAction = ButtonUiAction({}, {}, { _, _ -> }),
                    clickData = Any(),
                    iconModel = IconModel(
                        IconAsset.VectorIcon(vectorResource(id = R.raw.ic_open_in_new)),
                        IconPlacement.END
                    )
                )
            )
    }
}

