package com.km.compose_tutorial

import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.core.*
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Paint
import androidx.ui.layout.Arrangement
import androidx.ui.unit.Dp
import androidx.ui.unit.IntPx
import androidx.ui.unit.dp
import androidx.ui.unit.max

/**
 * Configuration for bullet separated flow row.
 *
 * @property areBulletsShown True to add bullets between child views
 * @property horizontalArrangement Optional specifies the arrangement of the child views.
 *      ex: START to place the views as close as possible to the beginning of the row.
 * @property horizontalSpacing Optional spacing between the children in a row.
 * @property numLines Optional total number of lines to display.
 * @property bulletRadius Optional the size of the bullet.
 * @property bulletPaint Optional Paint object to be used for drawing the bullet.
 */
class BulletedFlowRowConfig(
    val areBulletsShown: Boolean,
    val horizontalArrangement: Arrangement = Arrangement.Start,
    val horizontalSpacing: Dp = 0.dp,
    val numLines: Int = 1,
    val bulletRadius: Float = 0f,
    val bulletPaint: Paint = Paint().apply { isAntiAlias = true }
) {
    val horizontalSpacingIntPx by lazy {
        IntPx(horizontalSpacing.value.toInt())
    }
}

typealias BulletPosition = Offset

/**
 * Arranges children in left-to-right flow, packing as many child views as possible on
 * each line. When the current line does not have enough horizontal space, the layout continues on
 * the next line. Can be configured to separate children with a circular bullet.
 *
 * In the 'Layout' phase, the children are measured and placed on the required number of lines.
 * At this time, the positions of the Bullet separators are computed and stored.
 * The drawBehind modifier is invoked during the 'Draw' phase and it draws the bullets at the
 * pre-computed positions.
 */
@Composable
fun BulletedFlowRow(
    config: BulletedFlowRowConfig,
    modifier: Modifier = Modifier,
    children: @Composable() () -> Unit
) {
    val bulletPositions = remember { mutableListOf<BulletPosition>() }
    val bulletModifier: Modifier = Modifier.drawBehind {
        for (bulletPosition in bulletPositions) {
            drawCircle(bulletPosition, config.bulletRadius, config.bulletPaint)
        }
    }

    Layout(
        children,
        modifier = modifier + bulletModifier
    ) { measurables, outerConstraints, layoutDirection ->
        val sequences = mutableListOf<List<Placeable>>()
        val rowHeights = mutableListOf<IntPx>()
        val rowVerticalPositions = mutableListOf<IntPx>()

        var totalWidth = IntPx.Zero
        var totalHeight = IntPx.Zero

        val currentSequence = mutableListOf<Placeable>()
        var currentWidth = IntPx.Zero
        var currentHeight = IntPx.Zero

        val childConstraints =
            Constraints(maxWidth = outerConstraints.maxWidth)

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || (currentWidth + placeable.width +
                    config.horizontalSpacingIntPx) <= outerConstraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
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

            // Stop placing children if there a configured number of lines
            if (sequences.size >= config.numLines) {
                break
            }

            // Add spacing to the width if this child is not the first element in the row
            if (currentSequence.isNotEmpty()) {
                currentWidth += config.horizontalSpacingIntPx
            }

            // Add the child to the current sequence.
            currentSequence.add(placeable)
            currentWidth += placeable.width
            currentHeight = max(currentHeight, placeable.height)
        }

        // Add last line
        if (currentSequence.isNotEmpty()) startNewSequence()

        val layoutWidth = max(totalWidth, outerConstraints.minWidth)
        val layoutHeight = max(totalHeight, outerConstraints.minHeight)

        // Layout all the children
        layout(layoutWidth, layoutHeight) {
            sequences.forEachIndexed { i, placeables ->
                val childrenWidths = placeables.mapIndexed { j, placeable ->
                    val additionalPadding =
                        if (j < placeables.lastIndex) config.horizontalSpacingIntPx else IntPx.Zero
                    placeable.width + additionalPadding
                }

                val horizontalPositions = config.horizontalArrangement.arrange(
                    layoutWidth,
                    childrenWidths,
                    layoutDirection
                )

                placeables.forEachIndexed { j, placeable ->
                    placeable.placeAbsolute(
                        x = horizontalPositions[j],
                        y = rowVerticalPositions[i]
                    )

                    // Prepend bullet if not the first element in the row
                    if (j > 0 && config.areBulletsShown) {
                        val dx =
                            (horizontalPositions[j] - config.horizontalSpacingIntPx / 2).value
                        val dy = (rowVerticalPositions[i] + placeable.height / 2).value
                        bulletPositions.add(BulletPosition(dx.toFloat(), dy.toFloat()))
                    }
                }
            }
        }
    }
}