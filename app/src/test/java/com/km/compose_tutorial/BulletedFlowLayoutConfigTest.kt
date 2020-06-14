package com.km.compose_tutorial

import androidx.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.IllegalArgumentException

@RunWith(JUnit4::class)
class DelimiterFlowLayoutConfigTest {

    @Test(expected = IllegalArgumentException::class)
    fun invalidConfig() {
        DelimiterFlowLayoutConfig(
            areBulletsShown = true,
            bulletRadius = 4.dp,
            horizontalSpacing = 6.dp
        )
    }

}