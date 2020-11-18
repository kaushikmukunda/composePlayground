package com.km.composePlayground.scroller

import android.os.Bundle
import android.util.Log
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
            )
          )
          Spacer(modifier = Modifier.height(16.dp))

          Text("1x")
          HorizontalScrollerUi(
            layoutPolicy = FixedLayoutPolicy(
              desiredItemWidth = 80.dp,
              baseWidthMultipler = 1.25f
            ),
            mapper = uiModelMapper,
            uiModel = scrollerUiModel1x
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
                uiAction = { Log.d("dbg", "load more") },
                items = testList
              ),
            )
          )

          Spacer(modifier = Modifier.height(16.dp))
          Text("fit entire content")
          HorizontalScrollerUi(
            mapper = uiModelMapper,
            layoutPolicy = FixedLayoutPolicy(desiredItemWidth = 80.dp),
            uiModel = ScrollerUiModel(
              HorizontalScrollerUiContent(
                uiAction = { Log.d("dbg", "load more") },
                items = testList.subList(0, 2)
              ),
            )
          )

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

/** Action associated with Scrolling Ui. */
fun interface ScrollingUiAction {
  /**
   * Notifies listener to what position an item is currently visible
   */
  fun triggerPagination(position: Int)
}

/** Maps UiModels to composables. */
fun interface UiModelMapper {
  @Composable
  fun map(uiModel: UiModel, modifier: Modifier)
}

/** Container class to hold item size in Dp. */
class DpSize(val width: Dp, val height: Dp)

/**
 * Configure the layout parameters for the scroller.
 */
interface HorizontalScrollerLayoutPolicy {

  /**
   * Compute the dimensions of the item provided the scroller layout constraints.
   *
   * @param scope Provides the layout constraints of the scroller.
   */
  @Composable
  fun getItemSize(scope: WithConstraintsScope): DpSize

  /** The padding to be set on the scroller. */
  fun getContentPadding(): PaddingValues

  /** If the entire content were to fit in a single screen, should it be centered? */
  fun shouldCenterContent(): Boolean

}

/**
 * UiContent for the horizontal scroller.
 *
 * @property layoutPolicy Determine the layout and size of the item.
 * @property uiAction Ui actions supported by the scroller.
 * @property items The UiModels that are to be rendered in a list.
 * @property footeritem Optional The uiModel that ought to be appended to the list. Usually used
 *   for loading or error scenarios.
 */
@Stable
class HorizontalScrollerUiContent(
  val uiAction: ScrollingUiAction,
  val items: List<UiModel>,
)

/**
 * UiModel that establishes contract with HorizontalScrollerUi.
 *
 * @property mapper Maps UiModels to composables.
 */
@Stable
class ScrollerUiModel(
  horizontalScrollerUiContent: HorizontalScrollerUiContent,
) : UniformUiModel<HorizontalScrollerUiContent> {
  override val content = mutableStateOf(horizontalScrollerUiContent)
}

@Composable
fun HorizontalScrollerUi(
  uiModel: ScrollerUiModel,
  layoutPolicy: HorizontalScrollerLayoutPolicy,
  mapper: UiModelMapper,
) = UniformUi(uiModel) { content ->
  WithConstraints {
    val alignment =
      if (layoutPolicy.shouldCenterContent()) Alignment.Center else Alignment.TopStart
    val itemSize = layoutPolicy.getItemSize(this)

    Box(alignment = alignment, modifier = Modifier.fillMaxWidth()) {
      LazyRowForIndexed(
        items = content.items,
        contentPadding = layoutPolicy.getContentPadding(),
      ) { index, item ->
        // Only notify on first composition of a particular item
        onActive {
          content.uiAction.triggerPagination(index)
        }

        mapper.map(
          uiModel = item,
          modifier = Modifier.size(width = itemSize.width, height = itemSize.height)
        )
      }
    }
  }
}

/** A simple implementation of fixed layout policy. */
class FixedLayoutPolicy(
  val desiredItemWidth: Dp,
  val childPeekAmount: Float = 0.1f,
  val baseWidthMultipler: Float = 1f
) :
  HorizontalScrollerLayoutPolicy {

  @Composable
  override fun getItemSize(scope: WithConstraintsScope): DpSize {
    val width = with(DensityAmbient.current) {
      val widthForChildrenPx =
        (scope.maxWidth - getContentPadding().start - getContentPadding().end).toIntPx()

      remember(desiredItemWidth, widthForChildrenPx) {
        CardCountHelper.getUnitCardWidth(
          (desiredItemWidth * baseWidthMultipler).toIntPx(),
          widthForChildrenPx,
          childPeekAmount
        )
      }.toDp()
    }

    val height: Dp = width.times(9 / 16f)

    return DpSize(width, height)
  }

  override fun getContentPadding() = PaddingValues(8.dp)

  override fun shouldCenterContent() = true

}

/** A sample implmentation of UiModel. */
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