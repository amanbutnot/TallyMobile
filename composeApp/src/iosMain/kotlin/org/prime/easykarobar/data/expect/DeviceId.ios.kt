package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice

@Composable
actual fun getDeviceId(): String {
    return UIDevice.currentDevice.identifierForVendor?.UUIDString ?: ""
}
