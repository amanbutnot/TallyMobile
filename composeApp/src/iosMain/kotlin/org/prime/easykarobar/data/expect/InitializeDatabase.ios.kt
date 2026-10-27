package org.prime.easykarobar.data.expect

import org.prime.easykarobar.TallyDatabase
import org.prime.easykarobar.data.utils.DB_FILE_NAME
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun initializeDatabase(byteArray: ByteArray): TallyDatabase {
    val driver = DriverFactory().createDriver(byteArray)
    return TallyDatabase(driver)
}

actual fun initializeDatabaseFromFile(filePath: String): TallyDatabase {
    val appSupport = NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        true
    ).first() as String

    val bundleId = NSBundle.mainBundle.bundleIdentifier ?: "default"
    val sqliterDir = "$appSupport/$bundleId"
    val sqliterDbPath = "$sqliterDir/$DB_FILE_NAME"

    val fm = NSFileManager.defaultManager
    if (fm.fileExistsAtPath(sqliterDbPath)) {
        fm.removeItemAtPath(sqliterDbPath, error = null)
    }
    if (!fm.fileExistsAtPath(sqliterDir)) {
        fm.createDirectoryAtPath(sqliterDir, withIntermediateDirectories = true, attributes = null, error = null)
    }
    fm.copyItemAtPath(filePath, sqliterDbPath, error = null)
    fm.removeItemAtPath(filePath, error = null)

    val driver = DriverFactory().createDriver()
    return TallyDatabase(driver)
}
