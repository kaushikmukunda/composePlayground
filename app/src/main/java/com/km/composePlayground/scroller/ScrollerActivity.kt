package com.km.composePlayground.scroller

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUiModel
import com.km.composePlayground.modifiers.rememberState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScrollerActivity : AppCompatActivity() {

  private var scrollerUiModel1x by mutableStateOf(
    ScrollerUiModel(
      HorizontalScrollerUiContent(
        uiAction = { updateScrollingContent(it) },
        items = mutableListOf<UiModel>().apply { addAll(testList.subList(0,10)) }
      )
    )
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Column {
          HorizontalScrollers()
//          SimpleListPagination()
//          GridScroller()
        }
      }
    }
  }

  @Composable
  private fun GridScroller() {
    VerticalScrollerUi(
      uiModel = VerticalScrollerUiModel(
        uiContent = VerticalScrollerUiModelContent(
          itemList = listOf(
            UnderlineTextModel("Static Grid"),
            StaticGridUiModel(
              StaticGridUiModelContent(
                itemList = testList.subList(0, 10) +
//                                object : RenderBlockingUiModel {} +
                  testList.subList(11, 100),
                spanCount = 3,
                spanLookup = { idx -> if (idx > 0 && idx % 7 == 0) 2 else 1 },
                ""
              )
            ),

            UnderlineTextModel("Horizontal Scroller"),
            scrollerUiModel1x,

//                    object : RenderBlockingUiModel {},

            UnderlineTextModel("Dynamic Grid"),
            DynamicGridUiModel(
              DynamicGridUiModelContent(
                itemList = testList,
                desiredCellSize = 240,
                spanLookup = { idx -> if (idx > 0 && (idx % 7 == 0 || idx % 11 == 0)) 2 else 1 },
                ""
              )
            ),

            UnderlineTextModel("Linear Section"),
            SectionUiModel(
              SectionUiModelContent(
                itemList = testList.subList(0, 10),
                identity = "",
              )
            )
          )
        )
      ),
      mapper = uiModelMapper
    )
  }

  @Composable
  private fun HorizontalScrollers() {
    Column {
//      Text("0.75x")
//      HorizontalScrollerUi(
//        layoutPolicy = FixedLayoutPolicy(
//          desiredItemWidth = 80.dp,
//          baseWidthMultipler = 0.75f
//        ),
//        mapper = uiModelMapper,
//        uiModel = ScrollerUiModel(
//          HorizontalScrollerUiContent(
//            uiAction = { Log.d("dbg", "load more") },
//            items = testList
//          ),
//        ),
//        itemDecoration = { _, _, _ -> listOf(SpacerDecorator()) }
//      )
//      Spacer(modifier = Modifier.height(16.dp))

      Text("1x")
      HorizontalScrollerUi(
        layoutPolicy = FixedLayoutPolicy(
          desiredItemWidth = 80.dp,
          baseWidthMultipler = 1.25f
        ),
        mapper = uiModelMapper,
        uiModel = scrollerUiModel1x,
        decorationCalculator = { uiModel ->
          val decorators = mutableListOf<Decorator>()
          if (uiModel is TextModel) {
            decorators.add(DividerDecorator())
          }
          decorators
        }
      )

//      Spacer(modifier = Modifier.height(16.dp))
//      Text("1.25x")
//      HorizontalScrollerUi(
//        layoutPolicy = FixedLayoutPolicy(
//          desiredItemWidth = 80.dp,
//          baseWidthMultipler = 1.25f
//        ),
//        mapper = uiModelMapper,
//        uiModel = ScrollerUiModel(
//          HorizontalScrollerUiContent(
//            uiAction = {},
//            items = testList
//          ),
//        ),
//        itemDecoration = { _, _, _ -> listOf(SpacerDecorator()) }
//      )
//
//      Spacer(modifier = Modifier.height(16.dp))
//      Text("fit entire content")
//      HorizontalScrollerUi(
//        mapper = uiModelMapper,
//        layoutPolicy = FixedLayoutPolicy(desiredItemWidth = 80.dp),
//        uiModel = ScrollerUiModel(
//          HorizontalScrollerUiContent(
//            uiAction = {},
//            items = testList.subList(0, 2)
//          ),
//        ),
//        itemDecoration = { _, _, _ -> listOf(SpacerDecorator()) }
//      )
//
//      Spacer(modifier = Modifier.height(16.dp))
//      Text("Fading Edge")
//      Box(modifier = Modifier.align(alignment = Alignment.CenterHorizontally).fillMaxWidth(0.9f)) {
//        HorizontalScrollerUi(
//          mapper = uiModelMapper,
//          layoutPolicy = FixedLayoutPolicy(desiredItemWidth = 80.dp),
//          uiModel = ScrollerUiModel(
//            HorizontalScrollerUiContent(
//              uiAction = {},
//              items = testList
//            ),
//          ),
//          itemDecoration = { _, _, _ -> listOf(SpacerDecorator()) }
//        )
//      }
    }
  }

  var loadCount = 2
  fun updateScrollingContent(index: Int) {
    val scrollerUiModel = scrollerUiModel1x
    if (index < scrollerUiModel.content.value.items.lastIndex) return

    MainScope().launch {
      // already loading
      if (scrollerUiModel.content.value.items.last() is FooterModel) return@launch
      if (loadCount <= 0) return@launch

      loadCount--

      val scrollingList =
        mutableListOf<UiModel>().apply { addAll(scrollerUiModel.content.value.items) }
      scrollerUiModel.content.value =
        HorizontalScrollerUiContent(
          uiAction = { updateScrollingContent(it) },
          items = scrollingList.apply { add(FooterModel()) }
        )

      Log.d("dbg", "loading")
      delay(2000)
      Log.d("dbg", "loading complete")

      val appendItems = mutableListOf<UiModel>().apply {
        for (i in 0..6) {
          add(TextModel("adding $loadCount $i"))
        }
      }
      val newList = scrollingList.apply {
        removeLast()
        addAll(appendItems)
      }
      scrollerUiModel.content.value =
        HorizontalScrollerUiContent(
          uiAction = { updateScrollingContent(it) },
          items = newList
        )
    }
  }
}

class SimplePaginationModel(
  uiContent: SimplePaginationContent
) : UniformUiModel<SimplePaginationContent> {
  override val content = mutableStateOf(uiContent)
}

class SimplePaginationContent(
  val paginationList: List<String>,
  val uiAction: (Int) -> Any = {}
)

@Composable
private fun SimpleListPagination() {
  val uiModel = remember {
    SimplePaginationModel(
      SimplePaginationContent(
        listOf("a", "b", "c", "d", "e", "f", "g", "h", "i")
      )
    )
  }

  var pageCount by rememberState { 0 }
  val content = uiModel.content.value

  fun itemRendered(index: Int) {
    MainScope().launch {
      if (index == content.paginationList.size - 1 && pageCount < 1) {
        pageCount++

        var paginationList = content.paginationList + "loading"
        uiModel.content.value = SimplePaginationContent(paginationList)
        Log.d("dbg", "simulate loading pagecnt $pageCount")
        delay(1000)
        Log.d("dbg", "finish loading $pageCount")

        paginationList = paginationList - "loading" + listOf("k", "l", "m", "n", "o")
        uiModel.content.value = SimplePaginationContent(paginationList)
      }
    }
  }

  Log.d("dbg", "recomposing simple list")
  LazyRow(modifier = Modifier.fillMaxWidth()) {
    Log.d("dbg", "recomposing lazy row")
    itemsIndexed(content.paginationList) { index, item ->
      itemRendered(index)
      SideEffect {
        uiModel.content.value.uiAction(index)
      }
      Text(
        item,
        modifier = Modifier.size(80.dp).padding(start = 8.dp)
          .background(color = Color.LightGray)
      )
      Log.d("dbg", "rendering $index")
    }
  }
}


val testList = mutableListOf<UiModel>().apply {
  for (i in 0..100) {
    add(TextModel("Word $i"))
  }
}

class TextModel(val text: String) : UiModel
class UnderlineTextModel(val text: String) : UiModel
class FooterModel : UiModel


/** A sample implmentation of UiModel. */
val uiModelMapper = object : UiModelComposableMapper {

  override fun map(uiModel: UiModel): @Composable() (Modifier) -> Unit {
    return { modifier -> internalMap(uiModel, modifier) }
  }

  @Composable
  private fun internalMap(uiModel: UiModel, modifier: Modifier) {
    when (uiModel) {
      is TextModel -> TextItem(uiModel, modifier)
      is UnderlineTextModel -> UnderlineTextItem(model = uiModel)
      is FooterModel -> FooterItem(model = uiModel)
      is ScrollerUiModel ->
        HorizontalScrollerUi(
          layoutPolicy = FixedLayoutPolicy(
            desiredItemWidth = 80.dp,
            baseWidthMultipler = 1.25f
          ),
          mapper = this,
          uiModel = uiModel,
          decorationCalculator = { itemModel ->
            val decorators = mutableListOf<Decorator>()
            if (itemModel is TextModel) {
              decorators.add(DividerDecorator(verticalPadding = 0.dp))
            }
            decorators
          }
        )
      is RenderBlockingUiModel -> TextItem(TextModel("blocking item"))
    }
  }
}

@Composable
fun TextItem(model: TextModel, modifier: Modifier = Modifier) {
  Text(
    text = model.text,
    modifier = modifier
      .padding(bottom = 8.dp, end = 8.dp)
      .background(color = Color.Gray)
//      .border(width = 1.dp, color = Color.Yellow)
  )
}

@Composable
fun UnderlineTextItem(model: UnderlineTextModel, modifier: Modifier = Modifier) {
  Text(
    text = model.text,
    textDecoration = TextDecoration.Underline,
    modifier = modifier.padding(8.dp)
  )
}

@Composable
private fun FooterItem(model: FooterModel) {
  Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
    CircularProgressIndicator(modifier = Modifier.size(50.dp).padding(start = 16.dp))
  }
}