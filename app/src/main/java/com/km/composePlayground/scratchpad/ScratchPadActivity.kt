package com.km.composePlayground.scratchpad

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ClickableText
import androidx.compose.material.Text
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.annotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.km.composePlayground.components.button.ButtonComposer
import com.km.composePlayground.components.button.ColorUtility
import com.km.composePlayground.components.buttongroup.ButtonGroupComposer
import com.km.composePlayground.components.delimiterFlowRow.BulletDelimiter
import com.km.composePlayground.components.delimiterFlowRow.DelimiterFlowLayout
import com.km.composePlayground.components.dialog.*
import com.km.composePlayground.linkText.*
import com.km.composePlayground.modifiers.rememberState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScratchPadActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent(Recomposer.current()) {
      MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
//                TextAnnotation()
//          FullScreenDialogSample()
          DialogSample()
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
    ConstraintLayout(
      constraintSet = ConstraintSet {
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
      },
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
        )),
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
  val annotatedString = annotatedString {
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

  Stack(modifier = Modifier.fillMaxWidth()) {
    AnimatedVisibility(
      visible = (animState.numText == 1),
      enter = expandHorizontally(
        initialWidth = { (it * 0.1).toInt() },
        animSpec = tween(
          durationMillis = ANIM_DURATION_MS,
          easing = CubicBezierEasing(0.8f, 0.0f, 0.6f, 1.0f)
        )
      ),
      exit = shrinkHorizontally(
        shrinkTowards = Alignment.Start,
        targetWidth = { (it * 0.1).toInt() },
        animSpec = tween(
          durationMillis = ANIM_DURATION_MS,
          easing = CubicBezierEasing(0.8f, 0.0f, 0.6f, 1.0f)
        )
      ) + fadeOut(
        animSpec = tween(
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
      enter = fadeIn(initialAlpha = 0.3f, animSpec = tween()),
      exit = fadeOut(targetAlpha = 0.3f, animSpec = tween())
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
    animatedOpacity(tween(durationMillis = 250, delayMillis = 500), animState.value.visible) {
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

  Text(animState.value.current, modifier = Modifier.drawOpacity(opacity.value))
}

@Composable
private fun animatedOpacity(
  animation: AnimationSpec<Float>,
  visible: Boolean,
  onAnimationFinish: () -> Unit = {}
): AnimatedFloat {
  val animatedFloat = animatedFloat(if (!visible) 1f else 0f)
  onCommit(visible) {
    animatedFloat.animateTo(
      if (visible) 1f else 0f,
      anim = animation,
      onEnd = { reason, _ ->
        if (reason == AnimationEndReason.TargetReached) {
          onAnimationFinish()
        }
      })
  }
  return animatedFloat
}
