package org.prime.easykarobar.ui.shared.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TallyDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp
) {
    val centerColor = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        centerColor.copy(alpha = 0f),
                        centerColor,
                        centerColor.copy(alpha = 0f)
                    ),
                )
            )
    )
}