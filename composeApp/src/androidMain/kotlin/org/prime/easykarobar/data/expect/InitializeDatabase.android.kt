package org.prime.easykarobar.data.expect

import org.prime.easykarobar.AppContextHolder
import org.prime.easykarobar.TallyDatabase

actual fun initializeDatabase(byteArray: ByteArray): TallyDatabase {
   val driverFactory = DriverFactory(AppContextHolder.appContext)
    val driver = driverFactory.createDriver(byteArray)
    return TallyDatabase(driver)
}