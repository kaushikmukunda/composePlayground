package com.km.composePlayground.scroller

import android.os.Bundle
import android.util.Log
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRowForIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel

class ScrollerActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Column {
          Text("0.75x")
          DynamicLazyRow(
            uiModel = ScrollerUiModel(
              config = ScrollerConfig(itemBaseWidthMultiplier = 0.75f),
              uiAction = object : ScrollingUiAction {
                override fun loadMore() {
                  Log.d("dbg", "load more")
                }
              },
              items = mutableStateOf(testList)
            )
          )
          Spacer(modifier = Modifier.height(16.dp))

          Text("1x")
          DynamicLazyRow(
            uiModel = ScrollerUiModel(
              config = ScrollerConfig(),
              uiAction = object : ScrollingUiAction {
                override fun loadMore() {
                  Log.d("dbg", "load more")
                }
              },
              items = mutableStateOf(testList)
            )
          )

          Spacer(modifier = Modifier.height(16.dp))
          Text("1.25x")
          DynamicLazyRow(
            uiModel = ScrollerUiModel(
              config = ScrollerConfig(itemBaseWidthMultiplier = 1.25f),
              uiAction = object : ScrollingUiAction {
                override fun loadMore() {
                  Log.d("dbg", "load more")
                }
              },
              items = mutableStateOf(testList)
            )
          )

          Spacer(modifier = Modifier.height(16.dp))
          Text("fit entire content")
          DynamicLazyRow(
            uiModel = ScrollerUiModel(
              config = ScrollerConfig(),
              uiAction = object : ScrollingUiAction {
                override fun loadMore() {
                  Log.d("dbg", "load more")
                }
              },
              items = mutableStateOf(testList.subList(0, 2))
            )
          )

        }
      }
    }
  }
}


val testList = listOf(
  TextModel("Hello World"),
  TextModel("Foo"),
  TextModel("Bar"),
  TextModel("Baz"),
  TextModel("Word 1"),
  TextModel("Word 2"),
  TextModel("Word 3"),
  TextModel("Word 4"),
  TextModel("Word 5"),
)

val SCROLLER_PADDING_START = 16.dp
val SCROLLER_PADDING_END = 24.dp
val PAGINATION_THRESHOLD = 5

class TextModel(val text: String) : UiModel

class ContentPadding(
  val start: Dp,
  val end: Dp
) {
  fun getTotalPadding() = start + end
}

class ScrollerConfig(
  @FloatRange(from = 0.0, to = 1.0) val childPeekAmount: Float = 0.1f,
  val itemBaseWidthMultiplier: Float = 1.0f,
  val centerContent: Boolean = true,
  val contentPadding: ContentPadding = ContentPadding(SCROLLER_PADDING_START, SCROLLER_PADDING_END),
  val paginationThreshold: Int = PAGINATION_THRESHOLD
)

interface ScrollingUiAction {

  fun loadMore()

}

class ScrollerUiModel(
  val config: ScrollerConfig,
  val uiAction: ScrollingUiAction,
  val items: MutableState<List<UiModel>>,
  val isLoadingMore: Boolean = false
)

@Composable
fun DynamicLazyRow(uiModel: ScrollerUiModel) {
  WithConstraints {
    val desiredCardWidth = 240 // Replace with DensityMetric
    val widthForChildrenPx =
      with(DensityAmbient.current) {
        maxWidth.toIntPx() - uiModel.config.contentPadding.getTotalPadding().toIntPx()
      }
    val numColumns = remember(desiredCardWidth, widthForChildrenPx) {
      CardCountHelper.getCardCount(
        (desiredCardWidth * uiModel.config.itemBaseWidthMultiplier).toInt(),
        widthForChildrenPx,
        uiModel.config.childPeekAmount
      )
    }
    val itemWidthPx = remember(desiredCardWidth, widthForChildrenPx) {
      CardCountHelper.getUnitCardWidth(
        (desiredCardWidth * uiModel.config.itemBaseWidthMultiplier).toInt(),
        widthForChildrenPx,
        uiModel.config.childPeekAmount
      )
    }
    val itemWidthDp = with(DensityAmbient.current) { itemWidthPx.toDp() }
    val padding = with(DensityAmbient.current) {
      ((widthForChildrenPx - (itemWidthPx * numColumns)) / numColumns).toDp()
    }
    Log.d("dbg", "numColumns $numColumns itemWidth $itemWidthDp padding $padding")

    Column {
      val alignment = if (uiModel.config.centerContent) Alignment.Center else Alignment.TopStart
      Box(alignment = alignment, modifier = Modifier.fillMaxWidth()) {
        LazyRowForIndexed(
          items = uiModel.items.value,
          contentPadding = PaddingValues(
            start = uiModel.config.contentPadding.start,
            end = uiModel.config.contentPadding.end
          )
        ) { index, item ->
          if (index == uiModel.items.value.lastIndex - uiModel.config.paginationThreshold) {
            uiModel.uiAction.loadMore()
          }

          MyItem(
            model = item as TextModel,
            modifier = Modifier.width(width = itemWidthDp)
          )
        }
      }

      if (uiModel.isLoadingMore) {
        CircularProgressIndicator()
      }
    }
  }
}

@Composable
fun MyItem(model: TextModel, modifier: Modifier = Modifier) {
  Text(
    text = model.text, modifier = modifier
      .padding(horizontal = 8.dp)
      .background(color = Color.Gray)
      .border(width = 1.dp, color = Color.Red)
  )
}