package org.prime.tally.data.expect

import app.cash.sqldelight.db.SqlDriver
import org.tally.TallyDatabase

expect class DriverFactory {
    fun createDriver(bytes: ByteArray): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory,byteArray: ByteArray): TallyDatabase {

    val driver = driverFactory.createDriver(byteArray)
    return TallyDatabase(driver)

}