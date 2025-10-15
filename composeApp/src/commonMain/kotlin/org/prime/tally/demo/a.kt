package org.prime.tally.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun canvas() {

    val scope = rememberCoroutineScope()

    val a = listOf(
        1,
        23,
        43,
        54,
        534,
        56,
        56,
        456,
        456,
        45,
        6,
        46,
        345,
        23,
        4,
        2,
        34,
        12,
        3,
        1,
        31,
        4,
        2,
        35,
        3,
        45
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            scope.launch {
            }
        }) {
            Text("Create Pdf")
        }
    }

}


