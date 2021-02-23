package com.km.composePlayground.scratchpad

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ClickableText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ConstraintSet
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.km.composePlayground.components.button.ButtonComposer
import com.km.composePlayground.components.button.ColorUtility
import com.km.composePlayground.components.buttongroup.ButtonGroupComposer
import com.km.composePlayground.components.delimiterFlowRow.BulletDelimiter
import com.km.composePlayground.components.delimiterFlowRow.DelimiterFlowLayout
import com.km.composePlayground.components.dialog.ContentModel
import com.km.composePlayground.components.dialog.DialogButtonConfig
import com.km.composePlayground.components.dialog.DialogComposer
import com.km.composePlayground.components.dialog.DialogUiAction
import com.km.composePlayground.components.dialog.DialogUiModel
import com.km.composePlayground.components.dialog.FooterModel
import com.km.composePlayground.linkText.LinkTextUi
import com.km.composePlayground.linkText.LinkTextUiAction
import com.km.composePlayground.linkText.LinkTextUiModel
import com.km.composePlayground.linkText.MarkdownText
import com.km.composePlayground.modifiers.rememberState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScratchPadActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
          AnimatedVisibilityLazyColumnDemo()
//                    ModifierSample()
//                TextAnnotation()
//          FullScreenDialogSample()
//          DialogSample()
//          LinkText()
//          AlertDialogSample()
//      DelimiterFlowAnim()
//            FullyLoadedTransition()
//            CrossfadeSample()
//            AnimatingText()
        }
      }
    }
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedVisibilityLazyColumnDemo() {
  var itemNum by remember { mutableStateOf(0) }
  Column {
    Row(Modifier.fillMaxWidth()) {
      Button(
        { itemNum = itemNum + 1 },
        enabled = itemNum <= turquoiseColors.size - 1,
        modifier = Modifier.padding(15.dp).weight(1f)
      ) {
        Text("Add")
      }

      Button(
        { itemNum = itemNum - 1 },
        enabled = itemNum >= 1,
        modifier = Modifier.padding(15.dp).weight(1f)
      ) {
        Text("Remove")
      }
    }
    LazyRow {
      itemsIndexed(turquoiseColors) { i, color ->
        AnimatedVisibility(
          (turquoiseColors.size - itemNum) <= i,
          enter = fadeIn(0.1f, animationSpec = tween(120)),
          exit = fadeOut(0.1f, animationSpec = tween(120))
        ) {
          Log.d("dbg", "rendering i $i")
          Spacer(Modifier.width(90.dp).height(90.dp).background(color))
        }
      }
    }

    Button(
      { itemNum = 0 },
      modifier = Modifier.align(Alignment.End).padding(15.dp)
    ) {
      Text("Clear All")
    }
  }
}

private val turquoiseColors = listOf(
  Color(0xff07688C),
  Color(0xff1986AF),
  Color(0xff50B6CD),
  Color(0xffBCF8FF),
  Color(0xff8AEAE9),
  Color(0xff46CECA)
)


@Composable
fun ModifierSample() {
  Text(
    text = "foobar",
    modifier = Modifier
      .width(80.dp)
      .border(width = 1.dp, color = Color.Magenta)
      .padding(horizontal = 24.dp)
      .drawBehind {
        Log.d("dbg", "drawContext width ${drawContext.size.width} size width ${size.width}")
        val top = center.y - size.height / 2
        val bot = center.y + size.height / 2
        val end = center.x + size.width / 2 + 24.dp.toPx()

        drawLine(
          color = Color.Blue,
          start = Offset(end, top),
          end = Offset(end, bot),
          strokeWidth = 5f
        )
      })
//    .border(width = 1.dp, color = Color.Green)
//    .drawBehind {
//      val top = center.y - size.height / 2
//      val bot = center.y + size.height / 2
//      val end = center.x + size.width / 2
//      drawLine(color = Color.Yellow, start = Offset(end, top), end = Offset(end, bot), strokeWidth = 20f)
//    }
}

@Composable
fun AlertDialogSample() {
  val openDialog = remember { mutableStateOf(true) }
  if (!openDialog.value) {
    return
  }
  AlertDialog(onDismissRequest = {},
    shape = MaterialTheme.shapes.large,
    title = { Text("Title") },
    text = { Text("This is some dialog aint it?") },
    buttons = {
      Button({
        openDialog.value = false
      }) { Text("Button 1") }
    })
}

@Composable
fun FullScreenDialogSample() {
  val openDialog = remember { mutableStateOf(true) }

  val headerId = "header"
  val contentId = "content"
  val footerId = "footer"
  if (openDialog.value) {
      ConstraintSet {
          val headerRef = createRefFor(headerId)
          val contentRef = createRefFor(contentId)
          val footerRef = createRefFor(footerId)

          constrain(headerRef) {
              top.linkTo(parent.top)
              bottom.linkTo(contentRef.top, 8.dp)
              linkTo(start = parent.start, end = parent.end, bias = 0f)
          }

          constrain(contentRef) {
              top.linkTo(headerRef.bottom)
              bottom.linkTo(footerRef.top)
              linkTo(start = parent.start, end = parent.end, bias = 0f)
              height = Dimension.fillToConstraints
          }

          constrain(footerRef) {
              start.linkTo(parent.start)
              top.linkTo(contentRef.bottom)
              bottom.linkTo(parent.bottom)
          }
      }
      ConstraintLayout(
          modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)
      ) {
          Text("Header", modifier = Modifier.layoutId(headerId))
          Text("Content", modifier = Modifier.layoutId(contentId))

          Button(onClick = { openDialog.value = false }, modifier = Modifier.layoutId(footerId)) {
              Text("click me")
          }
      }
  }
}

@Composable
private fun DialogSample() {
  val dialogComposer = DialogComposer(ButtonGroupComposer(ButtonComposer(ColorUtility())))
  dialogComposer.compose(
    model = DialogUiModel(
      content = ContentModel(
        content = MarkdownText(
          "This is the dialog body<br>Some longish text<br>How about them Cowboys?<p>" +
            "This is the dialog body<br>Some longish text<br>How about them Cowboys?<p>" +
            "This is the dialog body<br>Some longish text<br>How about them Cowboys?<p>" +
            "This is the dialog body<br>Some longish text<br>How about them Cowboys?<p>" +
            "This is the dialog body<br>Some longish text<br>How about them Cowboys?<p>" +
            "This is the dialog body<br>Some longish text<br>How about them Cowboys?<p>" +
            "This is the dialog body<br> Some <a href=\"www.google.com\">Search</a> longish text<p>" +
            "This is the dialog body<br> another <a href=\"www.bing.com\">Search</a> longish text<p>"
        )
      ),
      footer = FooterModel(
        positiveButtonConfig = DialogButtonConfig("click me and what if this is a super long long long long long dialog text that goes on"),
        negativeButtonConfig = DialogButtonConfig("click me and what if this is a super long long long long long dialog text that goes on")
      ),
      uiAction = object : DialogUiAction {
        override fun onLinkClicked(url: String, dialogData: Any?) {
          Log.d("dbg", "click $url")
        }

        override fun onPositiveButtonClicked(dialogData: Any?) {
          Log.d("dbg", "+ve button clicked")
        }

        override fun onNegativeButtonClicked(dialogData: Any?) {
          Log.d("dbg", "-ve button clicked")
        }

        override fun onDismiss(dialogData: Any?) {
          Log.d("dbg", "dialog dismissed")
        }
      }
    )
  )
}

@Composable
private fun LinkText() {
  val stubUiAction = object : LinkTextUiAction {
    override fun onClick(clickData: Any?) {}
  }
  Column {
    LinkTextUi(
      LinkTextUiModel(
        uiAction = stubUiAction,
        textMarkdown = MarkdownText("Simple <a href=\"www.example.com\">click me</a><b>bold</b> <i>italic</i>"),
        linkTextMarkdown = MarkdownText("link"),
      )
    )
    LinkTextUi(
      LinkTextUiModel(
        uiAction = stubUiAction,
        textMarkdown = MarkdownText("start <b>bold <i>italic</i> bold <i>italic</i></b> end "),
        linkTextMarkdown = MarkdownText("link"),
      )
    )
  }
}

@Composable
private fun TextAnnotation() {
  val annotatedString = buildAnnotatedString {
    append("link: <b>Jetpack</b> Compose")
    // attach a string annotation that stores a URL to the text "Jetpack Compose".
    addStringAnnotation(
      tag = "URL",
      annotation = "https://developer.android.com/jetpack/compose",
      start = 0,
      end = 4
    )
    addStyle(
      SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
      start = 0,
      end = 4
    )

    toAnnotatedString()
  }

  ClickableText(text = annotatedString, onClick = { offset ->
    if (offset in (0..4)) {
      Log.d("dbg", "message in offset clicked $offset")
    } else {
      Log.d("dbg", "out of bounds clicked")
    }
  })
}

private val TEST_PHRASES = listOf<@Composable() () -> Unit>(
  { Text("Etiam sit amet") },
  { Text("ex id ipsum") },
  { Text("commodo dictum") },
  { Text("Ut id ex vehicula") },
  { Text("venenatis enim") },
  { Text("feugiat, porta neque") },
  { Text("In venenatis") },
  { Text("neque ac quam aliquam") },
  { Text("tempus vitae sit amet lorem") },
  { Text("Vestibulum accumsan") },
  { Text("nisl eget neque") },
  { Text("aliquam ultricies") },
  { Text("Nullam non leo") },
  { Text("ullamcorper, ornare") },
  { Text("felis nec") },
  { Text("euismod magna") }
)

@Composable
private fun DelimiterFlowAnim() {
  Column {
    DelimiterFlowLayout(
      numLines = 1,
      delimiter = { modifier ->
        BulletDelimiter(
          bulletRadius = 2.dp,
          bulletGap = 8.dp,
          modifier = modifier
        )
      },
      children = TEST_PHRASES
    )

    Spacer(modifier = Modifier.padding(top = 16.dp))

    DelimiterFlowLayout(
      numLines = 10,
      delimiter = { modifier ->
        BulletDelimiter(
          bulletRadius = 2.dp,
          bulletGap = 8.dp,
          modifier = modifier
        )
      },
      children = TEST_PHRASES
    )
  }
}

class FadeAnimState(
  val numText: Int = 1
)

private val ANIM_DURATION_MS = 667

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FullyLoadedTransition() {
  var animState by rememberState { FadeAnimState() }

  Box(modifier = Modifier.fillMaxWidth()) {
    AnimatedVisibility(
      visible = (animState.numText == 1),
      enter = expandHorizontally(
        initialWidth = { (it * 0.1).toInt() },
        animationSpec = tween(
          durationMillis = ANIM_DURATION_MS,
          easing = CubicBezierEasing(0.8f, 0.0f, 0.6f, 1.0f)
        )
      ),
      exit = shrinkHorizontally(
        shrinkTowards = Alignment.Start,
        targetWidth = { (it * 0.1).toInt() },
        animationSpec = tween(
          durationMillis = ANIM_DURATION_MS,
          easing = CubicBezierEasing(0.8f, 0.0f, 0.6f, 1.0f)
        )
      ) + fadeOut(
        animationSpec = tween(
          durationMillis = 167,
          easing = LinearEasing,
          delayMillis = 333
        )
      )
    ) {
      Text(
        "open",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = Color.Red)
      )
    }

    AnimatedVisibility(
      visible = animState.numText == 2,
      enter = fadeIn(initialAlpha = 0.3f, animationSpec = tween()),
      exit = fadeOut(targetAlpha = 0.3f, animationSpec = tween())
    ) {
      Row {
        Text("try", modifier = Modifier.fillMaxWidth(0.5f))
        Text("install", modifier = Modifier.fillMaxWidth(0.5f))
      }
    }
  }

  MainScope().launch {
    for (i in 1..10) {
      delay(1500)
      Log.d("dbg", "updating state")
      animState = if (animState.numText == 1) FadeAnimState(2) else FadeAnimState(1)
    }
  }

}

class OpacityAnimState(
  var visible: Boolean = true,
  var current: String,
  var next: String = "",
  var count: Int = 0
)

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun AnimatingText() {
  val animState = rememberState { OpacityAnimState(current = "text") }

  val opacity =
    animatedAlpha(tween(durationMillis = 250, delayMillis = 500), animState.value.visible) {
      Log.d("dbg", "animation end")
      MainScope().launch {
        delay(500)

        var current = animState.value.current
        var next = animState.value.next
        var count = animState.value.count

        if (animState.value.visible) {
          next = animState.value.current + animState.value.count
          count++
        } else {
          current = animState.value.next
        }

        animState.value = OpacityAnimState(
          visible = !animState.value.visible,
          current = current, next = next, count = count
        )
      }
    }

  Text(animState.value.current, modifier = Modifier.alpha(opacity.value))
}

@Composable
private fun animatedAlpha(
  animation: AnimationSpec<Float>,
  visible: Boolean,
  onAnimationFinish: () -> Unit = {}
): Animatable<Float, AnimationVector1D> {
  val animatedFloat = remember { Animatable(if (!visible) 1f else 0f) }
  LaunchedEffect(visible) {
    val result = animatedFloat.animateTo(
      if (visible) 1f else 0f,
      animationSpec = animation,
    )
    if (result.endReason == AnimationEndReason.Finished) {
      onAnimationFinish()
    }
  }
  return animatedFloat
}
