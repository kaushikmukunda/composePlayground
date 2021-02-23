package com.km.composePlayground.components.delimiterFlowRow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimatedFloat
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.km.composePlayground.modifiers.rememberState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Arranges children in left-to-right flow, packing as many child views as possible on
 * each line. When the current line does not have enough horizontal space, the layout continues on
 * the next line. Can be configured to separate children with any delimiter composable.
 *
 * The delimiter composable is inserted between each composable child. The delimiter composable
 *  is tagged with [DELIMITER_TAG] to identify and trim any trailing delimiters.
 *
 * When the number of lines required to layout all [children] exceeds [numLines], the layout
 * continuously cycles the content in order to display all the children. To do so, this is built
 * as a hierarchial composable. The top level composable is responsible for the animation.
 * The second level [DelimiterFlowLayoutInternal] is responsible for the actual layout.
 *
 * Once x of n children are placed in a layout, [onChildrenPlaced] is invoked with the index of
 * the last child placed. If there are more children to be placed, this layout is faded out with
 * a delay and then faded in with the next set of children.
 *
 * Time t0: Fade in to display 1..x children
 * Time t0 + animation delay: Fade out to display 1..x children
 * Time t0 + animation delay + fadeout animation duration: Fade in to displays x..n children
 *
 * @param horizontalArrangement Optional specifies the arrangement of the child views.
 *      ex: START to place the views as close as possible to the beginning of the row.
 * @param numLines Optional total number of lines to display.
 * @param modifier Optional modifier to be applied to this Layout.
 * @param delimiter Composable that is to be used as the delimiter between child composables.
 * @param children List of child composables that need to be rendered within the layout.
 */
@Composable
fun DelimiterFlowLayout(
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  numLines: Int = 1,
  modifier: Modifier = Modifier,
  delimiter: @Composable() (Modifier) -> Unit,
  children: List<@Composable() () -> Unit>
) {
  var animationState by rememberState { AnimationState() }

  val opacity = animatedOpacity(
    animation = tween(
      durationMillis = ANIMATION_DURATION_MS,
      easing = LinearEasing
    ),
    visible = animationState.visible,
    onAnimationFinish = {
      // If the current and next indexes are the same, all the content fit in the available space,
      // no animation is required.
      if (animationState.currentIndex == animationState.nextIndex) {
        return@animatedOpacity
      }

      MainScope().launch {
        // Leave the current content visible for ANIMATION_DELAY_MS before fading out.
        if (animationState.visible) {
          delay(ANIMATION_DELAY_MS)
        }

        // If the animationState is currently visible, the opacity will be changed to invisible.
        // We want to keep the currentIndex for the fade-out so that the same content is faded out.
        // On the flip side, if the current visibility is invisible, update current index to the
        // next index for the fade-in transition.
        animationState = AnimationState(
          visible = !animationState.visible,
          currentIndex =
          if (animationState.visible) animationState.currentIndex else animationState.nextIndex,
          nextIndex = animationState.nextIndex
        )
      }
    }
  )

  DelimiterFlowLayoutInternal(
    horizontalArrangement = horizontalArrangement,
    numLines = numLines,
    startIdx = animationState.currentIndex,
    modifier = modifier
      .alpha(opacity.value)
      .onChildrenPlaced { idx, isLastIdx ->
        // If the lastIdx was placed, we need to loop back from the start.
        animationState.nextIndex = if (isLastIdx) 0 else idx
      },
    delimiter = delimiter,
    children = children
  )
}

/** Invoke [OnChildrenPlaced] with the nextChild index that needs to be placed. */
private fun Modifier.onChildrenPlaced(onRendered: (Int, Boolean) -> Unit): Modifier =
  this.then(
    object : OnChildrenPlaced {
      override fun onPlaced(nextChildIdx: Int, isLastIndex: Boolean) {
        onRendered(nextChildIdx, isLastIndex)
      }
    }
  )

/**
 * A Modifier who's onLayout is called when [DelimiterFlowLayoutInternal] has placed children
 * up to the specified number of lines.
 */
private interface OnChildrenPlaced : Modifier.Element {

  fun onPlaced(nextChildIdx: Int, isLastIndex: Boolean)
}

/** Layout animation constants. */
private const val ANIMATION_DELAY_MS = 2000L
private const val ANIMATION_DURATION_MS = 250

/**
 * State of the ongoing animation.
 *
 * @property visible True if the layout is visible or fading in, otherwise False
 * @property currentIndex Child index at which to begin rendering children.
 * @property nextIndex Child index at which to begin rendering children in the subsequent fade-in.
 */
private class AnimationState(
  var visible: Boolean = true,
  var currentIndex: Int = 0,
  var nextIndex: Int = 0
)

/**
 * Triggers an animation when [visible] param is updated. This is to be used in conjuction with
 * [Modifier.drawOpacity].
 */
@Composable
private fun animatedOpacity(
  animation: AnimationSpec<Float>,
  visible: Boolean,
  onAnimationFinish: () -> Unit = {}
): AnimatedFloat {
  val animatedFloat = remember { Animatable(if (!visible) 1f else 0f) }
  SideEffect {
    animatedFloat.animateTo(
      if (visible) 1f else 0f,
      anim = animation,
      onEnd = { reason, _ ->
        if (reason == AnimationEndReason.TargetReached) {
          onAnimationFinish()
        }
      }
    )
  }
  return animatedFloat
}

/**
 * Arranges children in left-to-right flow, packing as many child views as possible on
 * each line. When the current line does not have enough horizontal space, the layout continues on
 * the next line. Can be configured to separate children with any delimiter composable.
 *
 * The delimiter composable is inserted between each composable child. The delimiter composable
 *  is tagged with [DELIMITER_ID] to identify and trim any trailing delimiters.
 *
 * @property horizontalArrangement Optional specifies the arrangement of the child views.
 *      ex: START to place the views as close as possible to the beginning of the row.
 * @property numLines Optional total number of lines to display.
 * @property startIdx The index of the first child to display.
 * @property modifier Optional modifier to be applied to this Layout.
 * @property delimiter Composable that is to be used as the delimiter between child composables.
 * @property children List of child composables that need to be rendered within the layout.
 *
 */
@Composable
private fun DelimiterFlowLayoutInternal(
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  numLines: Int = 1,
  startIdx: Int,
  modifier: Modifier = Modifier,
  delimiter: @Composable() (Modifier) -> Unit,
  children: List<@Composable() () -> Unit>
) {
  // The layout logic is derived from Jetpack Compose Flow layout : http://shortn/_o7nTAEQgPM
  Layout(
    content = {
      for (child in children) {
        child()
        delimiter(Modifier.layoutId(DELIMITER_ID))
      }
    },
    modifier = modifier
  ) { measurables, outerConstraints ->
    val sequences = mutableListOf<List<Placeable>>()
    val rowHeights = mutableListOf<Int>()
    val rowVerticalPositions = mutableListOf<Int>()

    var totalWidth = 0
    var totalHeight = 0

    val currentSequence = mutableListOf<Placeable>()
    var currentWidth = 0
    var currentHeight = 0

    val childConstraints = Constraints(maxWidth = outerConstraints.maxWidth)
    var delimiterWidth = 0

    var measurableIdx = startIdx - 1

    fun trimLastLineDelimiter() {
      if (measurables[measurableIdx].layoutId == DELIMITER_ID) {
        currentSequence.removeAt(currentSequence.lastIndex)
      }
    }

    // Return whether the placeable can be added to the current sequence.
    fun canAddToCurrentSequence(placeable: Placeable): Boolean {
      return currentSequence.isEmpty() ||
        (currentWidth + placeable.width + delimiterWidth) <= outerConstraints.maxWidth
    }

    // Store current sequence information and start a new sequence.
    fun commitCurrentSequence() {
      sequences += currentSequence.toList()
      rowHeights += currentHeight
      rowVerticalPositions += totalHeight

      totalHeight += currentHeight
      totalWidth = max(totalWidth, currentWidth)

      currentSequence.clear()
      currentWidth = 0
      currentHeight = 0
    }

    fun commitLastSequence() {
      trimLastLineDelimiter()
      commitCurrentSequence()
    }

    for (idx in startIdx until measurables.size) {
      val measurable = measurables[idx]
      measurableIdx++

      // Ask the child for its preferred size.
      val placeable = measurable.measure(childConstraints)

      // Lazily initialize delimiter width
      if (delimiterWidth == 0 && measurable.layoutId == DELIMITER_ID) {
        delimiterWidth = placeable.width
      }

      // Start a new sequence if there is not enough space.
      if (!canAddToCurrentSequence(placeable)) commitCurrentSequence()

      // Stop placing children if it already filled configured number of lines
      if (sequences.size >= numLines) {
        break
      }

      // Do not start line with delimiter
      if (currentSequence.isEmpty() && measurable.layoutId == DELIMITER_ID) {
        continue
      }

      // Add the child to the current sequence.
      currentSequence.add(placeable)
      currentWidth += placeable.width
      currentHeight = max(currentHeight, placeable.height)
    }

    // Add last line
    if (currentSequence.isNotEmpty()) commitLastSequence()

    modifier.foldOut(null) { mod, _ ->
      if (mod is OnChildrenPlaced) {
        mod.onPlaced(measurableIdx, measurableIdx == measurables.lastIndex)
      }
      null
    }

    val layoutWidth = max(totalWidth, outerConstraints.minWidth)
    val layoutHeight = max(totalHeight, outerConstraints.minHeight)

    // Layout all the children
    // Note: This is based of java/com/google/supplychain/scales/store/access/FlowRow.java
    layout(layoutWidth, layoutHeight) {
      sequences.fastForEachIndexed { i, placeables ->
        val childrenWidths = IntArray(placeables.size) { j -> placeables[j].width }
        val horizontalPositions = IntArray(placeables.size)

        with(horizontalArrangement) {
          arrange(layoutWidth, childrenWidths, layoutDirection, horizontalPositions)
        }

        placeables.fastForEachIndexed { j, placeable ->
          // If delimiter and content are of unequal height, offset ensures they are center aligned.
          val verticalOffset = Alignment.CenterVertically.align(
            rowHeights[i] - placeable.height, placeable.height
          )

          placeable.place(
            x = horizontalPositions[j],
            y = rowVerticalPositions[i] + verticalOffset
          )
        }
      }
    }
  }
}

/**
 * Layout Id to be used with delimiter composable so that the [DelimiterFlowLayout] can identify
 * delimiter composables and layout views.
 */
internal const val DELIMITER_ID = "delimiterId"

/**
 * A Bullet delimiter that can be used with [DelimiterFlowLayout].
 *
 * @property bulletColor Optional color for the bullet. Defaults to current theme text primary.
 * @property bulletGap Optional the horizontal padding on either side of the bullet.
 * @property bulletRadius Optional size of the bullet.
 */
@Composable
fun BulletDelimiter(
  bulletColor: Color = MaterialTheme.colors.primary,
  bulletRadius: Dp = 2.dp,
  bulletGap: Dp = 6.dp,
  modifier: Modifier
) {
  Text(
    text = "",
    modifier = modifier
      .padding(horizontal = bulletGap)
      .drawWithContent {
        drawCircle(color = bulletColor, radius = bulletRadius.toPx())
      }
  )
}

/**
 * An empty space delimiter that can be used with [DelimiterFlowLayout].
 *
 * @property width The width of the empty space.
 */
@Composable
fun SpaceDelimiter(width: Dp, modifier: Modifier) {
  Spacer(modifier = modifier.width(width = width))
}
