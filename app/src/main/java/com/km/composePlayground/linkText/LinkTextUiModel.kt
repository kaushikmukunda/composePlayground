package com.km.composePlayground.linkText

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import com.km.composePlayground.base.UiModel

/** Actions user can perform on a link text. */
interface LinkTextUiAction {
    /** Called when the link text is clicked. */
    fun onClick(clickData: Any?)
}

/** Container class to represent Span style range. */
class Range(val start: Int, val end: Int)

/** Container to hold styles. */
class Markdown(
    val bold: List<Range>,
    val italics: List<Range>
)

/**
 * UiModel to configure LinkText.
 *
 * @param uiAction Callback interface for user actions.
 * @param text The non-clickable portion of the display text.
 * @param linkText The clickable portion of the display text.
 * @param textStyle Typography for the text.
 * @param markdown Markdown styles for the display text. (Only bold and italics are supported).
 */
@Immutable
class LinkTextUiModel(
    val uiAction: LinkTextUiAction,
    val text: String = "",
    val linkText: String = "",
    val textStyle: TextStyle,
    val markdown: Markdown
) : UiModel