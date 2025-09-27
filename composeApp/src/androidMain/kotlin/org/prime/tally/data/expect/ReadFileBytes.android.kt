package org.prime.tally.data.expect

import org.prime.tally.AppContextHolder
import org.prime.tally.data.utils.DB_FILE_NAME

actual fun readFileBytes(): ByteArray? {
    val context = AppContextHolder.appContext
    val file = context.getDatabasePath(DB_FILE_NAME)
    return if (file.exists()) file.readBytes() else null
}