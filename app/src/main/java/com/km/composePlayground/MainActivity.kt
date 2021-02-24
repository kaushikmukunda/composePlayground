package com.km.composePlayground

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.km.composePlayground.codelabs.layout.CodeLabLayoutActivity
import com.km.composePlayground.customView.CustomViewActivity
import com.km.composePlayground.scratchpad.ScratchPadActivity
import com.km.composePlayground.scroller.ScrollerActivity

class MainActivity : ComponentActivity() {
  private val activityMap = mapOf(
    FlowRowActivity::class.java to "Delimiter Flow",
    ButtonActivity::class.java to "Buttons",
    ConstrainLayoutTestActivity::class.java to "Constraint Layout",
    ScratchPadActivity::class.java to "Scratch pad",
    CustomViewActivity::class.java to "Custom View",
    ScrollerActivity::class.java to "Scroller",
    CodeLabLayoutActivity::class.java to "CodeLab Layout",
  )

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
      for (activityEntry in activityMap) {
        Button(onClick = {
          startActivity(Intent(baseContext, activityEntry.key))
        }) {
          Text(activityEntry.value)
        }

        Spacer(modifier = Modifier.heightIn(8.dp))
      }
    }
  }
}
