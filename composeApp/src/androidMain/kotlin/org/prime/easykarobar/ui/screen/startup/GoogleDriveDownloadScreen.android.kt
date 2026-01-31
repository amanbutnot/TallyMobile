// ReadDatabaseFile.android.kt
package org.prime.easykarobar.ui.screen.startup

import org.prime.easykarobar.AppContextHolder
import java.io.File

actual fun readDatabaseFile(filePath: String): ByteArray {
    return File(filePath).readBytes()
}

actual fun getAppDatabaseDirectory(): String {
    // Return app's files directory where extracted db will be placed temporarily
    return AppContextHolder.appContext.filesDir.absolutePath
}