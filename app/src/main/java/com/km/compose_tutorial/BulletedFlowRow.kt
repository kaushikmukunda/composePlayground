package com.km.compose_tutorial

import androidx.compose.Composable
import androidx.ui.core.Constraints
import androidx.ui.core.Layout
import androidx.ui.core.Modifier
import androidx.ui.core.Placeable
import androidx.ui.core.drawWithContent
import androidx.ui.core.tag
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Spacer
import androidx.ui.layout.padding
import androidx.ui.layout.width
import androidx.ui.unit.Dp
import androidx.ui.unit.IntPx
import androidx.ui.unit.dp
import androidx.ui.unit.max
import androidx.ui.util.fastForEachIndexed

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
fun DelimiterFlowLayout(
    horizontalArrangement: Arrangement = Arrangement.Start,
    numLines: Int = 1,
    modifier: Modifier = Modifier,
    delimiter: @Composable() (Modifier) -> Unit,
    children: List<@Composable() () -> Unit>
) {
    // The layout logic is derived from Jetpack Compose Flow layout : http://shortn/_o7nTAEQgPM
    Layout(
        children = {
            for (child in children) {
                child()
                delimiter(Modifier.tag(DELIMITER_TAG))
            }
        },
        modifier = modifier
    ) { measurables, outerConstraints, layoutDirection ->
        val sequences = mutableListOf<List<Placeable>>()
        val rowHeights = mutableListOf<IntPx>()
        val rowVerticalPositions = mutableListOf<IntPx>()

        var totalWidth = IntPx.Zero
        var totalHeight = IntPx.Zero

        val currentSequence = mutableListOf<Placeable>()
        var currentWidth = IntPx.Zero
        var currentHeight = IntPx.Zero

        val childConstraints = Constraints(maxWidth = outerConstraints.maxWidth)

        var measurableIdx = 0

        fun trimDelimiter() {
            if (measurables[measurableIdx - 1].tag == DELIMITER_TAG) {
                currentSequence.removeAt(currentSequence.lastIndex)
            }
        }

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable): Boolean {
            return currentSequence.isEmpty() ||
                    (currentWidth + placeable.width) <= outerConstraints.maxWidth
        }

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            trimDelimiter()
            sequences += currentSequence.toList()
            rowHeights += currentHeight
            rowVerticalPositions += totalHeight

            totalHeight += currentHeight
            totalWidth = max(totalWidth, currentWidth)

            currentSequence.clear()
            currentWidth = IntPx.Zero
            currentHeight = IntPx.Zero
        }

        for (measurable in measurables) {
            // Ask the child for its preferred size.
            val placeable = measurable.measure(childConstraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Stop placing children if it already filled configured number of lines
            if (sequences.size >= numLines) {
                break
            }

            // Do not start line with delimiter
            if (currentSequence.isEmpty() && measurable.tag == DELIMITER_TAG) {
                continue
            }

            // Add the child to the current sequence.
            currentSequence.add(placeable)
            currentWidth += placeable.width
            currentHeight = max(currentHeight, placeable.height)

            measurableIdx++
        }

        // Add last line
        if (currentSequence.isNotEmpty()) startNewSequence()

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
                    placeable.placeAbsolute(
                        x = horizontalPositions[j],
                        y = rowVerticalPositions[i]
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
    modifier: Modifier = Modifier.tag(DELIMITER_TAG)
) {
    val color = if (bulletColor == Color.Unset) Color.Black else bulletColor
    Text(
        text = "",
        modifier = modifier
            .padding(horizontal = bulletGap)
            .drawWithContent {
                drawCircle(color = color, radius = bulletRadius.toPx().value)
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
    Spacer(modifier = Modifier.width(width = width).tag(DELIMITER_TAG))
}