package com.km.composePlayground.scroller

import androidx.compose.foundation.lazy.LazyListScope

/**
 * Renders a group of UiModels linearly within a LazyList (Row or Column). To be used in the context
 * of a [VerticalScroller] which supports grid and linear arrangments alongside each other.
 */
fun LazyListScope.LinearUi(
  model: SectionUiModel,
  mapper: UiModelComposableMapper,
  decorationResolver: DecorationResolver) {
  val items = model.content.value.itemList
  items(items.size) { idx ->
    val item = items[idx]
    renderItem(
      item = item,
      mapper = mapper,
      modifier = decorationResolver.getDecorationModifier(item)
    )
  }
}