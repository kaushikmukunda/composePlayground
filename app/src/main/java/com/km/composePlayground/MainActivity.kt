package com.km.composePlayground

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import com.km.composePlayground.scratchpad.ScratchPadActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MyScreenContent()
            }
        }
    }

    @Composable
    fun MyScreenContent() {
        Column(modifier = Modifier.padding(8.dp)) {
            Button(onClick = {
                startActivity(Intent(baseContext, FlowRowActivity::class.java))
            }) {
                Text("Delimiter Flow")
            }

            Spacer(modifier = Modifier.preferredHeight(8.dp))

            Button(onClick = {
                startActivity(Intent(baseContext, ButtonActivity::class.java))
            }) {
                Text("Buttons")
            }

            Spacer(modifier = Modifier.preferredHeight(8.dp))

            Button(onClick = {
                startActivity(Intent(baseContext, ConstrainLayoutTestActivity::class.java))
            }) {
                Text("Constraint Layout")
            }

            Spacer(modifier = Modifier.preferredHeight(8.dp))

            Button(onClick = {
                startActivity(Intent(baseContext, ScratchPadActivity::class.java))
            }) {
                Text("ScratchPad Activity")
            }
        }

    }

}
