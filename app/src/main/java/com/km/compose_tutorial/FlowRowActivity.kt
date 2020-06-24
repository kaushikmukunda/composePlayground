package com.km.compose_tutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.km.compose_tutorial.delimiterFlowRow.BulletDelimiter
import com.km.compose_tutorial.delimiterFlowRow.DelimiterFlowLayout
import com.km.compose_tutorial.delimiterFlowRow.SpaceDelimiter

class FlowRowActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ScreenContent()
            }
        }
    }
}

private val TEST_PHRASES = listOf<@Composable() () -> Unit>(
    { Text("Etiam sit amet") },
    { Text("ex id ipsum") },
    { Text("commodo dictum") },
    { Text("Ut id ex vehicula") },
    { Text("venenatis enim") },
    { Text("feugiat, porta neque") },
    { Text("In venenatis") },
    { Text("neque ac quam aliquam") },
    { Text("tempus vitae sit amet lorem") },
    { Text("Vestibulum accumsan") },
    { Text("nisl eget neque") },
    { Text("aliquam ultricies") },
    { Text("Nullam non leo") },
    { Text("ullamcorper, ornare") },
    { Text("felis nec") },
    { Text("euismod magna") }
)

@Composable
fun ScreenContent() {
    Column(modifier = Modifier.padding(start = 0.dp, end = 0.dp)) {
        DelimiterFlowLayout(
            numLines = 1,
            modifier = Modifier.ltr,
            delimiter = {
                SpaceDelimiter(
                    16.dp
                )
            },
            children = TEST_PHRASES
        )

        DelimiterFlowLayout(
            numLines = 4,
            delimiter = { modifier ->
                BulletDelimiter(
                    bulletRadius = 2.dp,
                    bulletGap = 8.dp,
                    modifier = modifier
                )
            },
            children = TEST_PHRASES
        )

        DelimiterFlowLayout(
            numLines = 4,
            delimiter = { modifier ->
                TallDelimiter(modifier = modifier)
            },
            children = TEST_PHRASES
        )
    }
}


@Composable
fun TallDelimiter(modifier: Modifier) {
    Text("|", fontSize = 30.sp, modifier = modifier)
}

