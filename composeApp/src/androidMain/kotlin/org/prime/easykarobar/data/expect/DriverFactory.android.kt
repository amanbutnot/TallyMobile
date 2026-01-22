package org.prime.easykarobar.data.expect

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
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

        return AndroidSqliteDriver(
            schema = TallyDatabase.Schema,
            context = context,
            name = dbName,
            callback = object : AndroidSqliteDriver.Callback(TallyDatabase.Schema) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // No-op, file is pre-populated
                }
            }
        )
    }
}
