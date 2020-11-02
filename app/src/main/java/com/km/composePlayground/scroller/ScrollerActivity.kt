package com.km.composePlayground.scroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyRowForIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

class ScrollerActivity : AppCompatActivity() {

  @ExperimentalLazyDsl
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Column {
          Text("3 items per row, maxSize 90.dp")
          DynamicLazyRow(config = LazyRowConfig(items = testList))
          Spacer(modifier = Modifier.height(16.dp))

          Text("2 items per row, maxSize 90.dp")
          DynamicLazyRow(
            config = LazyRowConfig(items = testList, itemsPerWidth = { if (it > 600.dp) 4 else 2 }))
          Spacer(modifier = Modifier.height(16.dp))

          Text("2 items per row, maxSize 160.dp")
          DynamicLazyRow(
            config = LazyRowConfig(
              items = testList,
              maxItemSize = 160.dp,
              itemsPerWidth = { if (it > 600.dp) 4 else 2 }))
          Spacer(modifier = Modifier.height(16.dp))
        }
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

val SCROLLER_PADDING_START = 16.dp
val SCROLLER_PADDING_END = 24.dp

class LazyRowConfig<T>(
  val maxItemSize: Dp = 90.dp,
  val peekAmount: Dp = 30.dp,
  val itemsPerWidth: (Dp) -> Int = { if (it > 600.dp) 5 else 3 },
  val items: List<T>
)

@Composable
fun DynamicLazyRow(config: LazyRowConfig<Any>) {
  WithConstraints {
    val itemsPerWidth = config.itemsPerWidth(maxWidth)
    val itemContainerSize = (maxWidth - config.peekAmount) / itemsPerWidth
    val itemSize = min(itemContainerSize, config.maxItemSize)
    val additionalPadding = itemContainerSize - itemSize
    val listState = rememberLazyListState()

    Box(alignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
      LazyRowForIndexed(config.items,
        state = listState,
        contentPadding =
        PaddingValues(start = SCROLLER_PADDING_START, end = SCROLLER_PADDING_END)) { index, item ->
        val padding = if (index == 0) 0.dp else additionalPadding
        MyItem(text = item as String, modifier = Modifier.padding(start = padding).size(itemSize))
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