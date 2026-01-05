package org.prime.tally.data.expect

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import org.prime.tally.data.utils.DB_FILE_NAME
import org.tally.TallyDatabase
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

actual class DriverFactory {

    @OptIn(ExperimentalForeignApi::class)
    actual fun createDriver(bytes: ByteArray): SqlDriver {

        println("🔍 createDriver() called")

        val appSupport = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory,
            NSUserDomainMask,
            true
        ).first() as String

        val bundleId = NSBundle.mainBundle.bundleIdentifier ?: "default"
        val sqliterDir = "$appSupport/$bundleId"
        val sqliterDbPath = "$sqliterDir/$DB_FILE_NAME"

        val fm = NSFileManager.defaultManager

        println("📁 App Support: $appSupport")
        println("📁 SQLiter Dir: $sqliterDir")
        println("📄 Target DB Path: $sqliterDbPath")

        if (!fm.fileExistsAtPath(sqliterDir)) {
            println("📌 Directory doesn't exist → creating")
            fm.createDirectoryAtPath(
                sqliterDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        } else {
            println("✔️ Directory exists already")
        }

        // ❗ Copy BEFORE opening the driver!
        println("📥 Writing database file BEFORE creating driver")
        val wrote = bytes.toNSData().writeToFile(sqliterDbPath, atomically = true)

        if (!wrote) error("❌ Failed to write DB file to: $sqliterDbPath")
        println("✔️ DB file written successfully")

        // Double-check existence
        val existsAfter = fm.fileExistsAtPath(sqliterDbPath)
        println("🔎 DB exists after write? $existsAfter")

        if (existsAfter) {
            val contents = fm.contentsOfDirectoryAtPath(sqliterDir, null)?.map { it.toString() }
            println("📄 Files in SQLiter dir: $contents")
        }

        println("🚀 NOW creating driver (explicit basePath via extendedConfig)")

        val driver = NativeSqliteDriver(
            schema = TallyDatabase.Schema,
            name = DB_FILE_NAME,
            onConfiguration = { config ->
                config.copy(
                    // Prevent SQLDelight from trying to create the schema on an already-populated DB
                    create = { /* no-op */ },
                    upgrade = { _, _, _ -> /* no-op */ },
                    // keep setting basePath so the driver opens the file you wrote
                    extendedConfig = config.extendedConfig.copy(
                        basePath = sqliterDir
                    )
                )
            }
        )

        println("✔️ Driver created successfully, database path = $sqliterDbPath")

        return driver
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(
        bytes = this@toNSData.refTo(0).getPointer(this),
        length = this@toNSData.size.toULong()
    )
}