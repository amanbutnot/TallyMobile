package org.prime.easykarobar.data.expect

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.prime.easykarobar.TallyDatabase
import org.prime.easykarobar.data.utils.DB_FILE_NAME
import java.io.FileOutputStream

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(bytes: ByteArray): SqlDriver {
        val dbName = DB_FILE_NAME
        val dbPath = context.getDatabasePath(dbName)

        // Always replace the old file
        if (dbPath.exists()) {
            dbPath.delete()
        }

        // Ensure parent directory exists
        dbPath.parentFile?.mkdirs()

        // Write the new database file
        FileOutputStream(dbPath).use { output ->
            output.write(bytes)
        }

        val driver = AndroidSqliteDriver(
            schema = TallyDatabase.Schema,
            context = context,
            name = dbName,
            callback = object : AndroidSqliteDriver.Callback(TallyDatabase.Schema) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // No-op, file is pre-populated
                }
            }
        )

        // Manual check for columns after database is loaded
        try {
            driver.executeQuery(null, "PRAGMA table_info(Products)", { cursor ->
                val existingColumns = mutableSetOf<String>()
                while (cursor.next().value) {
                    cursor.getString(1)?.let { existingColumns.add(it) }
                }

                val columnsToAdd = listOf(
                    "N1" to "REAL",
                    "AltUnit" to "TEXT",
                    "ConFactor" to "REAL",
                    "ConType" to "REAL"
                )

                columnsToAdd.forEach { (name, type) ->
                    if (!existingColumns.contains(name)) {
                        println("⚠️ $name column missing in Products table, adding it...")
                        driver.execute(null, "ALTER TABLE Products ADD COLUMN $name $type", 0)
                    }
                }
                QueryResult.Unit
            }, 0)
        } catch (e: Exception) {
            println("❌ Error during manual migration: ${e.message}")
        }

        return driver
    }
}
