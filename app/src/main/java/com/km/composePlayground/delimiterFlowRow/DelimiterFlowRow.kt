package com.km.composePlayground.delimiterFlowRow

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animatedFloat
import androidx.compose.animation.core.*
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.id
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

private const val ANIMATION_DELAY_MS = 2000L
private const val ANIMATION_DURATION_MS = 250
private const val ANIMATION_REPEAT = 2

class AnimationState(
    var visible: Boolean = true,
    var currentIndex: Int = 0,
    var nextIndex: Int = 0
)

private fun Modifier.OnFlowLayoutRendered(onRendered: (Int, Boolean) -> Unit): Modifier =
    this.then(object : OnFlowLayoutRendered {
        override fun onFlowLayoutRendered(lastChildIdxPlaced: Int, isLastIndex: Boolean) {
            onRendered(lastChildIdxPlaced, isLastIndex)
        }
    })

private interface OnFlowLayoutRendered : Modifier.Element {

    fun onFlowLayoutRendered(lastChildIdxPlaced: Int, isLastIndex: Boolean)

}

/**
 * Arranges children in left-to-right flow, packing as many child views as possible on
 * each line. When the current line does not have enough horizontal space, the layout continues on
 * the next line. Can be configured to separate children with any delimiter composable.
 *
 * The delimiter composable is inserted between each composable child. The delimiter composable
 *  is tagged with [DELIMITER_TAG] to identify and trim any trailing delimiters.
 *
 * @property horizontalArrangement Optional specifies the arrangement of the child views.
 *      ex: START to place the views as close as possible to the beginning of the row.
 * @property numLines Optional total number of lines to display.
 * @property modifier Optional modifier to be applied to this Layout.
 * @property delimiter Composable that is to be used as the delimiter between child composables.
 * @property children List of child composables that need to be rendered within the layout.
 *
 */

@Composable
@OptIn(InternalLayoutApi::class, ExperimentalAnimationApi::class)
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
            if (animationState.currentIndex == animationState.nextIndex) {
                return@animatedOpacity
            }

            MainScope().launch {
                if (animationState.visible) {
                    delay(ANIMATION_DELAY_MS)
                }

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
            .drawOpacity(opacity.value)
            .OnFlowLayoutRendered { idx, isLastIdx ->
                animationState.nextIndex = if (isLastIdx) 0 else idx
            },
        delimiter = delimiter,
        children = children
    )
}

@Composable
private fun animatedOpacity(
    animation: AnimationSpec<Float>,
    visible: Boolean,
    onAnimationFinish: () -> Unit = {}
): AnimatedFloat {
    val animatedFloat = animatedFloat(if (!visible) 1f else 0f)
    onCommit(visible) {
        animatedFloat.animateTo(
            if (visible) 1f else 0f,
            anim = animation,
            onEnd = { reason, _ ->
                if (reason == AnimationEndReason.TargetReached) {
                    onAnimationFinish()
                }
            })
    }
    return animatedFloat
}


@Composable
@OptIn(InternalLayoutApi::class)
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
        children = {
            for (child in children) {
                child()
                delimiter(Modifier.layoutId(DELIMITER_TAG))
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

        var measurableIdx = startIdx - 1
        var delimiterWidth = 0

        fun trimLastLineDelimiter() {
            if (measurables[measurableIdx].id == DELIMITER_TAG) {
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

        fun startNewSequence() {
            commitCurrentSequence()
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
            if (delimiterWidth == 0 && measurable.id == DELIMITER_TAG) {
                delimiterWidth = placeable.width
            }

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Stop placing children if it already filled configured number of lines
            if (sequences.size >= numLines) {
                break
            }

            // Do not start line with delimiter
            if (currentSequence.isEmpty() && measurable.id == DELIMITER_TAG) {
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
            if (mod is OnFlowLayoutRendered) {
                mod.onFlowLayoutRendered(measurableIdx, measurableIdx == measurables.lastIndex)
            }
            null
        }

        val layoutWidth = max(totalWidth, outerConstraints.minWidth)
        val layoutHeight = max(totalHeight, outerConstraints.minHeight)

        // Layout all the children
        // Note: This is based of java/com/google/supplychain/scales/store/access/FlowRow.java
        layout(layoutWidth, layoutHeight) {
            sequences.fastForEachIndexed { i, placeables ->
                val childrenWidths = placeables.map { placeable -> placeable.width }

                val horizontalPositions = horizontalArrangement.arrange(
                    layoutWidth,
                    childrenWidths,
                    layoutDirection
                )

                placeables.fastForEachIndexed { j, placeable ->
                    val verticalOffset = Alignment.CenterVertically.align(
                        rowHeights[i] - placeable.height
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
 * Tag to be used with delimiter composable so that the [DelimiterFlowLayout] can identify
 * delimiter composables and layout views.
 */
const val DELIMITER_TAG = "delimiterTag"

/**
 * A Bullet delimiter that can be used with [DelimiterFlowLayout].
 *
 * @property bulletColor Optional color for the bullet. Defaults to current theme text primary.
 * @property bulletGap Optional the horizontal padding on either side of the bullet.
 * @property bulletRadius Optional size of the bullet.
 */
@Composable
fun BulletDelimiter(
    bulletColor: Color = Color.Unset,
    bulletRadius: Dp = 0.dp,
    bulletGap: Dp = 0.dp,
    modifier: Modifier = Modifier.layoutId(DELIMITER_TAG)
) {
    val color = if (bulletColor == Color.Unset) Color.Black else bulletColor
    Text(
        text = "",
        modifier = modifier
            .padding(horizontal = bulletGap)
            .drawWithContent {
                drawCircle(color = color, radius = bulletRadius.toPx())
            }
    )
}

/**
 * An empty space delimiter that can be used with [DelimiterFlowLayout].
 *
 * @property width The width of the empty space.
 */
@Composable
fun SpaceDelimiter(width: Dp) {
    Spacer(modifier = Modifier.width(width = width))
}
