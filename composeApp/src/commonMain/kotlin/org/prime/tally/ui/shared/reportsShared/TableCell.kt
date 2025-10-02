package org.prime.tally.ui.shared.reportsShared

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        textAlign = textAlign,
        style = if (isHeader) {
            MaterialTheme.typography.titleSmall
        } else {
            MaterialTheme.typography.bodyMedium
        },
        fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
        color = if (isHeader) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        maxLines = if (isHeader) 1 else 3,
        overflow = TextOverflow.Ellipsis
    )
}


