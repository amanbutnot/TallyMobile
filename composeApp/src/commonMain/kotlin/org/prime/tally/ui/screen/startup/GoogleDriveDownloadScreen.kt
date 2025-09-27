package org.prime.tally.ui.screen.startup

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
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.initializeDatabase
import org.prime.tally.ui.screen.home.Dashboard
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.business.viewmodel.GoogleDriveViewModel

object GoogleDriveDownloadScreen : Screen {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography
        val nav = LocalNavigator.currentOrThrow
        val googleDriveViewModel: GoogleDriveViewModel = viewModel { GoogleDriveViewModel() }
        val profileState by googleDriveViewModel.driveState


//        LaunchedEffect(Unit) {
//            googleDriveViewModel.getDriveToken() {
//
//            }
//        }
        LaunchedEffect(Unit) {
            SharedPrefs.FileId.get()?.let {fileId->
                googleDriveViewModel.getDriveToken {
                    googleDriveViewModel.downloadDriveFile(
                        fileId =fileId,
                        accessToken = it
                    ) {
                        DatabaseHolder.init(byteArray = it)
                       initializeDatabase(it)
                        nav.replaceAll(Dashboard)
                    }
                }
            }
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
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
