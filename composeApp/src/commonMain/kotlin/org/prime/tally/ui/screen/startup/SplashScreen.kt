package org.prime.tally.ui.screen.startup

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.readFileBytes
import org.prime.tally.data.utils.MOBILE_VERSION
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.screen.auth.LoginScreen
import org.prime.tally.ui.screen.auth.OnBoardingScreen
import org.prime.tally.ui.screen.auth.SignUpScreen
import org.prime.tally.ui.screen.home.Dashboard
import org.prime.tally.ui.shared.composables.ForceUpdateDialog
import org.prime.tally.ui.shared.composables.TallyResultDialog
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.splashImage

object SplashScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography
        var showErrorPopup by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(2000)
            if (SharedPrefs.LoginVersion.get() == null || SharedPrefs.LoginVersion.get() == MOBILE_VERSION) {
                SharedPrefs.LoginVersion.save(MOBILE_VERSION)
                val fileBytes = readFileBytes()
                if (fileBytes != null) {
                    DatabaseHolder.init(fileBytes)
                    nav.replaceAll(Dashboard)
                } else {
                    nav.replaceAll(OnBoardingScreen)
                }
            } else {
                showErrorPopup = true
            }
        }
        if (showErrorPopup) {
            ForceUpdateDialog()
        }
        Box(
            modifier = Modifier.fillMaxSize().background(colors.background).navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
//                Text(
//                    "Easy Karobar",
//                    textAlign = TextAlign.Center,
//                    color = colors.primary,
//                    style = type.headlineSmall.copy(fontSize = 50.sp)
//                )
                Image(
                    painterResource(Res.drawable.splashImage),
                    contentDescription = "",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth().height(300.dp).align(
                    Alignment.BottomCenter
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(100, 150, 200, 250, 300).forEach { height ->
                    BarChartLine(height.dp, modifier = Modifier.weight(1f))
                }

            }
        }
    }
}


@Composable
private fun BarChartLine(height: Dp, modifier: Modifier = Modifier) {

    var visible by remember { mutableStateOf(false) }

    val animatedHeight by animateDpAsState(
        targetValue = if (visible) height else 0.dp,
        animationSpec = tween(1800)
    )

    LaunchedEffect(Unit) {
        visible = true
    }


    Box(
        modifier = modifier
            .height(animatedHeight).clip(
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 0.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
//            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .background(Color(0xfff56013).copy(alpha = 1f))
    )
}
