package org.prime.easykarobar.data.expect

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.prime.easykarobar.AppContextHolder

@SuppressLint("HardwareIds")
@Composable
actual fun getDeviceId(): String {
    val context = LocalContext.current
    return Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ANDROID_ID
    ) ?: ""
}

actual fun getDeviceIdString(): String {
    return try {
        val context = AppContextHolder.appContext
        Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ANDROID_ID
        ) ?: ""
    } catch (e: Exception) {
        ""
    }
}

actual fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER ?: ""
    val model = Build.MODEL ?: ""
    return if (model.startsWith(manufacturer, ignoreCase = true)) {
        model.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    } else {
        "${manufacturer.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} $model".trim()
    }
}