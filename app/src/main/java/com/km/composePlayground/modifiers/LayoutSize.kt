package com.km.composePlayground.modifiers


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned

/** A modifier which will update the provided [LayoutSize] to enable its querying API. */
@Composable
fun Modifier.layoutSizeCache(layoutSize: LayoutSize): Modifier {
  return this.then(Modifier.onGloballyPositioned {
    layoutSize.width = it.size.width
    layoutSize.height = it.size.height
  })
}

/** Provides a [remember]ed LayoutSize that will cache its internal state. */
@Composable
fun layoutSize(): LayoutSize {
  return remember { LayoutSize(0, 0) }
}

/** Container object for layout width and height. */
class LayoutSize(var width: Int, var height: Int)
