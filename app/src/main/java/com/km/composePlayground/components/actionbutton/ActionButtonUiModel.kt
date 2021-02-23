package com.km.composePlayground.components.actionbutton

import com.km.composePlayground.components.button.ButtonUiModel

class AdTrackData(
  val viewHeight: Int,
  val viewWidth: Int
)

interface ActionButtonClickData {
  var adTrackData: AdTrackData
}

typealias ActionButtonUiModel = ButtonUiModel
