package com.km.composePlayground.scroller

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

class ScrollerActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        DynamicLazyRow()
      }
    }
  }
}


val testList = listOf(
  "Hello World", "World Hello", "Foo Bar Baz",
  "Random 1", "Random 2", "Random 3",
  "Random 4", "Random 5", "Random 6",
  "Random 7", "Random 8", "Random 9",
)

@Composable
fun DynamicLazyRow() {
  WithConstraints(modifier = Modifier.fillMaxWidth()) {
    val maxItemSize = 90.dp
    val peekAmount = 30.dp
    val itemsPerWidth = if (maxWidth > 600.dp) 5 else 3
    val itemContainerSize = (maxWidth - peekAmount) / itemsPerWidth
    val itemSize = min(itemContainerSize, maxItemSize)
    val additionalPadding = itemContainerSize - itemSize
    Log.d("dbg", "$maxWidth")

    Box(alignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
//      LazyRowFor(testList.subList(0, 1)) {
      LazyRowFor(testList) {
        Log.d("dbg>>$", "composing $it")
        MyItem(text = it, modifier = Modifier.padding(horizontal = additionalPadding / 2).size(itemSize))
      }
    }
  }
}

@Composable
fun MyItem(text: String, modifier: Modifier = Modifier) {
  Text(text = text, modifier = modifier
    .background(color = Color.Gray)
    .border(width = 1.dp, color = Color.Red))
}