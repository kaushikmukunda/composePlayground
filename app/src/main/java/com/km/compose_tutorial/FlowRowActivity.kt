package com.km.compose_tutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.foundation.Canvas
import androidx.ui.foundation.Text
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Paint
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.Dp
import androidx.ui.unit.dp

class FlowRowActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ScreenContent()
//        FlexBoxContent()
            }
        }
    }
}

@Composable
fun ScreenContent() {
    Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp) + Modifier.rtl) {
        BulletSeperatedFlowRow(horizontalSpacing=  8.dp) {
            BulletedText("some ads")
            BulletedText("no in-game purchases")
            BulletedText("total excitement")
            BulletedText("non-stop fun")
        }
    }
}

class FlexBoxBulletSeperatorUiModel(
    val bulletUiModel: BulletUiModel,
    val contentAlignment: Alignment.Horizontal = Alignment.Start,
//  val numLines: Int,
//  val bulletsShown: Boolean,
    val children: List<@Composable() () -> Unit>
)

class BulletUiModel(
    val bulletPaint: Paint,
    val bulletGap: Dp,
    val bulletSize: Float
)

@Composable
fun FlexBoxBulletSeparatorLayout(model: FlexBoxBulletSeperatorUiModel) {
    for (child in model.children) {
        child.invoke()
        Bullet(BulletUiModel(Paint(), 4.dp, 10f))
    }
}

@Composable
internal fun BulletedText(text: String) {
    Row(verticalGravity = Alignment.CenterVertically, modifier = Modifier.ltr) {
        Text(maxLines = 1, text = text)
        Bullet(BulletUiModel(Paint(), 4.dp, 10f))
    }
}

@Composable
internal fun Bullet(model: BulletUiModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier + Modifier.padding(start = model.bulletGap)) {
        Canvas(modifier = Modifier) {
            drawCircle(Offset.zero, model.bulletSize / 2, model.bulletPaint)
        }
    }
}

