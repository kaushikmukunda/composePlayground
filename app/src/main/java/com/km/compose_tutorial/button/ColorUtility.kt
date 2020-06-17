package com.km.compose_tutorial.button

import android.content.Context

class ColorUtility {
    fun getColorRes(current: Context, colorRes: Int): Int {
        return current.getColor(colorRes)
    }

}
