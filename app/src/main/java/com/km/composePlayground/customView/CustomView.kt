package com.km.composePlayground.customView

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp

class CustomView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val cnt = mutableStateOf(0)

    override fun onFinishInflate() {
        super.onFinishInflate()
        setContent(Recomposer.current()) {
            ScreenContent()
        }
    }

    @Composable
    private fun ScreenContent() {
        Column {
            Button(onClick = { cnt.value++ }) { Text("click" )}
            Spacer(modifier = Modifier.height(8.dp))
            Text("Button has been clicked ${cnt.value} times", color= Color.White)
        }

    }
}