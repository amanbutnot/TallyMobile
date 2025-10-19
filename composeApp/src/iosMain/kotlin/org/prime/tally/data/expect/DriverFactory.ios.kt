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
            val appSupportURL = fileManager.URLsForDirectory(NSApplicationSupportDirectory, NSUserDomainMask)
                .firstOrNull() as? NSURL ?: error("Cannot access Application Support")

            val dbFileURL = appSupportURL.URLByAppendingPathComponent(DB_FILE_NAME)!!
            val dbPath = dbFileURL.path!!

            // Delete old file if it exists
            if (fileManager.fileExistsAtPath(dbPath)) {
                fileManager.removeItemAtPath(dbPath, null)
            }

            // Write prepopulated database
            val nsData = bytes.toNSData()
            nsData.writeToFile(dbPath, atomically = true)

            println("Database file written to: $dbPath")
            println("File exists? ${fileManager.fileExistsAtPath(dbPath)}")

            return NativeSqliteDriver(
                schema = TallyDatabase.Schema,
                name = dbPath
            )
        }

}

// Helper: convert ByteArray -> NSData
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(
        bytes = this@toNSData.refTo(0).getPointer(this),
        length = size.toULong()
    )
}
