package org.prime.easykarobar.ui.screen.startup

import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfFile
import kotlinx.cinterop.*
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun readDatabaseFile(filePath: String): ByteArray {
    val data = NSData.dataWithContentsOfFile(filePath)
        ?: throw Exception("Failed to read database file at: $filePath")

    val bytes = ByteArray(data.length.toInt())
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return bytes
}

actual fun getAppDatabaseDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val documentsDir = fileManager.URLsForDirectory(
        NSDocumentDirectory,
        NSUserDomainMask
    ).first() as NSURL

    return documentsDir.path ?: throw Exception("Cannot get documents directory")
}