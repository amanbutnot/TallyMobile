package org.prime.easykarobar.data.expect

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@SuppressLint("HardwareIds")
@Composable
actual fun getDeviceId(): String {
    val context = LocalContext.current
    return Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ANDROID_ID
    ) ?: ""
}