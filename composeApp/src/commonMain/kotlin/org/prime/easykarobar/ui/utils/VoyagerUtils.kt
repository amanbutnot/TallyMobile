package org.prime.easykarobar.ui.utils

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.startup.GoogleDriveDownloadScreen

fun Navigator.pushEasyMart(screen: Screen) {
    if (SharedPrefs.IsEasyMart.get() && shouldSyncEasyMart()) {
        val registeredNumber = SharedPrefs.RegisteredNumber.get() ?: ""
        this.push(GoogleDriveDownloadScreen(registeredNumber, targetScreen = screen))
    } else {
        this.push(screen)
    }
}

fun Navigator.replaceAllEasyMart(screen: Screen) {
    if (SharedPrefs.IsEasyMart.get() && shouldSyncEasyMart()) {
        val registeredNumber = SharedPrefs.RegisteredNumber.get() ?: ""
        this.replaceAll(GoogleDriveDownloadScreen(registeredNumber, targetScreen = screen))
    } else {
        this.replaceAll(screen)
    }
}
