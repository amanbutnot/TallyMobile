package org.prime.tally.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import org.prime.tally.data.utils.DB_FILE_NAME
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
actual fun deleteDbFile() {
    val fileManager = NSFileManager.defaultManager
    val dbName = DB_FILE_NAME

    val urls = fileManager.URLsForDirectory(
        directory = NSDocumentDirectory,
        inDomains = NSUserDomainMask
    )
    val documentsDirectory = urls.firstOrNull() as? NSURL ?: return
    val dbUrl = documentsDirectory.URLByAppendingPathComponent(dbName)

    if (dbUrl != null && fileManager.fileExistsAtPath(dbUrl.path!!)) {
        val success = fileManager.removeItemAtURL(dbUrl, error = null)
        if (!success) {
            println("Failed to delete db file at: ${dbUrl.path}")
        }
    }
}
