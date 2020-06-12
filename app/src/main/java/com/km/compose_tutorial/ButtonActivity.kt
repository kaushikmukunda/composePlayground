package com.km.compose_tutorial

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButton
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Favorite
import androidx.ui.res.colorResource
import androidx.ui.res.imageResource
import androidx.ui.unit.dp

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
        ButtonComposable(
            config = ButtonConfig(
                uiAction = ButtonUiAction({ _, _ -> }, {}),
                text = "link-abcd",
                backgroundColor = Color.Transparent,
                clickData = Any()

            )
        )

        Spacer(modifier = Modifier.padding(8.dp))

        ButtonComposable(
            config = ButtonConfig(
                uiAction = ButtonUiAction({ _, _ -> }, {}),
                text = "link",
                backgroundColor = Color.Transparent,
                clickData = Any()
            )
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            text = { Text(text = "Fill_no_elevation") },
            elevation = 0.dp,
            onClick = {}
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            text = { Text(text = "Default") },
            onClick = {}
        )

        Spacer(modifier = Modifier.padding(8.dp))

        OutlinedButton(
            text = { Text(text = "Secondary") },
            onClick = {}
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            text = {
                Row {
                    Text(text = "Standard size")
                }
            },
            onClick = {},
            modifier = Modifier.defaultMinSizeConstraints(minWidth = 160.dp) + Modifier.wrapContentWidth(),
            backgroundColor = colorResource(id = R.color.stateful_color)
        )

        IconButton(onClick = { /* doSomething() */ }) {
            Icon(Icons.Filled.Favorite)
        }

    }
}

