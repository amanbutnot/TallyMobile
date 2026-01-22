package org.prime.easykarobar.data.expect

import org.prime.easykarobar.AppContextHolder
import org.prime.easykarobar.data.utils.DB_FILE_NAME

actual fun deleteDbFile() {
    val context = AppContextHolder.appContext
    val dbName = DB_FILE_NAME
    val dbPath = context.getDatabasePath(dbName)

    // Always replace the old file
    if (dbPath.exists()) {
        dbPath.delete()
    }

}