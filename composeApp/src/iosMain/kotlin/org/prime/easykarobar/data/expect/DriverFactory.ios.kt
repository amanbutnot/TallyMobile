package org.prime.easykarobar.data.expect

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import org.prime.easykarobar.TallyDatabase
import org.prime.easykarobar.data.utils.DB_FILE_NAME
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

            listOf("-wal", "-shm").forEach { suffix ->
                val journalPath = "$sqliterDbPath$suffix"
                if (fm.fileExistsAtPath(journalPath)) {
                    println("🧹 Deleting old journal file: $journalPath")
                    fm.removeItemAtPath(journalPath, null)
                }
            }
        }

        println("📥 Writing database file BEFORE creating driver")
        val wrote = bytes.toNSData().writeToFile(sqliterDbPath, atomically = true)

        if (!wrote) {
            error("❌ Failed to write DB file to: $sqliterDbPath")
        }

        println("✔️ DB file written successfully")

        val existsAfter = fm.fileExistsAtPath(sqliterDbPath)
        println("🔎 DB exists after write? $existsAfter")

        if (existsAfter) {
            val contents = fm.contentsOfDirectoryAtPath(sqliterDir, null)
                ?.map { it.toString() }

            println("📄 Files in SQLiter dir: $contents")
        }

        println("🚀 NOW creating driver")

        val driver = NativeSqliteDriver(
            schema = TallyDatabase.Schema,
            name = DB_FILE_NAME,
            onConfiguration = { config ->
                config.copy(
                    create = { /* no-op */ },
                    upgrade = { _, _, _ -> /* no-op */ },
                    extendedConfig = config.extendedConfig.copy(
                        basePath = sqliterDir
                    )
                )
            }
        )

        try {
            ensureColumns(
                driver = driver,
                tableName = "Vouchers_StockItems",
                columnsToAdd = listOf(
                    "D5" to "REAL",
                    "D6" to "REAL",
                    "D7" to "REAL",
                    "D8" to "REAL"
                )
            )

            ensureColumns(
                driver = driver,
                tableName = "Products",
                columnsToAdd = listOf(
                    "N1" to "REAL",
                    "AltUnit" to "TEXT",
                    "ConFactor" to "REAL",
                    "ConType" to "REAL"
                )
            )
        } catch (e: Exception) {
            println("❌ Error during manual migration: ${e.message}")
        }

        println("✔️ Driver created successfully, database path = $sqliterDbPath")

        return driver
    }

    private fun ensureColumns(
        driver: SqlDriver,
        tableName: String,
        columnsToAdd: List<Pair<String, String>>
    ) {
        driver.executeQuery(
            identifier = null,
            sql = "PRAGMA table_info($tableName)",
            mapper = { cursor ->
                val existingColumns = mutableSetOf<String>()

                while (cursor.next().value) {
                    cursor.getString(1)?.let { columnName ->
                        existingColumns.add(columnName)
                    }
                }

                columnsToAdd.forEach { (name, type) ->
                    if (!existingColumns.contains(name)) {
                        println("⚠️ $name column missing in $tableName table, adding it...")

                        driver.execute(
                            identifier = null,
                            sql = "ALTER TABLE $tableName ADD COLUMN $name $type",
                            parameters = 0
                        )
                    } else {
                        println("✔️ $name already exists in $tableName")
                    }
                }

                QueryResult.Unit
            },
            parameters = 0
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(
        bytes = this@toNSData.refTo(0).getPointer(this),
        length = this@toNSData.size.toULong()
    )
}