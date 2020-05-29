package com.km.compose_tutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.DimenRes
import androidx.compose.Composable
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.ltr
import androidx.ui.layout.padding
import androidx.ui.layout.rtl
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp

class FlowRowActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ScreenContent()
            }
        }

        val listOf = listOf("a", "b", "c")
    }
}

private fun getBulletedFlowRowConfig(numLines: Int): BulletedFlowLayoutConfig {
    return BulletedFlowLayoutConfig(
        areBulletsShown = true,
        horizontalSpacing = 24.dp,
        bulletRadius = 2.dp,
        numLines = numLines,
        bulletColor = Color.Black
    )
}

@Composable
fun ScreenContent() {
    Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
        Text("********** numlines1 *************\n\n")

        pixelResource(id = R.dimen.medium_padding)

        BulletedFlowLayout(
            config = getBulletedFlowRowConfig(1),
            modifier = Modifier.rtl
        ) {
            Text("some ads")
            Text("no in-game purchases")
            Text("total excitement")
            Text("non-stop fun")
        }

        Text("\n\n********** numlines2+rtl *********\n\n")

        BulletedFlowLayout(
            config = getBulletedFlowRowConfig(2),
            modifier = Modifier.rtl
        ) {
            Text("some ads")
            Text("no in-game purchases")
            Text("total excitement")
            Text("non-stop fun")
        }

        Text("\n\n********** numlines3 *************\n\n")

        BulletedFlowLayout(
            config = getBulletedFlowRowConfig(3),
            modifier = Modifier.ltr
        ) {
            Text("some ads")
            Text("no in-game purchases")
            Text("total excitement")
            Text("non-stop fun")
            Text("no in-game purchases2")
            Text("total excitement2")
            Text("non-stop fun2")
        }
    }
}


@Composable
fun pixelResource(@DimenRes id: Int): Float {
    return ContextAmbient.current.resources.getDimensionPixelSize(id).toFloat()
}

