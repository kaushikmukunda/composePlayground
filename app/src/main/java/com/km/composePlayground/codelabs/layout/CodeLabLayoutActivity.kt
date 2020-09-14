package com.km.composePlayground.codelabs.layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.FirstBaseline
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.AlignmentLine
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class CodeLabLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Column {
                    Text("Hello world", modifier = Modifier.baselineToTop(90.dp))
                    CustomColumn {
                        Text("Line 1")
                        Text("Line 2")
                    }
                }
            }
        }
    }

    @Composable
    private fun CustomColumn(
        modifier: Modifier = Modifier,
        children: @Composable() () -> Unit
    ) {
        Layout(children = children, modifier = modifier) { measurables, constraints ->
            val placeables = measurables.map { measurable ->
                // Measure each children
                measurable.measure(constraints)
            }

            var ypos = 0

            // Set the size of the layout as big as it can
            layout(constraints.maxWidth, constraints.maxHeight) {
                // Place children
                for (placeable in placeables) {
                    placeable.placeRelative(0, ypos)
                    ypos += placeable.height
                }
            }
        }

    }

    fun Modifier.baselineToTop(firstBaselineToTop: Dp) =
        this.layout { measurable, constraints ->

            val placeable = measurable.measure(constraints)

            // Check composable has baseline
            check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
            val firstBaseline = placeable[FirstBaseline]

            // Height of the composable with padding - first baseline
            val placeableY = firstBaselineToTop.toIntPx() + firstBaseline
            val height = placeable.height - placeableY

            layout(placeable.width, height) {
                placeable.placeRelative(0, placeableY)
            }
        }
}