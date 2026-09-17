package org.prime.easykarobar.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.startup.GoogleDriveDownloadScreen
import kotlin.time.Clock

fun shouldSyncEasyMart(): Boolean {
    if (!SharedPrefs.IsEasyMart.get()) return false
    val lastCheckTime = SharedPrefs.IsEasyMart.getLastCheckTime()
    if (lastCheckTime == 0L) return false // Let it be saved once first or trigger immediately? 
    // Actually, if it's 0, it means it was never saved. 
    // The user said "take a time of first splashscreen or masteradd screen"
    val currentTime = Clock.System.now().toEpochMilliseconds()
    return (currentTime - lastCheckTime) > 300000L // 5 minutes in ms
}

fun triggerEasyMartSync(nav: Navigator) {
    val registeredNumber = SharedPrefs.RegisteredNumber.get() ?: ""
    nav.push(GoogleDriveDownloadScreen(registeredNumber))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyMartRefreshableBox(
    nav: Navigator,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (SharedPrefs.IsEasyMart.get()) {
        val scope = rememberCoroutineScope()
        val state = rememberPullToRefreshState()
        
        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = {
                scope.launch {
                    triggerEasyMartSync(nav)
                }
            },
            state = state,
            modifier = modifier
        ) {
            content()
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}
