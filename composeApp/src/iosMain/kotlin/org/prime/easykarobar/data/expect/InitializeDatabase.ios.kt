package org.prime.easykarobar.data.expect

import org.prime.easykarobar.TallyDatabase

actual fun initializeDatabase(byteArray: ByteArray): TallyDatabase {
    val driver = DriverFactory().createDriver(byteArray)
    return TallyDatabase(driver)
}