package com.km.compose_tutorial.actionbutton

import android.view.MotionEvent
import com.km.compose_tutorial.button.ButtonUiModel

class AdTrackData(
    val viewHeight: Int,
    val viewWidth: Int
)

class ActionButtonClickData(
    var adTrackData : AdTrackData? = null
)

