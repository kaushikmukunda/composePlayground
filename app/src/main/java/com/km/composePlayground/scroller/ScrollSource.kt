package com.km.composePlayground.scroller

import android.util.Log
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

enum class ScrollState {
  Idle,
  Scrolling
}

fun interface ScrollSource {
  fun scrollEvents(): Flow<ScrollState>
}

fun LazyListState.scrollSource(): ScrollSource {
  return ScrollSource {
    snapshotFlow { isScrollInProgress }.map { inProgess ->
      if (inProgess) ScrollState.Scrolling else ScrollState.Idle
    }.distinctUntilChanged()
  }
}

@ExperimentalCoroutinesApi
class NestedConnectionScrollSource : NestedScrollConnection, ScrollSource {

  private val scrollEventsFlow = MutableStateFlow(ScrollState.Idle)
  override fun scrollEvents(): Flow<ScrollState> = scrollEventsFlow.asStateFlow()


  override suspend fun onPreFling(available: Velocity): Velocity {
    scrollEventsFlow.emit(ScrollState.Scrolling)
    return super.onPreFling(available)
  }

  override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
    scrollEventsFlow.emit(ScrollState.Idle)
    return super.onPostFling(consumed, available)
  }
}

class TestScrollConsumer {

  private val sources = mutableMapOf<ScrollSource, Job>()

  fun registerSource(scope: CoroutineScope, source: ScrollSource) {
    if (sources.containsKey(source)) return

    val job = scope.launch {
      source.scrollEvents().collect {
        Log.d("dbg", "got scroll event $it")
      }
    }
    sources[source] = job
  }

  fun unregisterSource(source: ScrollSource) {
    Log.d("dbg", "unregistering scroll")
    sources.remove(source)?.cancel()
  }
}