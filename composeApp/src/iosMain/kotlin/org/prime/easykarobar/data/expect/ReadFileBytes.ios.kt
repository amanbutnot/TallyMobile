package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import org.prime.easykarobar.data.utils.DB_FILE_NAME
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
actual fun readFileBytes(): ByteArray? {
    val fm = NSFileManager.defaultManager

    val appSupport = NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() ?: return null

    val bundleId = NSBundle.mainBundle.bundleIdentifier ?: return null
    val dbDir = "$appSupport/$bundleId"
    val dbPath = "$dbDir/$DB_FILE_NAME"

    println("🔍 Reading DB from: $dbPath")

    if (!fm.fileExistsAtPath(dbPath)) {
        println("❌ DB file does NOT exist")
        return null
    }

    val data = NSData.dataWithContentsOfFile(dbPath) ?: return null
    val length = data.length.toInt()
    val pointer = data.bytes ?: return null

    println("✔️ DB file found, size = $length bytes")

    return ByteArray(length).also { bytes ->
        bytes.usePinned {
            platform.posix.memcpy(it.addressOf(0), pointer, length.convert())
        }
    }
}
