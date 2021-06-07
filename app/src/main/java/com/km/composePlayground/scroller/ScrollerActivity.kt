package com.km.composePlayground.scroller

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.km.composePlayground.R
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUiModel
import com.km.composePlayground.modifiers.rememberState
import com.km.composePlayground.scroller.common.Decoration
import com.km.composePlayground.scroller.common.DecorationCalculator
import com.km.composePlayground.scroller.common.FixedByGridLayoutPolicy
import com.km.composePlayground.scroller.common.NestedConnectionScrollSource
import com.km.composePlayground.scroller.common.RenderBlockingUiModel
import com.km.composePlayground.scroller.common.TestScrollConsumer
import com.km.composePlayground.scroller.common.enableAutoScroll
import com.km.composePlayground.scroller.horizontal.DividerDecorator
import com.km.composePlayground.scroller.horizontal.HorizontalScrollerComposeUiContent
import com.km.composePlayground.scroller.horizontal.HorizontalScrollerComposeUiModel
import com.km.composePlayground.scroller.horizontal.HorizontalScrollerUi
import com.km.composePlayground.scroller.horizontal.LayoutPolicyAwareHorizontalScrollerUi
import com.km.composePlayground.scroller.horizontal.LayoutPolicyHorizontalScrollerComposeUiModel
import com.km.composePlayground.scroller.horizontal.PlayFlingBehavior
import com.km.composePlayground.scroller.horizontal.UiModelComposableMapper
import com.km.composePlayground.scroller.vertical.SectionUiModel
import com.km.composePlayground.scroller.vertical.SectionUiModelContent
import com.km.composePlayground.scroller.vertical.StaticGridUiModel
import com.km.composePlayground.scroller.vertical.StaticGridUiModelContent
import com.km.composePlayground.scroller.vertical.VerticalScrollerUi
import com.km.composePlayground.scroller.vertical.VerticalScrollerUiModel
import com.km.composePlayground.scroller.vertical.VerticalScrollerUiModelContent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.abs

class ScrollerActivity : AppCompatActivity() {

  private var scrollerUiModel1x by mutableStateOf(
    HorizontalScrollerComposeUiModel(
      HorizontalScrollerComposeUiContent(
        uiAction = { updateScrollingContent(it) },
        items = mutableListOf<UiModel>().apply { addAll(testList.subList(0, 10)) }
      )
    )
  )

  private val layoutPolicyUiModel = LayoutPolicyHorizontalScrollerComposeUiModel(
    HorizontalScrollerComposeUiContent(
      uiAction = {},
      items = testList
    ),
    enableSnapping = true
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
//          HorizontalScrollers()
//          SimpleListPagination()
//        GridScroller()
//          TestScroller()
//          AutoScroller()
//        ParallaxScroller()
        NestedConnectionParallaxScroller()
      }
    }
  }

  @Composable
  fun ParallaxScrollerEx() {
    val scrollState = rememberScrollState()
    Box {
      Image(
        painter = painterResource(id = R.drawable.squirrel),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
          .graphicsLayer {
            val imageOffset = (-scrollState.value * 0.18f).dp
            val imageAlpha =
              ((scrollState.maxValue - scrollState.value.toFloat()) / scrollState.maxValue)
            translationY = imageOffset.value
            alpha = imageAlpha
          }
          .height(240.dp)
          .fillMaxWidth()
      )

      Column(
        Modifier
          .verticalScroll(scrollState)
          .padding(top = 200.dp)
          .background(
            MaterialTheme.colors.surface,
          )
          .fillMaxHeight()
          .fillMaxWidth()
          .padding(all = 16.dp)
      ) {
        for (idx in 1..8) {
          TextBox(idx)
        }
      }
    }
  }

  @Composable
  fun NestedConnectionParallaxScroller() {
    val headerImgWidth = 100.dp
    val headerImgWidthPx = with(LocalDensity.current) { headerImgWidth.roundToPx().toFloat() }
    val headerImgOffsetPx = remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    // Listen to scroll events from LazyRow
    val nestedScrollConnection = remember {
      object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
          val delta = available.x
          val firstVisibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()

          val isForwardScroll = delta < 0
          // | headerImg | item 1 | item 2 |....
          // |           | <-- offset 0
          // The header image should only be scrolled back once the first item is fullyvisible.
          // At steady state, first item offset is 0. When the first item reaches the edge of the screen,
          // it has moved by the width of the header image. Offset is -ve as forward scroll is -ve.
          //
          // | item 1 | item 2 |....
          // | <-- offset -headerImgWidth
          val inRangeReverseScroll = delta > 0 &&
            firstVisibleItem?.index == 0 && firstVisibleItem.offset > (-headerImgWidthPx)
          if (isForwardScroll || inRangeReverseScroll) {
            val newOffset = headerImgOffsetPx.value + delta
            headerImgOffsetPx.value = newOffset.coerceIn(-headerImgWidthPx, 0f)
          }

          // Don't consume any scroll. The header image is translated by X
          return Offset.Zero
        }
      }
    }

    Box(
      Modifier
        .fillMaxWidth()
        .nestedScroll(nestedScrollConnection)
    ) {

      Image(
        painter = painterResource(id = R.drawable.ic_launcher_background),
        contentDescription = null,
        modifier = Modifier
          .graphicsLayer {
            // Multiplier controls the speed of translation
            translationX = headerImgOffsetPx.value * 0.8f
            alpha = (headerImgWidthPx - abs(headerImgOffsetPx.value)) / headerImgWidthPx
          }
          .size(headerImgWidth)
      )

      LazyRow(
        state = listState,
        flingBehavior = PlayFlingBehavior(listState, rememberCoroutineScope()),
        modifier = Modifier
          .align(alignment = Alignment.Center)
          .fillMaxWidth(),
        contentPadding = PaddingValues(start = headerImgWidth)
      ) {
        items(10) { idx ->
          Text(
            "Item $idx",
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .size(100.dp)
              .border(width = 1.dp, color = Color.Red)
          )
        }
      }
    }
  }

  @Composable
  fun AutoScroller() {
    val listState = rememberLazyListState()
    listState.enableAutoScroll(1000)
    LazyRow(state = listState) {
      for (idx in 1..8) {
        item {
          TextBox(idx)
        }
      }
    }
  }

  private val scrollConsumer = TestScrollConsumer()

  @Composable
  fun TextBox(idx: Int) {
    Text(
      text = "Box $idx",
      modifier = Modifier
        .size(100.dp)
        .padding(4.dp)
        .background(color = Color.LightGray)
    )
  }

  @Composable
  fun TestScroller() {
    val scrollConnection = remember { NestedConnectionScrollSource() }
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.nestedScroll(connection = scrollConnection)) {
      DisposableEffect(scrollConnection) {
        scrollConsumer.registerSource(coroutineScope, scrollConnection)

        onDispose {
          scrollConsumer.unregisterSource(scrollConnection)
        }
      }

      LazyColumn {
        item {
          LazyRow {
            for (idx in 1..80) {
              item { TextBox(idx) }
            }
          }
        }

        item {
          val scope = rememberCoroutineScope()
          val listState = rememberLazyListState()
          LazyRow(flingBehavior = PlayFlingBehavior(listState, scope), state = listState) {
            for (idx in 20..50) {
              item { TextBox(idx) }
            }
          }
        }

        items(10) { idx ->
          TextBox(idx)
        }

        item {
          LazyRow {
            for (idx in 10..18) {
              item { TextBox(idx) }
            }
          }
        }
      }
    }
  }


  @Composable
  private fun GridScroller() {
    val staticGridUiModel = StaticGridUiModel(
      StaticGridUiModelContent(
        itemList = testList.subList(0, 10) +
//          object : RenderBlockingUiModel {} +
          testList.subList(11, 100),
        spanCount = 3,
        spanLookup = { idx -> if (idx > 0 && idx % 7 == 0) 2 else 1 },
        "id1"
      )
    )

//    LaunchedEffect(key1 = staticGridUiModel) {
//      delay(3000)
//
//      Log.d("dbg", "updating content")
//      staticGridUiModel.content.value = StaticGridUiModelContent(
//        itemList = testList.subList(0, 10) +
//          testList.subList(11, 20),
//        spanCount = 3,
//        spanLookup = { idx -> if (idx > 0 && idx % 7 == 0) 2 else 1 },
//        "id1"
//      )
//    }

    val scrollConnection = remember { NestedConnectionScrollSource() }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(scrollConnection) {
      scrollConsumer.registerSource(coroutineScope, scrollConnection)

      onDispose {
        scrollConsumer.unregisterSource(scrollConnection)
      }
    }

    VerticalScrollerUi(
      uiModel = VerticalScrollerUiModel(
        uiContent = VerticalScrollerUiModelContent(
          itemList = listOf(
//            UnderlineTextModel("Static Grid"),
//            staticGridUiModel,
//
//            UnderlineTextModel("Horizontal Scroller"),
//            scrollerUiModel1x,

//                    object : RenderBlockingUiModel {},

//            UnderlineTextModel("Dynamic Grid"),
//            DynamicGridUiModel(
//              DynamicGridUiModelContent(
//                itemList = testList,
//                desiredCellSize = 240,
//                spanLookup = { idx -> if (idx > 0 && (idx % 7 == 0 || idx % 11 == 0)) 2 else 1 },
//                ""
//              )
//            ),

            UnderlineTextModel("Linear Section"),
            SectionUiModel(
              SectionUiModelContent(
                itemList = listOf(layoutPolicyUiModel),
                identity = "ls1",
              )
            ),

            UnderlineTextModel("Linear Section 2"),
            SectionUiModel(
              SectionUiModelContent(
                itemList = testList.subList(0, 10),
                identity = "ls2",
              )
            ),

            )
        )
      ),
      mapper = uiModelMapper,
      containerModifier = Modifier.nestedScroll(scrollConnection)
    )
  }

  @Composable
  private fun HorizontalScrollers() {
    val decorationCalculator = DecorationCalculator { uiModel, _ ->
      val decorators = mutableListOf<Decoration>()
      if (uiModel is TextModel) {
        decorators.add(DividerDecorator())
      }
      decorators
    }

    Column {
      Text("1x")
      LayoutPolicyAwareHorizontalScrollerUi(
        layoutPolicy = FixedByGridLayoutPolicy(
          desiredChildWidth = 80.dp,
          childWidthMultiplier = 1
        ),
        mapper = uiModelMapper,
        uiModel = LayoutPolicyHorizontalScrollerComposeUiModel(
          HorizontalScrollerComposeUiContent(
            uiAction = { Log.d("dbg", "load more") },
            items = testList
          ),
          enableSnapping = true
        ),
        decorationCalculator = decorationCalculator,
      )

      Spacer(modifier = Modifier.height(16.dp))
      Text("2x")
      LayoutPolicyAwareHorizontalScrollerUi(
        layoutPolicy = FixedByGridLayoutPolicy(
          desiredChildWidth = 80.dp,
          childWidthMultiplier = 2
        ),
        mapper = uiModelMapper,
        uiModel = LayoutPolicyHorizontalScrollerComposeUiModel(
          HorizontalScrollerComposeUiContent(
            uiAction = {},
            items = testList
          ),
          enableSnapping = true
        ),
        decorationCalculator = decorationCalculator
      )
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
        HorizontalScrollerComposeUiContent(
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
        HorizontalScrollerComposeUiContent(
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
        modifier = Modifier
          .size(80.dp)
          .padding(start = 8.dp)
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

      is LayoutPolicyHorizontalScrollerComposeUiModel ->
        LayoutPolicyAwareHorizontalScrollerUi(
          layoutPolicy = FixedByGridLayoutPolicy(
            desiredChildWidth = 80.dp,
            childWidthMultiplier = 1
          ),
          mapper = this,
          uiModel = uiModel,
          decorationCalculator = { itemModel, _ ->
            val decorators = mutableListOf<Decoration>()
            if (itemModel is TextModel) {
              decorators.add(DividerDecorator(verticalPadding = 0.dp))
            }
            decorators
          }
        )

      is HorizontalScrollerComposeUiModel -> {
        Log.d("dbg", "horizontal scroller")
        HorizontalScrollerUi(
          mapper = this,
          uiModel = uiModel,
          decorationCalculator = { itemModel, _ ->
            val decorators = mutableListOf<Decoration>()
            if (itemModel is TextModel) {
              decorators.add(DividerDecorator(verticalPadding = 0.dp))
            }
            decorators
          }
        )
      }

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
      .size(size = 80.dp)
      .border(width = 1.dp, color = Color.Yellow)
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
    CircularProgressIndicator(
      modifier = Modifier
        .size(50.dp)
        .padding(start = 16.dp)
    )
  }
}