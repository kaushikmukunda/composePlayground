package com.km.composePlayground.scroller.common

import androidx.compose.ui.Modifier

/**
 * Interface which calculates the modifiers to apply to the composable based on the decorations to
 * be applied to the item.
 */
fun interface DecorationModifierCalculator {
  fun getModifierForDecorations(decorationList: List<Decoration>): Modifier
}

/** Placeholder calculator that provides no decorations. */
val EMPTY_DECORATION_MODIFIER_CALCULATOR: DecorationModifierCalculator =
  DecorationModifierCalculator {
    Modifier
  }
