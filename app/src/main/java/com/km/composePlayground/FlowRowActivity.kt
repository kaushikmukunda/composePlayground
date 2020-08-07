package com.km.composePlayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.km.composePlayground.delimiterFlowRow.BulletDelimiter
import com.km.composePlayground.delimiterFlowRow.DelimiterFlowLayout
import com.km.composePlayground.delimiterFlowRow.SpaceDelimiter

class FlowRowActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ScreenContentWithConstraints()
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
fun ScreenContentWithConstraints() {
    Column(modifier = Modifier.padding(start = 0.dp, end = 0.dp)) {
        DelimiterFlowLayout(
            numLines = 1,
//            modifier = Modifier.rtl,
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

