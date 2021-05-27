package com.km.composePlayground.scroller.horizontal


import androidx.compose.foundation.lazy.LazyListState

/**
 * Contract for [LayoutPolicyAwareScroller]. LayoutPolicyAwareHorizontalScroller renders a scrolling
 * list of children UI laid out based on different layout policies.
 *
 * @property enableSnapping when true, the scroller will snap to the start of the child that is
 * closest to the start of the screen.
 * @property lazyListState the state object to be used to control or observe the list's state.
 * @property scrollerAnimationState the state object to keep track of items that have already been
 * animated in.
 */
class LayoutPolicyHorizontalScrollerComposeUiModel(
  initialContent: HorizontalScrollerComposeUiContent,
  lazyListState: LazyListState = LazyListState(),
  scrollerAnimationState: ScrollerAnimationState = ScrollerAnimationState(initialContent.items),
  val enableSnapping: Boolean = false
) : HorizontalScrollerComposeUiModel(initialContent, lazyListState, scrollerAnimationState)
