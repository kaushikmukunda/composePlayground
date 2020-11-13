package com.km.composePlayground.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable

@Stable
interface UiModel

@Stable
interface UniformUiModel<T> : UiModel {
  val content: MutableState<T>
}

@Composable
inline fun <T> UniformUi(
  uiModel: UniformUiModel<T>,
  ui: @Composable (T) -> Unit
) = ui(uiModel.content.value)
