package org.prime.easykarobar.ui.screen.startup

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.BuildKonfig
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.readFileBytes
import org.prime.easykarobar.data.utils.MOBILE_VERSION
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.auth.OnBoardingScreen
import org.prime.easykarobar.ui.screen.easymart.DmsScreen
import org.prime.easykarobar.ui.screen.easymart.EasyMartScreen
import org.prime.easykarobar.ui.screen.home.Dashboard
import org.prime.easykarobar.ui.screen.home.ROLE
import org.prime.easykarobar.ui.screen.home.userRole
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.splashImage

object SplashScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography

        LaunchedEffect(Unit) {
            println(">>> LaunchedEffect started")

            delay(2000)
            println(">>> Delay completed")

            val loginVersion = SharedPrefs.LoginVersion.get()
            println(">>> LoginVersion = $loginVersion")

            if (loginVersion == MOBILE_VERSION) {
                println(">>> LoginVersion matches MOBILE_VERSION")

                val fileBytes = readFileBytes()
                println(">>> readFileBytes() returned: ${fileBytes?.size ?: "null"}")

                if (fileBytes != null) {
                    println(">>> Initializing database with ${fileBytes.size} bytes")
                    DatabaseHolder.init(fileBytes)

                    println(">>> Navigating to Dashboard")
                    nav.replaceAll(Dashboard)
                } else {
                    println(">>> File bytes null, navigating to Login Screen")
                    if (BuildKonfig.STORE_ID.isNotEmpty()) {
                        nav.replaceAll(EasyMartScreen)
                    } else {
                        nav.replaceAll(OnBoardingScreen)
                    }
                }
            } else {
                println(">>> LoginVersion mismatch, navigating to Login Screen")
                if (BuildKonfig.STORE_ID.isNotEmpty()) {
                    nav.replaceAll(EasyMartScreen)
                } else {
                    nav.replaceAll(OnBoardingScreen)
                }
            }

            println(">>> LaunchedEffect finished")
        }

        Box(
            modifier = Modifier.fillMaxSize().background(colors.background).navigationBarsPadding()
        )
        {
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
                    topEnd = 20.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
     .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
       //     .background(Color(0xfff56013).copy(alpha = 1f))
    )
}
