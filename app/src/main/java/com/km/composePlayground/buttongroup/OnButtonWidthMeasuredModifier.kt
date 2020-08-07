package com.km.composePlayground.buttongroup

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize

fun Modifier.onButtonSizeMeasured(onMeasured: (String, IntSize) -> Unit) =
    this + object : OnButtonSizeMeasuredModifier {
        override fun onButtonSizeMeasured(id: String, size: IntSize) {
            onMeasured(id, size)
        }
    }

interface OnButtonSizeMeasuredModifier : Modifier.Element {
    fun onButtonSizeMeasured(id: String, size: IntSize)
}

