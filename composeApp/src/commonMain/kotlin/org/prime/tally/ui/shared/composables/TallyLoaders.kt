package org.prime.tally.ui.shared.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun TallyLoadingDialog(
    text: String
) {
    Dialog(onDismissRequest = { }) {
        Box(
            modifier = Modifier.size(180.dp).clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TallyCircularLoader()

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = text, style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun TallyCircularLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "Loading Bar")
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000)
        ), label = "progress animation"
    )

    Box(modifier = Modifier.size(60.dp).graphicsLayer {
        scaleX = progress.value
        scaleY = progress.value
        alpha = 1 - progress.value
    }.border(5.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape))

}

@Composable
fun TallyResultDialog(
    message: String,
    onDone: () -> Unit,
    isSuccess: Boolean,
    confirmText: String = if (isSuccess) "OK" else "Retry"
) {
    var visible by remember { mutableStateOf(true) }

    val backgroundColor = MaterialTheme.colorScheme.surface
    val icon = if (isSuccess) Icons.Default.Check else Icons.Default.Close
    val iconTint = if (isSuccess) Color(0xFF81C784) else MaterialTheme.colorScheme.error
    val buttonColor = if (isSuccess) Color(0xFF81C784) else MaterialTheme.colorScheme.error
    val buttonTextColor =
        if (isSuccess) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onError
    val title = if (isSuccess) "Success" else "Oops!"

    Dialog(onDismissRequest = { onDone() }) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(tween(300)) + fadeIn(tween(300)),
            exit = scaleOut(tween(200)) + fadeOut(tween(200))
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 10.dp,
                color = backgroundColor,
                modifier = Modifier.padding(20.dp).widthIn(min = 280.dp, max = 400.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isSuccess) "Success" else "Error",
                        tint = iconTint,
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = message, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            visible = false
                            onDone()
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor, contentColor = buttonTextColor
                        ), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

