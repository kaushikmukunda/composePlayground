package com.km.composePlayground.scroller

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScrollerActivity : AppCompatActivity() {

  var scrollerUiModel1x by mutableStateOf(ScrollerUiModel(getScrollingContent(false)))

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Column {
          Text("0.75x")
          HorizontalScrollerUi(
            layoutPolicy = FixedLayoutPolicy(
              desiredItemWidth = 80.dp,
              baseWidthMultipler = 0.75f
            ),
            mapper = uiModelMapper,
            uiModel = ScrollerUiModel(
              HorizontalScrollerUiContent(
                uiAction = { Log.d("dbg", "load more") },
                items = testList
              ),
            ),
            itemDecoration = SpacerDecoration(shouldDecorate = { _, index, _ -> index > 0 })
          )
          Spacer(modifier = Modifier.height(16.dp))

          Text("1x")
          HorizontalScrollerUi(
            layoutPolicy = FixedLayoutPolicy(
              desiredItemWidth = 80.dp,
              baseWidthMultipler = 1.25f
            ),
            mapper = uiModelMapper,
            uiModel = scrollerUiModel1x,
            itemDecoration = DividerDecoration(shouldDecorate = { uiModel, index, _ ->
              when (uiModel) {
                is TextModel -> index > 0
                is FooterModel -> false
                else -> false
              }
            })
          )

          Spacer(modifier = Modifier.height(16.dp))
          Text("1.25x")
          HorizontalScrollerUi(
            layoutPolicy = FixedLayoutPolicy(
              desiredItemWidth = 80.dp,
              baseWidthMultipler = 1.25f
            ),
            mapper = uiModelMapper,
            uiModel = ScrollerUiModel(
              HorizontalScrollerUiContent(
                uiAction = {},
                items = testList
              ),
            ),
            itemDecoration = SpacerDecoration(shouldDecorate = { _, index, _ -> index > 0 })
          )

          Spacer(modifier = Modifier.height(16.dp))
          Text("fit entire content")
          HorizontalScrollerUi(
            mapper = uiModelMapper,
            layoutPolicy = FixedLayoutPolicy(desiredItemWidth = 80.dp),
            uiModel = ScrollerUiModel(
              HorizontalScrollerUiContent(
                uiAction = {},
                items = testList.subList(0, 2)
              ),
            ),
            itemDecoration = SpacerDecoration(shouldDecorate = { _, index, _ -> index > 0 })
          )

          Spacer(modifier = Modifier.height(16.dp))
          Text("Fading Edge")
          Box(modifier = Modifier.align(alignment = Alignment.CenterHorizontally).fillMaxWidth(0.9f)) {
            HorizontalScrollerUi(
              mapper = uiModelMapper,
              layoutPolicy = FixedLayoutPolicy(desiredItemWidth = 80.dp),
              uiModel = ScrollerUiModel(
                HorizontalScrollerUiContent(
                  uiAction = {},
                  items = testList
                ),
              ),
              itemDecoration = SpacerDecoration(shouldDecorate = { _, index, _ -> index > 0 })
            )
          }
        }
      }
    }
  }

  var loadCount = 2
  private fun updateScrollingContent() {
    // already loading
    if (scrollerUiModel1x.content.value.items.last() is FooterModel) return
    if (loadCount <= 0) return

    MainScope().launch {
      loadCount--
      val appendItems = mutableListOf<UiModel>(
        TextModel("adding $loadCount 0"),
        TextModel("adding $loadCount 1"),
        TextModel("adding $loadCount 2"),
      )
      val existingList = scrollerUiModel1x.content.value.items
      scrollerUiModel1x.content.value =
        getScrollingContent(true, existingList)

      Log.d("dbg", "loading")
      delay(2000)
      Log.d("dbg", "loading complete")

      scrollerUiModel1x.content.value = getScrollingContent(false, existingList, appendItems)
    }
  }

  private fun getScrollingContent(
    isLoadingMore: Boolean,
    existingList: List<UiModel> = testList,
    appendList: List<UiModel> = listOf()
  ): HorizontalScrollerUiContent {
    return HorizontalScrollerUiContent(
      uiAction = { position ->
        if (position == scrollerUiModel1x.content.value.items.size - 1) {
          updateScrollingContent()
        }
      },
      items = mutableListOf<UiModel>().apply {
        addAll(existingList)
        addAll(appendList)
        if (isLoadingMore) add(FooterModel())
      },
    )
  }
}


val testList = mutableListOf<UiModel>(
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

class TextModel(val text: String) : UiModel
class FooterModel() : UiModel


/** A sample implmentation of UiModel. */
val uiModelMapper = object : UiModelMapper {

  //  @Composable
  override fun map(uiModel: UiModel): @Composable() (Modifier) -> Unit {
    return when (uiModel) {
      is TextModel -> { modifier -> TextItem(uiModel, modifier) }
      is FooterModel -> { _ -> FooterItem(model = uiModel) }
      else -> { _ -> {} }
    }
  }

}

@Composable
private fun TextItem(model: TextModel, modifier: Modifier = Modifier) {
  Text(
    text = model.text,
    modifier = modifier
      .background(color = Color.Gray)
      .border(width = 1.dp, color = Color.Red)
  )
}

@Composable
private fun FooterItem(model: FooterModel) {
  CircularProgressIndicator(modifier = Modifier.padding(start = 16.dp))
}