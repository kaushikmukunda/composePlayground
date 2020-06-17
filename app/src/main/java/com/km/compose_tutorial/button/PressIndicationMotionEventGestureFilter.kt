package com.km.compose_tutorial.button

import android.view.MotionEvent
import androidx.compose.onDispose
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.core.composed
import androidx.ui.core.gesture.pressIndicatorGestureFilter
import androidx.ui.util.fastForEach

/**
 * This gesture detector wraps around [Modifier.pressIndicatorGestureFilter] and converts
 * a press event to legacy MotionEvent.
 */
internal fun Modifier.pressIndicationMotionEventGestureFilter(
    listener: (MotionEvent) -> Unit
): Modifier {
    return composed {
        // There ought to be only ever one action down event
        val actionDownEvents = remember { mutableListOf<MotionEvent>() }

        onDispose {
            actionDownEvents.fastForEach { it.recycle() }
            actionDownEvents.clear()
        }

        // TODO(b/158767049): Replace with a more appropriate gesture filter when available
        // Note that onStop, onCancel don't provide the required event co-ordinates, using 0 for now.
        Modifier.pressIndicatorGestureFilter(
            onStart = {
                val ts = System.currentTimeMillis()
                val motionEvent =
                    MotionEvent.obtain(ts, ts, MotionEvent.ACTION_DOWN, it.x.value, it.y.value, /* metaState= */ 0)
                listener.invoke(motionEvent)
                actionDownEvents.add(motionEvent)
            },
            onStop = {
                val ts = System.currentTimeMillis()
                val motionEvent = MotionEvent.obtain(
                    ts,
                    ts,
                    MotionEvent.ACTION_UP,
                    /* x= */ 0f,
                    /* y= */ 0f,
                    /* metaState= */ 0
                )
                listener.invoke(motionEvent)
                // Stop completes previous press event, remove the action down event.
                removeLastOrNull(actionDownEvents)?.recycle()
            },
            onCancel = {
                val ts = System.currentTimeMillis()
                val motionEvent = MotionEvent.obtain(
                    ts,
                    ts,
                    MotionEvent.ACTION_CANCEL,
                    /* x= */ 0f,
                    /* y= */ 0f,
                    /* metaState= */ 0
                )
                listener.invoke(motionEvent)
                // Cancel completes previous press event, remove the action down event.
                removeLastOrNull(actionDownEvents)?.recycle()
            }
        )
    }
}

// This is available as an extension function to MutableList but is marked experimental. Using that
// would mean annotating the entire compose chain with the experimental annotation. To avoid that,
// creating a local version.
private fun <T> removeLastOrNull(list: MutableList<T>): T? =
    if (list.isEmpty()) null else list.removeAt(list.lastIndex)
