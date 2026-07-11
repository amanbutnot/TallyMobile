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

        if (dbPath.exists()) {
            dbPath.delete()
        }

        dbPath.parentFile?.mkdirs()

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