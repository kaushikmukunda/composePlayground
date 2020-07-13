package com.km.compose_tutorial.button

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.onPositioned
import androidx.ui.core.semantics.semantics
import androidx.ui.foundation.*
import androidx.ui.graphics.Color
import androidx.ui.graphics.Shape
import androidx.ui.layout.InnerPadding
import androidx.ui.layout.defaultMinSizeConstraints
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.contentColorFor
import androidx.ui.unit.Dp
import androidx.ui.unit.IntSize
import androidx.ui.unit.dp


@Composable
fun MeasurableButton(
    onClick: (layoutSize: IntSize) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: Dp = 2.dp,
    disabledElevation: Dp = 0.dp,
    shape: Shape = MaterialTheme.shapes.small,
    border: Border? = null,
    backgroundColor: Color = MaterialTheme.colors.primary,
    disabledBackgroundColor: Color = androidx.ui.material.Button.defaultDisabledBackgroundColor,
    contentColor: Color = contentColorFor(backgroundColor),
    disabledContentColor: Color = androidx.ui.material.Button.defaultDisabledContentColor,
    padding: InnerPadding = androidx.ui.material.Button.DefaultInnerPadding,
    text: @Composable() () -> Unit
) {
    Surface(
        shape = shape,
        color = if (enabled) backgroundColor else disabledBackgroundColor,
        contentColor = if (enabled) contentColor else disabledContentColor,
        border = border,
        elevation = if (enabled) elevation else disabledElevation,
        modifier = modifier
            // Since we're adding layouts in between the clickable layer and the content, we need to
            // merge all descendants, or we'll get multiple nodes
            .semantics(mergeAllDescendants = true)
    ) {
        var layoutSize = IntSize(0, 0)
        Box(
            Modifier.defaultMinSizeConstraints(
                minWidth = androidx.ui.material.Button.DefaultMinWidth,
                minHeight = androidx.ui.material.Button.DefaultMinHeight
            ).clickable(onClick = { onClick.invoke(layoutSize) }, enabled = enabled)
                .onPositioned {
                    layoutSize = it.size
                }
            ,
            paddingStart = padding.start,
            paddingTop = padding.top,
            paddingEnd = padding.end,
            paddingBottom = padding.bottom,
            gravity = ContentGravity.Center
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.button,
                children = text
            )
        }
    }
}
