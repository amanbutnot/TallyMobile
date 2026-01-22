package org.prime.easykarobar.data.expect

import app.cash.sqldelight.db.SqlDriver
import org.prime.easykarobar.TallyDatabase

expect class DriverFactory {
    fun createDriver(bytes: ByteArray): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory,byteArray: ByteArray): TallyDatabase {

    val driver = driverFactory.createDriver(byteArray)
    return TallyDatabase(driver)

}