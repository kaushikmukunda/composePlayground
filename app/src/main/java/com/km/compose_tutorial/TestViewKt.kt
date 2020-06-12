package com.km.compose_tutorial

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.Recomposer
import androidx.ui.core.setContent
import androidx.ui.foundation.Text

class TestViewKt(context: Context) : FrameLayout(context) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        setContent(Recomposer.current()) {
            Text(text = "abcd")
        }
    }
}