package org.prime.easykarobar.ui.shared.reportsShared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class ReportColumn(
    val text: String,
    val weight: Float,
    val textAlign: TextAlign
)


@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    isHeader: Boolean = false, textColor: Color = MaterialTheme.colorScheme.onSurface
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
            textColor
        },
        maxLines = if (isHeader) 1 else 30,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TallyReportHeaderCard(
    columns: List<ReportColumn>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            columns.forEach { item ->
                TableCell(
                    text = item.text,
                    weight = item.weight,
                    textAlign = item.textAlign,
                    isHeader = true
                )
            }
        }
    }
}

@Composable
fun <T> TallyReportLazyList(
    items: List<T>,
    modifier: Modifier = Modifier,
    onItemClick: (T) -> Unit = {},
    content: @Composable RowScope.(T) -> Unit
) {
    val state = rememberLazyListState()
    LazyColumn(
        modifier = modifier.fillMaxSize(), state = state
    ) {
        items(items = items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 4.dp)
                    .clickable { onItemClick(item) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content(item)
                }
            }
        }
    }
}

@Composable
fun TallyReportBottomBar(
    columns: List<ReportColumn>,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { item ->
                Text(
                    text = item.text,
                    modifier = Modifier.weight(item.weight),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = item.textAlign,
                    color = contentColor
                )
            }
        }
    }
}
