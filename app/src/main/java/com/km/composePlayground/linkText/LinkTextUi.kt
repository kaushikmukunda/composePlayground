package com.km.composePlayground.linkText

import androidx.compose.foundation.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.SpanStyleRange

/**
 * LinkText renders text with a link. Supports bold and italic markdown on the non-clickable portion
 * of the text. Display Text=[text+linkText]
 *
 * Screenshot: https://screenshot.googleplex.com/47fAgLQHxnPynhE
 */
@Composable
fun LinkTextUi(model: LinkTextUiModel) {
//  PlayStoreVisualElement(model.containerLoggingData) {
  val linkColor = Color.Blue
  val annotatedString = remember(model, linkColor) { buildAnnotatedString(model, linkColor) }

  // LinkText logging requires separate logging nodes for the container and its clickable section.
//  PlayStoreVisualElement(model.linkLoggingData) { loggingModifier ->
  // Wrap clickable as ClickableText has custom onClick param
  val playStoreClickable = { model.uiAction.onClick(model.clickData) }//.playStoreClickable()

  ClickableText(
    text = annotatedString,
//      style = model.typography.toTextStyle(),
    onClick = { offset ->
      // Clickable portion of the text is always appended at the end of the display text.
      if (offset >= model.textMarkdown.text.length) {
        playStoreClickable.invoke()
      }
    },
//      modifier = loggingModifier
  )
//      }
}

private fun buildAnnotatedString(model: LinkTextUiModel, linkColor: Color): AnnotatedString {
  val spanStyles = mutableListOf<SpanStyleRange>().apply {
    addAll(model.textMarkdown.markdown.getStyleSpans())
    addAll(
      model.linkTextMarkdown.markdown
        .getStyleSpans(rangeOffset = model.textMarkdown.text.length)
    )

    add(
      SpanStyleRange(
        SpanStyle(color = linkColor),
        start = model.textMarkdown.text.length,
        end = model.textMarkdown.text.length + model.linkTextMarkdown.text.length
      )
    )
  }

  return AnnotatedString(
    text = model.textMarkdown.text + model.linkTextMarkdown.text,
    spanStyles = spanStyles
  )
}