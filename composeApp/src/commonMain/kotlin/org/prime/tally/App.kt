package org.prime.tally

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.prime.tally.ui.screen.startup.SplashScreen
import org.prime.tally.ui.theme.TallyTheme
import org.tally.TallyDatabase

@Composable
@Preview
fun App() {
    TallyTheme {
        Navigator(SplashScreen)
        //DriveUploadScreen()
    }

}