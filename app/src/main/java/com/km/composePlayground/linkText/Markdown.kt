package com.km.composePlayground.linkText

import android.graphics.Typeface
import android.text.Html
import android.text.ParcelableSpan
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.URLSpan
import androidx.annotation.IntRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.fastForEach

/**
 * Container class to represent Span style range.
 *
 * @property start The start of the range. It's inclusive
 * @property end The end of the range. It's exclusive
 */
class Range(@IntRange(from = 0) val start: Int, val end: Int)

class UrlSpan(
  val range: Range,
  val url: String
)

/**
 * Container to hold span styles.
 *
 * @property bold List of range of text to apply a bold span.
 * @property italics List of range of text to apply an italic span.
 */
class Markdown(
  val bold: List<Range>,
  val italics: List<Range>,
  val urls: List<UrlSpan>
)

/**
 * Container class that converts Html text to plain text and its markdown.
 *
 * @property text Plain text from input html.
 * @property markdown Markdown associated with the input html.
 */
class MarkdownText(htmlText: String) {
  val text: String
  val markdown: Markdown

  init {
    val spannedText = Html.fromHtml(htmlText)
    text = spannedText.toString()
    markdown = extractMarkdown(spannedText)
  }

  private fun extractMarkdown(spannedText: Spanned): Markdown {
    val boldSpans = mutableListOf<Range>()
    val italicSpans = mutableListOf<Range>()
    val urlSpans = mutableListOf<UrlSpan>()

    val spans = spannedText.getSpans(0, spannedText.length, ParcelableSpan::class.java)

    spans.forEach { span ->
      when (span) {
        is StyleSpan -> {
          val styleSpan = extractStyleSpan(spannedText, span)
          when (styleSpan?.typeface) {
            Typeface.BOLD -> boldSpans.add(styleSpan.range)
            Typeface.ITALIC -> italicSpans.add(styleSpan.range)
          }
        }

        is URLSpan -> {
          urlSpans.add(extractUrlSpan(spannedText, span))
        }
      }
    }

    return Markdown(boldSpans, italicSpans, urlSpans)
  }

  private fun extractStyleSpan(spannedText: Spanned, span: StyleSpan): SpanRange? {
    return when (span.style) {
      Typeface.BOLD -> {
        SpanRange(
          Range(spannedText.getSpanStart(span), spannedText.getSpanEnd(span)),
          Typeface.BOLD
        )
      }

      Typeface.ITALIC -> {
        SpanRange(
          Range(spannedText.getSpanStart(span), spannedText.getSpanEnd(span)),
          Typeface.BOLD
        )
      }
      else -> null
    }
  }

  private fun extractUrlSpan(spannedText: Spanned, span: URLSpan): UrlSpan {
    return UrlSpan(
      Range(spannedText.getSpanStart(span), spannedText.getSpanEnd(span)),
      span.url
    )
  }

  private class SpanRange(
    val range: Range,
    val typeface: Int
  )
}

fun Markdown.getStyleSpans(rangeOffset: Int = 0): List<AnnotatedString.Range<SpanStyle>> {
  return mutableListOf<AnnotatedString.Range<SpanStyle>>().apply {
    bold.fastForEach { boldSpan ->
      add(
        AnnotatedString.Range<SpanStyle>(
          SpanStyle(fontWeight = FontWeight.Bold),
          start = boldSpan.start + rangeOffset,
          end = boldSpan.end + rangeOffset
        )
      )
    }

    italics.fastForEach { italicSpan ->
      add(
        AnnotatedString.Range(
          SpanStyle(fontStyle = FontStyle.Italic),
          start = italicSpan.start + rangeOffset,
          end = italicSpan.end + rangeOffset
        )
      )
    }
  }
}
