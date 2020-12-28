package com.km.composePlayground.components.buttongroup

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize

fun Modifier.onButtonSizeMeasured(onMeasured: (String, IntSize) -> Unit) =
    this.then(object : OnButtonSizeMeasuredModifier {
        override fun onButtonSizeMeasured(id: String, size: IntSize) {
            onMeasured(id, size)
        }
    })

interface OnButtonSizeMeasuredModifier : Modifier.Element {
    fun onButtonSizeMeasured(id: String, size: IntSize)
}

