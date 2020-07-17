package com.km.composePlayground

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.ExperimentalLayout
import androidx.ui.layout.FlowRow
import androidx.ui.material.*
import androidx.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MyScreenContent()
    }
  }
}

@Composable
fun MyApp(children: @Composable() () -> Unit) {
  MaterialTheme {
    Surface(color = Color.LightGray) {
      children()
    }
  }
}

@Composable
fun MyScreenContent(
  names: List<String> = listOf("Android", "Compose", "Jetpack"),
  counterState: CounterState = CounterState()
) {
  Column {
    for (name in names) {
      Greeting(name = name)
      Divider(color = Color.Black)
    }
    Counter(counterState)
    CheckBox(state = FormState())
  }
  Log.d("DBG", "composing ScreenContent")
}

@OptIn(ExperimentalLayout::class)
@Composable
fun Greeting(name: String) {
  FlowRow {
    for (i in 0..10) {
      Text("word $i")
    }
  }
}

class CounterState(var count: Int = 0)

@Composable
fun Counter(state: CounterState) {
  Button(onClick = { state.count++ }) {
    Text("Clicked ${state.count} times")
    Log.d("DBG", "composing com.km.compose_tutorial.button")
  }
  Log.d("DBG", "composing Counter")
}

class FormState(var optionChecked: Boolean = false)

@Composable
fun CheckBox(state: FormState) {
  Checkbox(checked = state.optionChecked, onCheckedChange = {
    state.optionChecked = it
  })
}

@Preview
@Composable
fun DefaultPreview() {
  MyApp { MyScreenContent() }
}