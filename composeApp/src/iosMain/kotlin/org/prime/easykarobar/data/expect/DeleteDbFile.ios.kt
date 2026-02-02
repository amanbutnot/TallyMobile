package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import org.prime.easykarobar.data.utils.DB_FILE_NAME
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
actual fun deleteDbFile() {
    val fileManager = NSFileManager.defaultManager
    val dbName = DB_FILE_NAME

    // Delete from Documents directory
    val docsUrls = fileManager.URLsForDirectory(
        directory = NSDocumentDirectory,
        inDomains = NSUserDomainMask
    )
    val docsDir = docsUrls.firstOrNull() as? NSURL

    // Delete from Application Support (where SQLiter actually stores the db)
    val appSupportUrls = fileManager.URLsForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomains = NSUserDomainMask
    )
    val appSupportDir = appSupportUrls.firstOrNull() as? NSURL
    val sqliterDir = appSupportDir?.URLByAppendingPathComponent("org.prime.tally.TallyMobile")

    // All directories to check
    val directories = listOfNotNull(docsDir, sqliterDir)

    // All file variants to delete
    val fileVariants = listOf(
        dbName,
        "$dbName-wal",
        "$dbName-shm"
    )

    directories.forEach { dir ->
        fileVariants.forEach { variant ->
            val fileUrl = dir.URLByAppendingPathComponent(variant) ?: return@forEach
            val path = fileUrl.path ?: return@forEach

            if (fileManager.fileExistsAtPath(path)) {
                val success = fileManager.removeItemAtPath(path, error = null)
                if (success) {
                    println("🗑️ Deleted: $path")
                } else {
                    println("!!! Failed to delete: $path")
                }
            }
        }
    }
}