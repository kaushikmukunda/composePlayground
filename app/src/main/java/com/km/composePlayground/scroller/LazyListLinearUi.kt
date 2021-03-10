package com.km.composePlayground.scroller

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.SideEffect
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
  itemsIndexed(
    items = content.itemList,
    key = { _, item -> "${model.content.value.dataId}_${item.hashCode()}" }
  ) { idx, item ->
    elementRenderer.Render(uiModel = item, modifier = Modifier)

    SideEffect {
      content.scrollingUiAction.onItemRendered(idx)
    }
  }
}