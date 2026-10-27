package org.prime.easykarobar.data.expect

import org.prime.easykarobar.AppContextHolder
import org.prime.easykarobar.TallyDatabase
import org.prime.easykarobar.data.utils.DB_FILE_NAME
import java.io.File

actual fun initializeDatabase(byteArray: ByteArray): TallyDatabase {
   val driverFactory = DriverFactory(AppContextHolder.appContext)
    val driver = driverFactory.createDriver(byteArray)
    return TallyDatabase(driver)
}

actual fun initializeDatabaseFromFile(filePath: String): TallyDatabase {
    val context = AppContextHolder.appContext
    val dbName = DB_FILE_NAME
    val targetFile = context.getDatabasePath(dbName)

    if (targetFile.exists()) {
        targetFile.delete()
    }
    targetFile.parentFile?.mkdirs()

    val sourceFile = File(filePath)
    if (sourceFile.exists() && sourceFile.absolutePath != targetFile.absolutePath) {
        sourceFile.copyTo(targetFile, overwrite = true)
        sourceFile.delete()
    }

    val driverFactory = DriverFactory(context)
    val driver = driverFactory.createDriver()
    return TallyDatabase(driver)
}
