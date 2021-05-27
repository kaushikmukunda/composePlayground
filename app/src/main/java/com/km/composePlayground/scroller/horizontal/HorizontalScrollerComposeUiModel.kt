package com.km.composePlayground.scroller.horizontal


import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.km.composePlayground.base.UiModel
import com.km.composePlayground.base.UniformUiModel
import kotlin.math.max

/**
 * Keeps track of the state of the scroller relevant to item animation.
 *
 * @property items The list of UiModels backing the Scroller.
 * @property maxIdxRendered The max index of the rendered model in the scroller. This is to ensure
 * that only new items are animated.
 */
class ScrollerAnimationState(initialItems: List<UiModel>, var maxIdxRendered: Int = 0) {

  val items: MutableList<UiModel?> = initialItems.toMutableList()

  /**
   * This keeps the backing list of items up to date. This is expected to be invoked once an [item]
   * at [renderedIdx] is rendered.
   */
  fun updateState(renderedIdx: Int, item: UiModel) {
    maxIdxRendered = max(maxIdxRendered, renderedIdx)

    // If the new item does not match the backing list, replace or add to the list.
    // This occurs after the UI renders the replacement animation, this is safe to do.
    if (items.getOrNull(renderedIdx) != item) {
      // This can happen when a horizontal scroller is within a vertical scroller.
      // Fling horizontal followed by a vertical scroll causing the horizontal scroller to recycle
      // will mean that some items never got rendered and the state never updated.
      // Increase the list size and update the current index. The gaps will be filled when user
      // scrolls back.
      if (items.size < renderedIdx) {
        for (i in items.size..renderedIdx) {
          items.add(null)
        }
      }

      if (items.size == renderedIdx) {
        items.add(item)
      } else {
        items[renderedIdx] = item
      }
    }
  }
}

/**
 * UiContent for the horizontal scroller.
 *
 * @property uiAction Ui actions supported by the scroller.
 * @property items The UiModels that are to be rendered in a list.
 */
@Immutable
class HorizontalScrollerComposeUiContent(
  val uiAction: ScrollingUiAction,
  val items: List<UiModel>,
)

/**
 * UiModel that establishes contract with HorizontalScrollerUi.
 *
 * @property content Content that updates together for recomposition.
 * @property lazyListState the state object to be used to control or observe the list's state While
 * this should be an argument during a compose call, this is being hoisted to the UiModel to
 * preserve state. A composable hosted within a recycler view loses its state when a view gets
 * recycled.
 * @property scrollerAnimationState the state object to keep track of items that have already been
 * animated in. Hoisting this is a workaround to preserve state for a composble hosted in a recycler
 * view. !! Nothing but HorizontalScroller composable should access this !! TODO(b/186128528) Remove
 * once we migrate to LazyColumn
 */
@Stable
open class HorizontalScrollerComposeUiModel(
  horizontalScrollerUiContent: HorizontalScrollerComposeUiContent,
  val lazyListState: LazyListState = LazyListState(),
  val scrollerAnimationState: ScrollerAnimationState = ScrollerAnimationState(
    horizontalScrollerUiContent.items
  )
) : UniformUiModel<HorizontalScrollerComposeUiContent> {
  override val content = mutableStateOf(horizontalScrollerUiContent)
}
