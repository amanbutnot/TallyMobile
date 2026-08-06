package org.prime.easykarobar.ui.screen.startup

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.easykarobar.BuildKonfig
import org.prime.easykarobar.business.viewmodel.GDownloadViewModel
import org.prime.easykarobar.business.viewmodel.GoogleDriveViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.deleteDbFile
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class GoogleDriveDownloadScreen(val number: String) : Screen {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTime::class)
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography
        val nav = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        val googleDriveViewModel: GoogleDriveViewModel = viewModel { GoogleDriveViewModel() }
        val downloadViewModel: GDownloadViewModel = viewModel { GDownloadViewModel() }

        var showErrorDialog by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var isInitializing by remember { mutableStateOf(false) }

        val driveState by downloadViewModel.driveState.collectAsState()
        val downloadState by downloadViewModel.downloadState.collectAsState()

        val topColor = try { Color(BuildKonfig.SPLASH_TOP_COLOR.removePrefix("#").toLong(16) or 0xFF000000) } catch (e: Exception) { colors.primary }
        val bottomColor = try { Color(BuildKonfig.SPLASH_BOTTOM_COLOR.removePrefix("#").toLong(16) or 0xFF000000) } catch (e: Exception) { colors.primaryContainer }

        // Handle download errors
        LaunchedEffect(downloadState.error) {
            downloadState.error?.let { error ->
                errorMessage = error
                showErrorDialog = true
            }
        }

        if (showErrorDialog) {
            TallyResultDialog(
                message = errorMessage.ifEmpty { "Failed to download database. Please try again." },
                onDone = {
                    showErrorDialog = false
                    nav.pop()
                },
                isSuccess = false,
                confirmText = "Try Again"
            )
        }

        LaunchedEffect(Unit) {
            val fileId = SharedPrefs.FileId.get()
            val storeId = BuildKonfig.STORE_ID

            deleteDbFile()
            val destinationPath = getAppDatabaseDirectory()

            val onDownloadSuccess: (String) -> Unit = { dbPath ->
                isInitializing = true
                scope.launch(Dispatchers.IO) {
                    try {
                        val dbBytes = readDatabaseFile(dbPath)
                        DatabaseHolder.init(byteArray = dbBytes)
                        val now = Clock.System.now().toEpochMilliseconds()
                        SharedPrefs.LastSync.save(now)
                        withContext(Dispatchers.Main) {
                            nav.replaceAll(MasterAddScreen(number))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Failed to initialize database: ${e.message}"
                        withContext(Dispatchers.Main) {
                            showErrorDialog = true
                            isInitializing = false
                        }
                    }
                }
            }

            if (storeId.isNotEmpty()) {
                val url = "https://easykarobar.in/database/$storeId.zip"
                downloadViewModel.downloadDatabaseFromUrl(
                    url = url,
                    destinationPath = destinationPath,
                    onSuccess = onDownloadSuccess
                )
            } else {
                if (fileId == null) {
                    errorMessage = "File ID not found. Please try again."
                    showErrorDialog = true
                    return@LaunchedEffect
                }

                googleDriveViewModel.getDriveToken { accessToken ->
                    downloadViewModel.downloadAndExtractDatabase(
                        fileId = fileId,
                        accessToken = accessToken,
                        destinationPath = destinationPath,
                        onSuccess = onDownloadSuccess
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(topColor.copy(alpha = 0.1f), colors.background)))
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RocketAnimation(topColor)

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = if (SharedPrefs.IsEasyMart.get()) "Setting up your store..." else "Downloading Data...",
                    style = type.headlineSmall.copy(fontWeight = FontWeight.Bold, color = colors.onBackground),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "We're getting everything ready for you. This might take a moment.",
                    style = type.bodyMedium.copy(color = colors.onBackground.copy(alpha = 0.6f)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = topColor,
                    trackColor = topColor.copy(alpha = 0.2f),
                    wavelength = 40.dp,
                    amplitude = 2f
                )
            }

            Text(
                text = "Please don't close the app",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                style = type.labelMedium.copy(
                    color = colors.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RocketAnimation(mainColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "rocket")
    
    val translateY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translate"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(140.dp)
            .graphicsLayer {
                translationY = translateY
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = mainColor.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(2.dp, mainColor.copy(alpha = 0.3f))
        ) {}
        
        Icon(
            imageVector = Icons.Default.RocketLaunch,
            contentDescription = null,
            tint = mainColor,
            modifier = Modifier.size(70.dp)
        )
    }
}

// Platform-specific function to read database file
expect fun readDatabaseFile(filePath: String): ByteArray

// Platform-specific function to get app database directory
expect fun getAppDatabaseDirectory(): String
