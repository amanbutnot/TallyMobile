package org.prime.easykarobar.data.expect

import org.prime.easykarobar.AppContextHolder
import org.prime.easykarobar.data.utils.DB_FILE_NAME
import java.io.File

actual fun deleteDbFile() {
    val context = AppContextHolder.appContext
    val dbName = DB_FILE_NAME

    val fileVariants = listOf(
        dbName,
        "$dbName-wal",
        "$dbName-shm",
        "$dbName-journal"
    )

    // List of all directories to check
    val directories = listOfNotNull(
        context.getDatabasePath(dbName).parentFile,
        context.filesDir,
        context.cacheDir,
        context.getExternalFilesDir(null)
    )

    directories.forEach { dir ->
        fileVariants.forEach { variant ->
            val file = File(dir, variant)
            if (file.exists()) {
                val success = file.delete()
                if (success) {
                    println("🗑️ Deleted: ${file.absolutePath}")
                } else {
                    println("❌ Failed to delete: ${file.absolutePath}")
                }
            }
        }
    }
}