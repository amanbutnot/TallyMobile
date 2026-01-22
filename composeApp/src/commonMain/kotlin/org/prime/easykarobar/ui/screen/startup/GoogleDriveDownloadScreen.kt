package org.prime.easykarobar.ui.screen.startup

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.runBlocking
import org.prime.easykarobar.business.viewmodel.GoogleDriveViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.ZipExtractor
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.home.Dashboard
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object GoogleDriveDownloadScreen : Screen {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTime::class)
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography
        val nav = LocalNavigator.currentOrThrow
        val googleDriveViewModel: GoogleDriveViewModel = viewModel { GoogleDriveViewModel() }
        var showErrorDialog by remember { mutableStateOf(false) }
        val profileState by googleDriveViewModel.driveState


        if (showErrorDialog) {
            TallyResultDialog(
                message = "No ZIP file detected. Please upload the file again",
                onDone = {
                    showErrorDialog = false
                    nav.pop()
                },
                isSuccess = false,
                confirmText = "Try Again"
            )
        }
        LaunchedEffect(Unit) {
            println(">>> LaunchedEffect started")

            val fileId = SharedPrefs.FileId.get()
            println(">>> FileId from SharedPrefs = $fileId")

            if (fileId == null) {
                println("!!! FileId is NULL, aborting")
                return@LaunchedEffect
            }

            println(">>> Requesting Drive token...")

            googleDriveViewModel.getDriveToken { accessToken ->
                println(">>> Access token received: ${accessToken.take(15)}...")

                println(">>> Starting download for fileId: $fileId")

                googleDriveViewModel.downloadDriveFile(
                    fileId = fileId,
                    accessToken = accessToken
                ) { downloadedBytes ->

                    println(">>> Download completed")
                    println(">>> Downloaded bytes size = ${downloadedBytes.size}")

                    runBlocking {
                        println(">>> Entered runBlocking")

                        try {
                            println(">>> Creating ZipExtractor")
                            val extractor = ZipExtractor()

                            println(">>> Extracting zip bytes...")
                            val extractedBytes = extractor.extractSingle(downloadedBytes)
                            println(">>> Extraction successful, extracted size = ${extractedBytes.size}")

                            println(">>> Initializing database...")
                            DatabaseHolder.init(byteArray = extractedBytes)
                            println(">>> Database initialized")

                            val now = Clock.System.now().toEpochMilliseconds()
                            println(">>> Saving last sync time: $now")
                            SharedPrefs.LastSync.save(now)

                            println(">>> Navigating to Dashboard")
                            nav.replaceAll(Dashboard)

                        } catch (e: Exception) {
                            println("!!! ERROR OCCURRED")
                            e.printStackTrace()
                            showErrorDialog = true
                        }

                        println(">>> runBlocking finished")
                    }
                }
            }
        }



        Box(
            modifier = Modifier
                .fillMaxSize().navigationBarsPadding()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(colors.surface, colors.background)
                    )
                )
                .padding(24.dp),
        )
        {
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {
                LogoRound()

                Spacer(modifier = Modifier.height(48.dp))

                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = colors.primary,
                    trackColor = colors.primary.copy(alpha = 0.2f),
                    wavelength = 64.dp,
                    amplitude = 2f
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Downloading from Google Drive…",
                    style = type.titleMedium,
                    color = colors.onBackground,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = "Don’t close this page",
                modifier = Modifier.align(Alignment.BottomCenter),
                style = type.titleSmall,
                color = colors.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )


        }


    }
}

@Composable
private fun LogoRound() {
    val colors = MaterialTheme.colorScheme

    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(120.dp * pulse)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    radius = 220f
                ),
                shape = CircleShape
            )
            .border(
                width = 3.dp,
                color = colors.primary.copy(alpha = 0.7f),
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = "Cloud Download Icon",
            tint = colors.primary,
            modifier = Modifier.size(64.dp)
        )
    }
}
