package org.prime.easykarobar.data.expect

import android.util.Base64
import org.prime.easykarobar.AppContextHolder
import org.prime.easykarobar.ui.shared.globalShared.toValidUrl
import java.net.URL

actual fun getSplashImageBase64(): String {
    try {
        val db = DatabaseHolder.instance
        val config = db.companyConfigurationQueries.logoConfig().executeAsOneOrNull()
        val imgPath = config?.Imgpath?.trim()
        if (!imgPath.isNullOrBlank()) {
            if (imgPath.startsWith("data:image", true)) {
                return imgPath
            }
            val validUrl = imgPath.toValidUrl()
            try {
                val url = URL(validUrl)
                val bytes = url.openStream().use { it.readBytes() }
                if (bytes.isNotEmpty()) {
                    return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

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
