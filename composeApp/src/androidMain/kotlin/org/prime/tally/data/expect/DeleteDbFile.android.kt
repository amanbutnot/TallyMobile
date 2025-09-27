package org.prime.tally.data.expect

import org.prime.tally.AppContextHolder
import org.prime.tally.data.utils.DB_FILE_NAME

actual fun deleteDbFile() {
    val context = AppContextHolder.appContext
    val dbName = DB_FILE_NAME
    val dbPath = context.getDatabasePath(dbName)

    // Always replace the old file
    if (dbPath.exists()) {
        dbPath.delete()
    }

}