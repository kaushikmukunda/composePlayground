package com.km.composePlayground.scratchpad

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animatedFloat
import androidx.compose.animation.core.AnimatedFloat
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.onCommit
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.gesture.pressIndicatorGestureFilter
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.core.view.get
import com.km.composePlayground.R
import com.km.composePlayground.delimiterFlowRow.BulletDelimiter
import com.km.composePlayground.delimiterFlowRow.DelimiterFlowLayout
import com.km.composePlayground.modifiers.rememberState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScratchPadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(Recomposer.current()) {
            DelimiterFlowAnim()
//            CrossfadeSample()
//            AnimatingText()
        }
    }
}

private val TEST_PHRASES = listOf<@Composable() () -> Unit>(
    { Text("Etiam sit amet") },
    { Text("ex id ipsum") },
    { Text("commodo dictum") },
    { Text("Ut id ex vehicula") },
    { Text("venenatis enim") },
    { Text("feugiat, porta neque") },
    { Text("In venenatis") },
    { Text("neque ac quam aliquam") },
    { Text("tempus vitae sit amet lorem") },
    { Text("Vestibulum accumsan") },
    { Text("nisl eget neque") },
    { Text("aliquam ultricies") },
    { Text("Nullam non leo") },
    { Text("ullamcorper, ornare") },
    { Text("felis nec") },
    { Text("euismod magna") }
)

@Composable
private fun DelimiterFlowAnim() {
    Column {
        DelimiterFlowLayout(
            numLines = 1,
            delimiter = { modifier ->
                BulletDelimiter(
                    bulletRadius = 2.dp,
                    bulletGap = 8.dp,
                    modifier = modifier
                )
            },
            children = TEST_PHRASES
        )

        Spacer(modifier = Modifier.padding(top = 16.dp))

        DelimiterFlowLayout(
            numLines = 10,
            delimiter = { modifier ->
                BulletDelimiter(
                    bulletRadius = 2.dp,
                    bulletGap = 8.dp,
                    modifier = modifier
                )
            },
            children = TEST_PHRASES
        )
    }
}

class AnimState(
    var visible: Boolean = true,
    var current: String,
    var next: String = "",
    var count: Int = 0
)

private const val ANIM_DURATION = 300

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun AnimatingText() {
    val animState = rememberState { AnimState(current = "text") }

    val opacity =
        animatedOpacity(tween(durationMillis = 250, delayMillis = 500), animState.value.visible) {
            Log.d("dbg", "animation end")
            MainScope().launch {
                delay(500)

                var current = animState.value.current
                var next = animState.value.next
                var count = animState.value.count

                if (animState.value.visible) {
                    next = animState.value.current + animState.value.count
                    count++
                } else {
                    current = animState.value.next
                }

                animState.value = AnimState(
                    visible = !animState.value.visible,
                    current = current, next = next, count = count
                )
            }
        }

    Text(animState.value.current, modifier = Modifier.drawOpacity(opacity.value))
}

@Composable
private fun animatedOpacity(
    animation: AnimationSpec<Float>,
    visible: Boolean,
    onAnimationFinish: () -> Unit = {}
): AnimatedFloat {
    val animatedFloat = animatedFloat(if (!visible) 1f else 0f)
    onCommit(visible) {
        animatedFloat.animateTo(
            if (visible) 1f else 0f,
            anim = animation,
            onEnd = { reason, _ ->
                if (reason == AnimationEndReason.TargetReached) {
                    onAnimationFinish()
                }
            })
    }
    return animatedFloat
}


