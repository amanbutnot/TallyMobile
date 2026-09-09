package org.prime.easykarobar.data.expect

import android.util.Base64
import org.prime.easykarobar.AppContextHolder

actual fun getSplashImageBase64(): String {
    try {
        val context = AppContextHolder.appContext
        val assetManager = context.assets
        val paths = arrayOf(
            "composeResources/tallymobile.composeapp.generated.resources/drawable/splashImage.png",
            "drawable/splashImage.png"
        )
        for (path in paths) {
            try {
                val inputStream = assetManager.open(path)
                val bytes = inputStream.readBytes()
                inputStream.close()
                return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            } catch (ignored: Exception) {
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}
