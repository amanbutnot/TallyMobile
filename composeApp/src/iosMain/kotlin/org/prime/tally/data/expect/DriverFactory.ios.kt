package org.prime.tally.data.expect

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.prime.tally.data.utils.DB_FILE_NAME
import org.tally.TallyDatabase

actual class DriverFactory {
    actual fun createDriver(bytes: ByteArray): SqlDriver {
        return NativeSqliteDriver(TallyDatabase.Schema, DB_FILE_NAME)
    }
}