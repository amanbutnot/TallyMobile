package org.prime.easykarobar.ui.shared.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction

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
fun DownloadResultDialog(
    message: String,
    onDone: () -> Unit,
    isSuccess: Boolean,
    confirmText: String = if (isSuccess) "OK" else "Retry",
    fileName: String = "",
    htmlContent: String = "",
    onLoadingChange: (Boolean) -> Unit = {}
) {
    var visible by remember { mutableStateOf(true) }
    val backgroundColor = MaterialTheme.colorScheme.surface
    val primaryColor =
        if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.errorContainer
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error
    val title = if (isSuccess) "Success!" else "Error Occurred"
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = { onDone() }) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                initialScale = 0.8f
            ) + fadeIn(tween(400)),
            exit = scaleOut(
                animationSpec = tween(250, easing = FastOutLinearInEasing),
                targetScale = 0.9f
            ) + fadeOut(tween(250))
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp
                ),
                modifier = Modifier
                    .padding(20.dp)
                    .widthIn(min = 300.dp, max = 420.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 28.dp, end = 28.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with background circle
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                color = primaryColor.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (isSuccess) "Success" else "Error",
                            tint = primaryColor,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        letterSpacing = 0.15.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isSuccess) {
                        // Action buttons for success
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Download Button
                            Button(
                                onClick = {
                                    scope.launch {
                                        handlePdfAction(
                                            fileName = fileName,
                                            htmlContent = htmlContent,
                                            action = PdfAction.Download,
                                            onLoadingChange = onLoadingChange
                                        )
                                        visible = false
                                        onDone()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 3.dp,
                                    pressedElevation = 6.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Download PDF",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Share Button
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        handlePdfAction(
                                            fileName = fileName,
                                            htmlContent = htmlContent,
                                            action = PdfAction.Share,
                                            onLoadingChange = onLoadingChange
                                        )
                                        visible = false
                                        onDone()
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                border = BorderStroke(2.dp, primaryColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Share PDF",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Done Button
                            OutlinedButton(
                                onClick = {
                                    visible = false
                                    onDone()
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                border = BorderStroke(2.dp, primaryColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Done",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    } else {
                        // Retry button for failure
                        Button(
                            onClick = {
                                visible = false
                                onDone()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 6.dp
                            )
                        ) {
                            Text(
                                text = confirmText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
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
            if(cancelButtonText == ""){
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

@Composable
fun TallyFormatSelectionDialog(
    onDismiss: () -> Unit,
    onSelectStandard: () -> Unit,
    onPartyWiseSelect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select Format")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onSelectStandard()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Standard Format")
                }

                Button(
                    onClick = {
                        onPartyWiseSelect()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Party Wise")
                }
            }
        },
        confirmButton = {}, // intentionally empty
        dismissButton = {}
    )
}