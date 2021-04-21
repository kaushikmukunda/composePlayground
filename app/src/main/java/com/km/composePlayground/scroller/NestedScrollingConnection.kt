package com.km.composePlayground.scroller

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ScrollState {
  Idle,
  Scrolling
}

interface ScrollSource {
  fun scrollEvents(): Flow<ScrollState>
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