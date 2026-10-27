package org.prime.easykarobar.data.expect

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToFile

@OptIn(BetaInteropApi::class)
actual suspend fun createExcel(
    fileName: String,
    headers: List<String>,
    rows: List<List<String>>
): String {
    return withContext(Dispatchers.Default) {
        val tempDir = NSTemporaryDirectory()
        val filePath = "$tempDir/$fileName.csv"
        val csvContent = buildString {
            appendLine(headers.joinToString(","))
            rows.forEach { row ->
                appendLine(row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
            }
        }
        val nsString = NSString.create(string = csvContent)
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding)
        data?.writeToFile(filePath, atomically = true)
        filePath
    }
}
