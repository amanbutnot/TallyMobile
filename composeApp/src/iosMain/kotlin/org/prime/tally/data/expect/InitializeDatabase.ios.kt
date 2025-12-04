package org.prime.tally.data.expect

import org.tally.TallyDatabase

actual fun initializeDatabase(byteArray: ByteArray): TallyDatabase {
    val driver = DriverFactory().createDriver(byteArray)
    return TallyDatabase(driver)
}