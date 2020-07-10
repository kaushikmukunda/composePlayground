package com.km.compose_tutorial.base

interface UiAdapter<T : UiModel> {
  val uiModel: T
}