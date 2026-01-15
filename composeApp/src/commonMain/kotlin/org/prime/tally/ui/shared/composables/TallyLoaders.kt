package org.prime.tally.ui.shared.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TallyCircularLoader() {
    CircularWavyProgressIndicator()
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

@Composable
fun ForceUpdateDialog() {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        dismissButton = {},
        icon = {
            Icon(
                imageVector = Icons.Rounded.SystemUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Update Required",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "You’re using an outdated version of the app. Please update to the latest version to continue.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 6.dp
    )
}



@Composable
fun TallyAlertBox(
    title: String,
    message: String = "",
    confirmButtonText: String = "Confirm",
    cancelButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit = onCancel,
    content: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                if (content != null) {
                    content()
                } else {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = confirmButtonText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = cancelButtonText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(24.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}