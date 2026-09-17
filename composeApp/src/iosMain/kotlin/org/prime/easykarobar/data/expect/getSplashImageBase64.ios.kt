package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import org.prime.easykarobar.ui.shared.globalShared.toValidUrl
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
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
                val nsUrl = NSURL.URLWithString(validUrl)
                if (nsUrl != null) {
                    val data = NSData.dataWithContentsOfURL(nsUrl)
                    if (data != null && data.length > 0u) {
                        val length = data.length.toInt()
                        val pointer = data.bytes
                        if (pointer != null) {
                            val bytes = ByteArray(length).also { b ->
                                b.usePinned {
                                    memcpy(it.addressOf(0), pointer, length.convert())
                                }
                            }
                            return "data:image/png;base64," + Base64.encode(bytes)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}
