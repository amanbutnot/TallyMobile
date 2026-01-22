package org.prime.easykarobar

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.prime.easykarobar.ui.screen.startup.SplashScreen
import org.prime.easykarobar.ui.theme.TallyTheme

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