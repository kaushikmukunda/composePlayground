package com.km.composePlayground.customView

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.padding
import androidx.ui.unit.dp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomViewActivity : AppCompatActivity() {

    private class Model(val text1: String, val text2: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ScreenContent() }
    }

    @Composable
    private fun ScreenContent() {
        val model = state { Model("line1", "line2") }

        Column(modifier = Modifier.padding(8.dp)) {
            Text(model.value.text1)
            Text(model.value.text2)
        }

        suspend fun updateText() {
            delay(1000)
            model.value = Model(model.value.text1, "delayed text2")
        }

        MainScope().launch() {
            updateText()
        }

    }
}