package com.km.composePlayground.scratchpad

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import com.km.composePlayground.delimiterFlowRow.BulletDelimiter
import com.km.composePlayground.delimiterFlowRow.DelimiterFlowLayout
import com.km.composePlayground.modifiers.rememberState

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

        Spacer(modifier = Modifier.padding(top=16.dp))

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
    val shouldShow1: Boolean = true,
    val delay1: Int = ANIM_DURATION,
    val shouldShow2: Boolean = false,
    val delay2: Int = ANIM_DURATION
)

private const val ANIM_DURATION = 300

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun AnimatingText() {
    Log.d("dbg", ">>> composing outer")
    Stack {
        Log.d("dbg", ">>> composing stack ")
        var animState = rememberState { AnimState() }
        AnimatedVisibility(
            visible = animState.value.shouldShow1,
            enter = fadeIn(
                animSpec = tween(
                    durationMillis = ANIM_DURATION,
                    delayMillis = animState.value.delay1
                )
            ),
            exit = fadeOut(
                animSpec = tween(durationMillis = ANIM_DURATION)
            )
        ) {
            Log.d("dbg", "composing inner 1")
            Text("First Line", Modifier.fillMaxWidth().height(200.dp))
            if (animState.value.shouldShow1) {
                Handler().postDelayed({
                    Log.d("dbg", "updating 1")
                    animState.value = AnimState(shouldShow1 = false, shouldShow2 = true)
                }, 1300)
            }
        }

        AnimatedVisibility(
            visible = animState.value.shouldShow2,
            enter = fadeIn(
                animSpec = tween(
                    durationMillis = ANIM_DURATION,
                    delayMillis = animState.value.delay2
                )
            ),
            exit = fadeOut(
                animSpec = tween(durationMillis = ANIM_DURATION)
            )
        ) {
            Log.d("dbg", "composing inner 2")
            Text("Next Line", Modifier.fillMaxWidth().height(200.dp))
            if (animState.value.shouldShow2) {
                Handler().postDelayed({
                    Log.d("dbg", "updating 2")
                    animState.value = AnimState(shouldShow1 = true, shouldShow2 = false)
                }, 1300)
            }
        }
    }
}



