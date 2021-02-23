package com.km.composePlayground.modifiers

import androidx.annotation.FloatRange
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.ShaderBrush
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
  // Pre-allocate gradient shaders based on input parameters to avoid creation during draw time.
  // Cannot use `drawWithCache` as that only regenerates based on size or snapshot changes which
  // doesn't work well with input color for the gradient.
  val gradients = remember(color, leftRatio, topRatio, rightRatio, bottomRatio) {
    createFadingEdgeBrushes(color, leftRatio, topRatio, rightRatio, bottomRatio)
  }

  drawWithContent {
    drawContent()

    gradients.fastForEach { gradient ->
      drawRect(
        brush = gradient,
        topLeft = gradient.calculateFromOffset(size),
        size = gradient.calculateGradientSize(size)
      )
    }
  }
}

private fun createFadingEdgeBrushes(
  color: Color,
  leftRatio: Float,
  topRatio: Float,
  rightRatio: Float,
  bottomRatio: Float
): List<FadingEdgeShaderBrush> {
  val startColorList = listOf(color, Color.Transparent)
  val endColorList = listOf(Color.Transparent, color)
  val result = mutableListOf<FadingEdgeShaderBrush>()

  if (leftRatio > 0.0f) {
    result += object : FadingEdgeShaderBrush(startColorList) {
      override fun calculateFromOffset(size: Size) = Offset.Zero

      override fun calculateToOffset(size: Size) = Offset(calculateGradientSize(size).width, 0f)

      override fun calculateGradientSize(size: Size) =
        Size(width = size.width * leftRatio, height = size.height)
    }
  }
  if (topRatio > 0.0f) {
    result += object : FadingEdgeShaderBrush(startColorList) {
      override fun calculateFromOffset(size: Size) = Offset.Zero

      override fun calculateToOffset(size: Size) = Offset(0f, calculateGradientSize(size).height)

      override fun calculateGradientSize(size: Size) =
        Size(width = size.width, height = size.height * topRatio)
    }
  }
  if (rightRatio > 0.0f) {
    result += object : FadingEdgeShaderBrush(endColorList) {
      override fun calculateFromOffset(size: Size): Offset {
        return Offset(x = size.width - calculateGradientSize(size).width, y = 0f)
      }

      override fun calculateToOffset(size: Size) = Offset(size.width, 0f)

      override fun calculateGradientSize(size: Size) =
        Size(width = size.width * rightRatio, height = size.height)
    }
  }
  if (bottomRatio > 0.0f) {
    result += object : FadingEdgeShaderBrush(endColorList) {
      override fun calculateFromOffset(size: Size): Offset {
        return Offset(x = 0f, y = size.height - calculateGradientSize(size).height)
      }

      override fun calculateToOffset(size: Size) = Offset(0f, size.height)

      override fun calculateGradientSize(size: Size) =
        Size(width = size.width, height = size.height * bottomRatio)
    }
  }

  return result
}

/**
 * Custom brush that uses a LinearGradientShader with provided `colors` based on from / to offsets
 * and gradient size.
 */
private abstract class FadingEdgeShaderBrush(
  private val colors: List<Color>,
) : ShaderBrush() {

  /** Returns the top left corner of the Rect for drawing the gradient. */
  abstract fun calculateFromOffset(size: Size): Offset

  /** Returns the "to" offset for creating LinearGradientShader. */
  abstract fun calculateToOffset(size: Size): Offset

  /** Returns size of the gradient Rect to draw. */
  abstract fun calculateGradientSize(size: Size): Size

  final override fun createShader(size: Size) = LinearGradientShader(
    colors = colors,
    from = calculateFromOffset(size),
    to = calculateToOffset(size),
  )
}
