package org.prime.tally

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.prime.tally.ui.screen.startup.SplashScreen
import org.prime.tally.ui.theme.TallyTheme

@Composable
@Preview
fun App() {
    TallyTheme {
        Navigator(SplashScreen) {
            FadeTransition(it)
        }
        //DriveUploadScreen()
    }

}