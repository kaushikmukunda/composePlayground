package com.km.compose_tutorial

import android.util.Log
import android.view.MotionEvent
import androidx.compose.Composable
import androidx.compose.onActive
import androidx.compose.onDispose
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.core.composed
import androidx.ui.core.gesture.pressIndicatorGestureFilter
import androidx.ui.foundation.Interaction
import androidx.ui.foundation.InteractionState
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.layout.InnerPadding
import androidx.ui.material.Button
import androidx.ui.material.IconButton
import androidx.ui.unit.dp

class ButtonUiAction(
    val onTouchListener: (Any, MotionEvent) -> Unit,
    val onClickListener: (Any) -> Unit
)

class ButtonConfig(
    val uiAction: ButtonUiAction,
    val text: CharSequence,
    val isEnabled: Boolean = true,
    val backgroundColor: Color,
    val clickData: Any
)

enum class Foo {
    BAR,
    BAZ
}

@Composable
fun ButtonComposable(config: ButtonConfig) {
    val interactionState = remember { InteractionState() }
    val color = when (Interaction.Pressed) {
        in interactionState -> Color.Red
        else -> Color.Transparent
    }

    onActive(callback = {
        Log.d("DBG", "on shown : ${config.text}")
    })

    Button(
        onClick = {
            config.uiAction.onClickListener.invoke(config.clickData)
        },
        text = { Text(text = config.text.toString()) },
        enabled = config.isEnabled,
        backgroundColor = config.backgroundColor,
        elevation = 0.dp,
        padding = InnerPadding(
            all = 0.dp
        ),
        modifier =
        Modifier.pressIndicationMotionEventGestureFilter { motionEvent ->
            Log.d("DBG", "got motion event : ${motionEvent.action}")
        }
    )
}

fun Modifier.pressIndicationMotionEventGestureFilter(listener: (MotionEvent) -> Unit): Modifier {
    return composed {
        val actionDownEvents = remember { mutableListOf<MotionEvent>() }

        onDispose(callback = {
            for (event in actionDownEvents) {
                event.recycle()
            }
        })

        Modifier.pressIndicatorGestureFilter(
            onStart = {
                val ts = System.currentTimeMillis()
                val motionEvent =
                    MotionEvent.obtain(ts, ts, MotionEvent.ACTION_DOWN, it.x.value, it.y.value, 0)
                listener.invoke(motionEvent)
                actionDownEvents.add(motionEvent)
            },
            onStop = {
                val ts = System.currentTimeMillis()
                val motionEvent =
                    MotionEvent.obtain(ts, ts, MotionEvent.ACTION_UP, 0f, 0f, 0)
                listener.invoke(motionEvent)
                removeLastOrNull(actionDownEvents)?.recycle()
            },
            onCancel = {
                val ts = System.currentTimeMillis()
                val motionEvent =
                    MotionEvent.obtain(ts, ts, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
                listener.invoke(motionEvent)
                removeLastOrNull(actionDownEvents)?.recycle()
            })
    }
}

private fun <T> removeLastOrNull(list: MutableList<T>): T? =
    if (list.isEmpty()) null else list.removeAt(list.lastIndex)

