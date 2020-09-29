package com.km.composePlayground.linkText

import android.util.Log
import androidx.compose.foundation.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.SpanStyleRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LinkTextUi(model: LinkTextUiModel) {
    ClickableText(
        text = buildAnnotatedString(model),
        style = model.textStyle,
        onClick = { offset ->
            if (offset >= model.text.length) {
                Log.d("dbg", "clicked")
                model.uiAction.onClick(Any())
            }
        },
    )
}

private fun buildAnnotatedString(model: LinkTextUiModel): AnnotatedString {
    val spanStyles = mutableListOf<SpanStyleRange>().apply {
        for (boldSpan in model.markdown.bold) {
            add(
                SpanStyleRange(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    start = boldSpan.start,
                    end = boldSpan.end
                )
            )
        }
        for (italicSpan in model.markdown.italics) {
            add(
                SpanStyleRange(
                    SpanStyle(fontStyle = FontStyle.Italic),
                    start = italicSpan.start,
                    end = italicSpan.end
                )
            )
        }

        // link style
        add(
            SpanStyleRange(
                SpanStyle(color = Color.Blue),
                start = model.text.length,
                end = model.text.length + model.linkText.length
            )
        )
    }

    return AnnotatedString(
        text = model.text + model.linkText,
        spanStyles = spanStyles
    )
}

