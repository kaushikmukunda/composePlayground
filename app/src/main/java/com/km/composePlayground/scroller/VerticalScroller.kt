package com.km.composePlayground.scroller

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUi

private val NO_CONTENT_PADDING = PaddingValues(0.dp)

/**
 * Displays vertically scrollable content.
 *
 * @param uiModel The UiModel for rendering the scroller.
 * @param mapper Transforms a UiModel to its Compose representation.
 * @param decorationCalculator Class which calculates the decorations to be applied to an item
 *     based on its UiModel
 * @param decorationModifierCalculator Class which calculates the modifiers to be applied to the
 *     item composables based on the decorations to be applied to the item
 * @param contentPadding a padding around the whole content. This will add padding for the
 *   content after it has been clipped, which is not possible via [modifier] param. Note that it is
 *   **not** a padding applied for each item's content.
 * @param containerModifier Optional modifier to be applied to the scroll container.
 */
@SuppressLint("ModifierParameter")
@Composable
fun VerticalScroller(
  uiModel: VerticalScrollerUiModel,
  mapper: UiModelComposableMapper,
  decorationCalculator: DecorationCalculator = EMPTY_DECORATION_CALCULATOR,
  decorationModifierCalculator: DecorationModifierCalculator = EMPTY_DECORATION_MODIFIER_CALCULATOR,
  contentPadding: PaddingValues = NO_CONTENT_PADDING,
  containerModifier: Modifier = Modifier
) = UniformUi(uiModel) { content ->
  val decorationResolver = remember(decorationCalculator, decorationModifierCalculator) {
    DecorationResolver(decorationCalculator, decorationModifierCalculator)
  }

  LazyColumn(modifier = containerModifier.fillMaxSize(), contentPadding = contentPadding) {
    for (listItem in content.itemList) {
      when (listItem) {
        is DynamicGridUiModel -> DynamicGridUi(listItem, mapper, decorationResolver)
        is StaticGridUiModel -> StaticGridUi(listItem, mapper, decorationResolver)
        is SectionUiModel -> LinearUi(listItem, mapper, decorationResolver)
        else -> item {
          mapper.map(listItem).invoke(Modifier)
        }
      }
    }
  }
}

/** Convenience class to allow Compose scrollers to obtain the Modifier for decoration. */
internal class DecorationResolver(
  private val decorationCalculator: DecorationCalculator,
  private val decorationModifierCalculator: DecorationModifierCalculator
) {

  @SuppressLint("ModifierFactoryExtensionFunction")
  fun getDecorationModifier(uiModel: UiModel): Modifier {
    return decorationModifierCalculator.getModifierForDecorations(
      decorationCalculator.getDecorationsForUiModel(uiModel)
    )
  }
}