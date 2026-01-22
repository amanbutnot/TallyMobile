package org.prime.easykarobar.data.expect

import org.prime.easykarobar.AppContextHolder
import org.prime.easykarobar.data.utils.DB_FILE_NAME

actual fun readFileBytes(): ByteArray? {
    val context = AppContextHolder.appContext
    val file = context.getDatabasePath(DB_FILE_NAME)
    return if (file.exists()) file.readBytes() else null
}