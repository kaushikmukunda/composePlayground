package com.km.composePlayground.components.button

import android.content.Context

class ColorUtility {
    fun getColorRes(current: Context, colorRes: Int): Int {
        return current.getColor(colorRes)
    }

}
