package com.km.composePlayground.scroller

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier

/**
 * Renders a group of UiModels linearly within a LazyList (Row or Column). To be used in the context
 * of a [VerticalScroller] which supports grid and linear arrangments alongside each other.
 */
internal fun LazyListScope.LinearUi(
  model: SectionUiModel,
  elementRenderer: ElementRenderer
) {
  val content = model.content.value
  items(content.itemList.size) { idx ->
    val item = content.itemList[idx]
    elementRenderer.Render(uiModel = item, modifier = Modifier)

    content.scrollingUiAction.onItemRendered(idx)
  }
}