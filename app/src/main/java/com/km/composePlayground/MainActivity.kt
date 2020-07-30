package com.km.composePlayground

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.unit.dp

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
        }

    }

}
