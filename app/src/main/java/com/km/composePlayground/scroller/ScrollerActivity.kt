package com.km.composePlayground.scroller

import android.os.Bundle
import android.util.Log
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRowForIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.layout.WithConstraintsScope
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUi
import com.km.composePlayground.base.UniformUiModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScrollerActivity : AppCompatActivity() {

  var scrollerUiModel1x by mutableStateOf(
    ScrollerUiModel(
      getScrollingContent(false),
      uiModelMapper
    )
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Column {
          Text("0.75x")
          DynamicLazyRow(
            uiModel = ScrollerUiModel(
              ScrollerUiContent(
                config = ScrollerConfig(itemBaseWidthMultiplier = 0.75f),
                uiAction = { Log.d("dbg", "load more") },
                items = testList
              ),
              mapper = uiModelMapper
            )
          )
          Spacer(modifier = Modifier.height(16.dp))

          Text("1x")
          DynamicLazyRow(uiModel = scrollerUiModel1x)

          Spacer(modifier = Modifier.height(16.dp))
          Text("1.25x")
          DynamicLazyRow(
            uiModel = ScrollerUiModel(
              ScrollerUiContent(
                config = ScrollerConfig(itemBaseWidthMultiplier = 1.25f),
                uiAction = { Log.d("dbg", "load more") },
                items = testList
              ),
              mapper = uiModelMapper
            )
          )

          Spacer(modifier = Modifier.height(16.dp))
          Text("fit entire content")
          DynamicLazyRow(
            uiModel = ScrollerUiModel(
              ScrollerUiContent(
                config = ScrollerConfig(),
                uiAction = { Log.d("dbg", "load more") },
                items = testList.subList(0, 2)
              ),
              mapper = uiModelMapper
            )
          )

        }
      }
    }
  }

  var loadCount = 2
  private fun updateScrollingContent() {
    // already loading
    if (scrollerUiModel1x.content.value.footeritem != null) return
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
  ): ScrollerUiContent {
    return ScrollerUiContent(
      config = ScrollerConfig(),
      uiAction = { position ->
        if (position == scrollerUiModel1x.content.value.items.size - 1) {
          updateScrollingContent()
        }
      },
      items = mutableListOf<UiModel>().apply {
        addAll(existingList)
        addAll(appendList)
      },
      footeritem = if (isLoadingMore) FooterModel() else null
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

fun interface ScrollingUiAction {
  /**
   * Notifies listener to what position an item is currently visible
   */
  fun triggerPagination(position: Int)
}

fun interface UiModelMapper {
  @Composable
  fun map(uiModel: UiModel, modifier: Modifier)
}

class ScrollerConfig(
  val desiredItemWidth: Dp = 80.dp,
  @FloatRange(from = 0.0, to = 1.0) val childPeekAmount: Float = 0.1f,
  val itemBaseWidthMultiplier: Float = 1.0f,
  val centerContent: Boolean = true,
  val contentPadding: PaddingValues = PaddingValues(all = 8.dp),
)

class ScrollerUiContent(
  val config: ScrollerConfig,
  val uiAction: ScrollingUiAction,
  val items: List<UiModel>,
  val footeritem: UiModel? = null,
)

class ScrollerUiModel(
  scrollerUiContent: ScrollerUiContent,
  val mapper: UiModelMapper,
) : UniformUiModel<ScrollerUiContent> {
  override val content = mutableStateOf(scrollerUiContent)
}

@Composable
fun DynamicLazyRow(uiModel: ScrollerUiModel) = UniformUi(uiModel) { content ->
  WithConstraints {
    val alignment = if (content.config.centerContent) Alignment.Center else Alignment.TopStart
    val itemWidthDp = getItemWidth(content = content)

    Box(alignment = alignment, modifier = Modifier.fillMaxWidth()) {
      LazyRowForIndexed(
        items = content.items,
        contentPadding = content.config.contentPadding,
      ) { index, item ->
        // Only notify on first composition of a particular item
        onActive {
          content.uiAction.triggerPagination(index)
        }

        uiModel.mapper.map(
          uiModel = item,
          modifier = Modifier.width(width = itemWidthDp)
        )

        // Append footer item
        if (index == content.items.lastIndex && content.footeritem != null) {
          uiModel.mapper.map(content.footeritem, Modifier)
        }
      }
    }
  }
}

@Composable
private fun WithConstraintsScope.getItemWidth(content: ScrollerUiContent): Dp {
  return with(DensityAmbient.current) {
    val widthForChildrenPx =
      (maxWidth - content.config.contentPadding.start - content.config.contentPadding.end).toIntPx()

    remember(content.config.desiredItemWidth, widthForChildrenPx) {
      CardCountHelper.getUnitCardWidth(
        (content.config.desiredItemWidth * content.config.itemBaseWidthMultiplier).toIntPx(),
        widthForChildrenPx,
        content.config.childPeekAmount
      )
    }.toDp()
  }
}

val uiModelMapper = object : UiModelMapper {

  @Composable
  override fun map(uiModel: UiModel, modifier: Modifier) {
    when (uiModel) {
      is TextModel -> TextItem(uiModel, modifier)
      is FooterModel -> FooterItem(model = uiModel)
    }
  }

}

@Composable
private fun TextItem(model: TextModel, modifier: Modifier = Modifier) {
  Text(
    text = model.text,
    modifier = modifier
      .padding(end = 8.dp)
      .background(color = Color.Gray)
      .border(width = 1.dp, color = Color.Red)
  )
}

@Composable
private fun FooterItem(model: FooterModel) {
  CircularProgressIndicator()
}