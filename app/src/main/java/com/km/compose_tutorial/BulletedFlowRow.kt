package com.km.compose_tutorial

import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.core.Constraints
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Layout
import androidx.ui.core.Modifier
import androidx.ui.core.Placeable
import androidx.ui.core.drawBehind
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.layout.Arrangement
import androidx.ui.unit.Dp
import androidx.ui.unit.IntPx
import androidx.ui.unit.dp
import androidx.ui.unit.max
import androidx.ui.util.fastForEachIndexed

/**
 * Configuration for bullet separated flow layout.
 *
 * @property areBulletsShown True to add bullets between child views
 * @property horizontalArrangement Optional specifies the arrangement of the child views.
 *      ex: START to place the views as close as possible to the beginning of the row.
 * @property horizontalSpacing Optional spacing between the children in a row.
 * @property numLines Optional total number of lines to display.
 * @property bulletRadius Optional the size of the bullet.
 * @property bulletColor Optional color for the bullet. Defaults to current theme text primary.
 *    The color is resolved within the composable scope of the Layout.
 */
class BulletedFlowLayoutConfig(
  val areBulletsShown: Boolean,
  val horizontalArrangement: Arrangement = Arrangement.Start,
  val horizontalSpacing: Dp = 0.dp,
  val numLines: Int = 1,
  val bulletRadius: Dp = 0.dp,
  val bulletColor: Color? = null
) {
    init {
        require(horizontalSpacing > bulletRadius * 2) { "BulletRadius too large!" }
    }
}

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
fun BulletedFlowLayout(
  config: BulletedFlowLayoutConfig,
  modifier: Modifier = Modifier,
  children: @Composable() () -> Unit
) {
    val bulletColor = config.bulletColor ?: Color.Black
    val bulletPositions = remember { mutableListOf<Offset>() }
    val bulletModifier: Modifier = Modifier.drawBehind {
        for (bulletPosition in bulletPositions) {
            drawCircle(
              center = bulletPosition,
              radius = config.bulletRadius.toPx().value,
              color = bulletColor
            )
        }
    }

    // The layout logic is derived from Jetpack Compose Flow layout : http://shortn/_o7nTAEQgPM
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

        bulletPositions.clear()

        val childConstraints =
          Constraints(maxWidth = outerConstraints.maxWidth)
        val horizontalSpacingIntPx = config.horizontalSpacing.toIntPx()

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
          currentSequence.isEmpty() || (
            currentWidth + placeable.width +
              horizontalSpacingIntPx
            ) <= outerConstraints.maxWidth

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

            // Stop placing children if it already filled configured number of lines
            if (sequences.size >= config.numLines) {
                break
            }

            // Add spacing to the width if this child is not the first element in the row
            if (currentSequence.isNotEmpty()) {
                currentWidth += horizontalSpacingIntPx
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
        // Note: This is based of java/com/google/supplychain/scales/store/access/FlowRow.java
        layout(layoutWidth, layoutHeight) {
            sequences.fastForEachIndexed { i, placeables ->
                val childrenWidths = placeables.mapIndexed { j, placeable ->
                    val additionalPadding =
                      if (j < placeables.lastIndex) horizontalSpacingIntPx else IntPx.Zero
                    placeable.width + additionalPadding
                }

                val horizontalPositions = config.horizontalArrangement.arrange(
                  layoutWidth,
                  childrenWidths,
                  layoutDirection
                )

                placeables.fastForEachIndexed { j, placeable ->
                    placeable.placeAbsolute(
                      x = horizontalPositions[j],
                      y = rowVerticalPositions[i]
                    )

                    // Prepend bullet if not the first element in the row
                    if (j > 0 && config.areBulletsShown) {
                        val dx =
                          (horizontalPositions[j] - horizontalSpacingIntPx / 2).value
                        val dy = (rowVerticalPositions[i] + placeable.height / 2).value
                        bulletPositions.add(Offset(dx.toFloat(), dy.toFloat()))
                    }
                }
            }
        }
    }
}
