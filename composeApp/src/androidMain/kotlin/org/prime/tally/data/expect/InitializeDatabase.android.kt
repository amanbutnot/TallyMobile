package org.prime.tally.data.expect

import org.prime.tally.AppContextHolder
import org.tally.TallyDatabase

actual fun initializeDatabase(byteArray: ByteArray): TallyDatabase {
   val driverFactory = DriverFactory(AppContextHolder.appContext)
    val driver = driverFactory.createDriver(byteArray)
    return TallyDatabase(driver)
}