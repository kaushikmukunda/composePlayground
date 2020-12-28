package com.km.composePlayground.modifiers

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.HorizontalGradient
import androidx.compose.ui.graphics.VerticalGradient
import androidx.compose.ui.util.fastForEach

/**
 * Modifier that draws a fading gradient from the outer edge of the composable with provided [color]
 * to [Color.Transparent] as it approaches the center.
 *
 * Screenshot: https://screenshot.googleplex.com/C9JgB6x735ARs5t
 *
 * @param color Color to start on the outer edge
 * @param leftRatio Starting from left, ratio of the composable width this gradient should cover
 * @param topRatio Starting from top, ratio of the composable width this gradient should cover
 * @param rightRatio Starting from right, ratio of the composable width this gradient should cover
 * @param bottomRatio Starting from bottom, ratio of the composable width this gradient should cover
 */
fun Modifier.fadingEdgeForeground(
  color: Color,
  @FloatRange(from = 0.0, to = 1.0) leftRatio: Float = 0f,
  @FloatRange(from = 0.0, to = 1.0) topRatio: Float = 0f,
  @FloatRange(from = 0.0, to = 1.0) rightRatio: Float = 0f,
  @FloatRange(from = 0.0, to = 1.0) bottomRatio: Float = 0f,
): Modifier = composed {
  var sizeState: Size by rememberState { Size.Zero }
  val gradientDatas = remember(color, sizeState, leftRatio, topRatio, rightRatio, bottomRatio) {
    calculateGradientDatas(color, sizeState, leftRatio, topRatio, rightRatio, bottomRatio)
  }

  drawWithContent {
    sizeState = size
    drawContent()

    gradientDatas.fastForEach { gradientData ->
      drawRect(brush = gradientData.brush, topLeft = gradientData.offset, size = gradientData.size)
    }
  }
}

@Immutable
private class GradientData(
  val offset: Offset,
  val size: Size,
  val brush: Brush,
)

private fun calculateGradientDatas(
  color: Color,
  layoutSize: Size,
  leftRatio: Float,
  topRatio: Float,
  rightRatio: Float,
  bottomRatio: Float,
): List<GradientData> {
  // Layout size not determined yet, don't draw any gradients.
  if (layoutSize == Size.Zero) {
    return emptyList()
  }

  val result = mutableListOf<GradientData>()
  val startColorList = listOf(color, Color.Transparent)
  val endColorList = listOf(Color.Transparent, color)

  if (leftRatio > 0.0f) {
    val leftGradientSize = Size(width = layoutSize.width * leftRatio, height = layoutSize.height)

    result += GradientData(
      size = leftGradientSize,
      offset = Offset.Zero,
      brush = HorizontalGradient(
        colors = startColorList,
        startX = 0f,
        endX = leftGradientSize.width
      )
    )
  }
  if (topRatio > 0.0f) {
    val topGradientSize = Size(width = layoutSize.width, height = layoutSize.height * topRatio)

    result += GradientData(
      size = topGradientSize,
      offset = Offset.Zero,
      brush = VerticalGradient(
        colors = startColorList,
        startY = 0f,
        endY = topGradientSize.height
      )
    )
  }

  if (rightRatio > 0.0f) {
    val rightGradientSize =
      Size(width = layoutSize.width * rightRatio, height = layoutSize.height)
    val rightGradientOffset = Offset(x = layoutSize.width - rightGradientSize.width, y = 0f)

    result += GradientData(
      size = rightGradientSize,
      offset = rightGradientOffset,
      brush = HorizontalGradient(
        colors = endColorList,
        startX = rightGradientOffset.x,
        endX = layoutSize.width
      )
    )
  }

  if (bottomRatio > 0.0f) {
    val bottomGradientSize =
      Size(width = layoutSize.width, height = layoutSize.height * bottomRatio)
    val bottomGradientOffset = Offset(x = 0f, y = layoutSize.height - bottomGradientSize.height)

    result += GradientData(
      size = bottomGradientSize,
      offset = bottomGradientOffset,
      brush = VerticalGradient(
        colors = endColorList,
        startY = bottomGradientOffset.y,
        endY = layoutSize.height
      )
    )
  }

  return result
}
