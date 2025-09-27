package org.prime.tally.ui.shared

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TallyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor, contentColor = contentColor
        )
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            style = MaterialTheme.typography.displaySmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1

        )
    }
}


@Composable
fun TallyIconButton(title: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.errorContainer.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.error.copy(alpha = 0.3f)
        )
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = colors.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = colors.error
            )
        }
    }
}