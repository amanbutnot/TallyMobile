package org.prime.tally.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import org.prime.tally.data.utils.DB_FILE_NAME
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
actual fun readFileBytes(): ByteArray? {
    val fileManager = NSFileManager.defaultManager

    // Path to Documents directory
    val urls = fileManager.URLsForDirectory(
        directory = NSDocumentDirectory,
        inDomains = NSUserDomainMask
    )
    val documentsDirectory = urls.firstOrNull() as? NSURL ?: return null
    val dbUrl = documentsDirectory.URLByAppendingPathComponent(DB_FILE_NAME) ?: return null
    val dbPath = dbUrl.path ?: return null

    if (!fileManager.fileExistsAtPath(dbPath)) {
        return null
    }

    val data = NSData.dataWithContentsOfFile(dbPath) ?: return null
    val length = data.length.toInt()

    // Convert NSData -> ByteArray
    val bytes = ByteArray(length)
    data.bytes?.let { pointer ->
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), pointer, length.convert())
        }
    }

    return bytes
}
