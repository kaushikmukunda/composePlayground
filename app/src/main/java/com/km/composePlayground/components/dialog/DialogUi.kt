package com.km.composePlayground.components.dialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.SpanStyleRange
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import com.km.composePlayground.components.button.ButtonUiAction
import com.km.composePlayground.components.buttongroup.*
import com.km.composePlayground.linkText.MarkdownText
import com.km.composePlayground.linkText.getStyleSpans

private const val HEADER_ID = "header_id"
private const val CONTENT_ID = "content_id"
private const val FOOTER_ID = "footer_id"

@Stable
class DialogComposer(private val buttonGroupComposer: ButtonGroupComposer) {

  @Composable
  fun compose(model: DialogUiModel) {
    DialogUi(buttonGroupComposer, model)
  }
}

@Composable
private fun DialogUi(buttonGroupComposer: ButtonGroupComposer, model: DialogUiModel) {
  val openDialog = remember { mutableStateOf(true) }
  if (!openDialog.value) return

  fun dismiss() {
    openDialog.value = false
  }

  Dialog(onDismissRequest = {
    dismiss()
    model.uiAction.onDismiss(model.dialogData)
  }) {
    ConstraintLayout(
      constraintSet = ConstraintSet {
        val headerRef = createRefFor(HEADER_ID)
        val contentRef = createRefFor(CONTENT_ID)
        val footerRef = createRefFor(FOOTER_ID)

        constrain(headerRef) {
          top.linkTo(parent.top)
          bottom.linkTo(contentRef.top)
          linkTo(start = parent.start, end = parent.end, bias = 0f)
        }

        constrain(contentRef) {
          top.linkTo(headerRef.bottom)
          bottom.linkTo(footerRef.top)
          linkTo(start = parent.start, end = parent.end, bias = 0f)
        }

        constrain(footerRef) {
          start.linkTo(parent.start)
          end.linkTo(parent.end)
          top.linkTo(contentRef.bottom)
          bottom.linkTo(parent.bottom)
        }
      },
      modifier = Modifier
        .background(color = MaterialTheme.colors.background, shape = RoundedCornerShape(corner = CornerSize(8.dp)))
        .fillMaxWidth()
        .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)
    ) {
      val footerAction = FooterAction(
        positiveAction = {
          model.uiAction.onPositiveButtonClicked(model.dialogData)
          dismiss()
        },
        negativeAction = {
          model.uiAction.onNegativeButtonClicked(model.dialogData)
          dismiss()
        }
      )

      val contentAction = { url: String -> model.uiAction.onLinkClicked(url, model.dialogData) }

      HeaderUi(model.header, modifier = Modifier.layoutId(HEADER_ID))
      ContentUi(model.content, contentAction, modifier = Modifier.layoutId(CONTENT_ID))
      FooterUi(
        buttonGroupComposer,
        model.footer,
        footerAction,
        modifier = Modifier.layoutId(FOOTER_ID)
      )
    }
  }
}

@Composable
private fun HeaderUi(model: HeaderModel?, modifier: Modifier) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    model?.let {
      Text(text=it.title, maxLines = 2, overflow = TextOverflow.Clip)
    }
  }
}

@Composable
private fun ContentUi(model: ContentModel, contentAction: (String) -> Unit, modifier: Modifier) {
  val scrollstate = rememberScrollState()
  val urlSpans = model.content.markdown.urls
  ClickableText(
    text = buildAnnotatedString(model.content),
    modifier = modifier.padding(vertical = 16.dp).heightIn(max = 448.dp).verticalScroll(scrollstate),
    onClick = { offset ->
      for (urlSpan in urlSpans) {
        if (offset >= urlSpan.range.start && offset < urlSpan.range.end) {
          contentAction.invoke(urlSpan.url)
          break
        }
      }
    }
  )
}

private fun buildAnnotatedString(markdownText: MarkdownText): AnnotatedString {
  val spanStyles = mutableListOf<SpanStyleRange>().apply {
    addAll(markdownText.markdown.getStyleSpans())

    markdownText.markdown.urls.fastForEach { urlSpan ->
      add(
        SpanStyleRange(
          SpanStyle(color = Color.Gray, textDecoration = TextDecoration.Underline),
          start = urlSpan.range.start,
          end = urlSpan.range.end
        )
      )
    }
  }

  return AnnotatedString(
    text = markdownText.text,
    spanStyles = spanStyles
  )
}

@Composable
private fun FooterUi(
  buttonGroupComposer: ButtonGroupComposer,
  model: FooterModel,
  action: FooterAction,
  modifier: Modifier
) {
  buttonGroupComposer.compose(
    model = createButtonGroupModel(model, action),
    modifier = modifier
  )
}

private fun createButtonGroupModel(model: FooterModel, action: FooterAction): ButtonGroupUiModel {
  val positiveButtonUiAction = wrapButtonUiAction { action.positiveAction() }
  val negativeButtonUiAction = wrapButtonUiAction { action.negativeAction() }

  val positiveButtonConfig =
    createButtonConfigForDialog(model.positiveButtonConfig, positiveButtonUiAction)
  val negativeButtonConfig =
    createButtonConfigForDialog(model.negativeButtonConfig, negativeButtonUiAction)

  return ButtonGroupUiModel(
    buttonGroupVariant = ButtonGroupVariant.INVISIBLE_INVISIBLE,
    buttonGroupSnapping = ButtonGroupSnapping.LEFT,
    firstButtonConfig = positiveButtonConfig,
    secondButtonConfig = negativeButtonConfig
  )
}

private fun wrapButtonUiAction(dialogAction: () -> Unit): ButtonUiAction {
  return ButtonUiAction({}, onClick = { dialogAction() }, onTouch = { _, _ -> })
}

private fun createButtonConfigForDialog(
  dialogButtonConfig: DialogButtonConfig,
  buttonUiAction: ButtonUiAction
): ButtonConfig {
  return ButtonConfig(
    buttonText = dialogButtonConfig.buttonText,
    uiAction = buttonUiAction,
    clickData = null
  )
}

private class FooterAction(
  val positiveAction: () -> Unit,
  val negativeAction: () -> Unit
)


