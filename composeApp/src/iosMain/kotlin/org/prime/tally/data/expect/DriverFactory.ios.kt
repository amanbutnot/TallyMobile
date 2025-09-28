package org.prime.tally.data.expect

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.prime.tally.data.utils.DB_FILE_NAME
import org.tally.TallyDatabase
import platform.Foundation.*
import kotlinx.cinterop.*

actual class DriverFactory {
    @OptIn(ExperimentalForeignApi::class)
    actual fun createDriver(bytes: ByteArray): SqlDriver {
        val fileManager = NSFileManager.defaultManager

        // Get path to Documents directory
        val urls = fileManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        )
        val documentsDirectory = urls.firstOrNull() as? NSURL
            ?: error("Unable to access Documents directory")

        // Full path to database file
        val dbUrl = documentsDirectory.URLByAppendingPathComponent(DB_FILE_NAME)
            ?: error("Unable to create DB URL")

        val dbPath = dbUrl.path ?: error("Unable to resolve DB path")

        // Remove old DB if exists
        if (fileManager.fileExistsAtPath(dbPath)) {
            fileManager.removeItemAtPath(dbPath, null)
        }

        // Write new DB file
        val nsData = bytes.toNSData()
        nsData.writeToFile(dbPath, atomically = true)

        // Create SQLDelight driver with the file path
        return NativeSqliteDriver(
            schema = TallyDatabase.Schema,
            name = dbPath
        )
    }
}

// Helper: convert ByteArray -> NSData safely
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(
        bytes = this@toNSData.refTo(0).getPointer(this),
        length = size.toULong()
    )
}
