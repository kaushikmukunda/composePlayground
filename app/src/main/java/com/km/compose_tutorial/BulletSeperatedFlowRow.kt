package com.km.compose_tutorial

import androidx.compose.Composable
import androidx.ui.core.Constraints
import androidx.ui.core.Layout
import androidx.ui.core.Placeable
import androidx.ui.layout.Arrangement
import androidx.ui.unit.Dp
import androidx.ui.unit.IntPx
import androidx.ui.unit.dp
import androidx.ui.unit.max

/**
 * A composable that places its children in a horizontal flow. Unlike [Row], if the
 * horizontal space is too small to put all the children in one row, multiple rows may be used.
 *
 * Note that just like [Row], flex values cannot be used with [BulletSeperatedFlowRow].
 *
 * @param horizontalArrangement The alignment of each row's children in the main axis direction.
 * @param horizontalSpacing The horizontal spacing between the children of each row.
 * @param verticalSpacing The vertical spacing between the rows of the layout.
 */
@Composable
fun BulletSeperatedFlowRow(
    horizontalArrangement: Arrangement = Arrangement.Start,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    children: @Composable() () -> Unit
) {
    Layout(children) { measurables, outerConstraints, layoutDirection ->
        val sequences = mutableListOf<List<Placeable>>()
        val verticalAxisSizes = mutableListOf<IntPx>()
        val verticalPositions = mutableListOf<IntPx>()

        var totalWidth = IntPx.Zero
        var totalHeight = IntPx.Zero

        val currentSequence = mutableListOf<Placeable>()
        var currentWidth = IntPx.Zero
        var currentHeight = IntPx.Zero

        val childConstraints =
            Constraints(maxWidth = outerConstraints.maxWidth)

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentWidth + horizontalSpacing.toIntPx() +
                    placeable.width <= outerConstraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                totalHeight += verticalSpacing.toIntPx()
            }
            sequences += currentSequence.toList()
            verticalAxisSizes += currentHeight
            verticalPositions += totalHeight

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

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentWidth += horizontalSpacing.toIntPx()
            }
            currentSequence.add(placeable)
            currentWidth += placeable.width
            currentHeight = max(currentHeight, placeable.height)
        }

        // Add last line
        if (currentSequence.isNotEmpty()) startNewSequence()

        val layoutWidth = max(totalWidth, outerConstraints.minWidth)
        val layoutHeight = max(totalHeight, outerConstraints.minHeight)

        layout(layoutWidth, layoutHeight) {
            sequences.forEachIndexed { i, placeables ->
                val childrenWidths = placeables.mapIndexed { j, placeable ->
                    placeable.width +
                            if (j < placeables.lastIndex) horizontalSpacing.toIntPx() else IntPx.Zero
                }

                val horizontalPositions = horizontalArrangement.arrange(
                    layoutWidth,
                    childrenWidths,
                    layoutDirection
                )

                placeables.forEachIndexed { j, placeable ->
                    placeable.placeAbsolute(
                        x = horizontalPositions[j],
                        y = verticalPositions[i]
                    )

                }
            }
        }
    }
}
