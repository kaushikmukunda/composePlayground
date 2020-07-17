package com.km.composePlayground.actionbutton

import com.km.composePlayground.button.ButtonUiModel

class AdTrackData(
    val viewHeight: Int,
    val viewWidth: Int
)

interface ActionButtonClickData {
    var adTrackData: AdTrackData
}

typealias ActionButtonUiModel = ButtonUiModel
