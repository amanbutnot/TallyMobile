package org.prime.easykarobar.data.expect

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

actual class ZipExtractor actual constructor() {
    actual suspend fun extractSingle(zipBytes: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            zis.nextEntry?.let {
                zis.readBytes()
            } ?: throw Exception("No entry found in zip")
        }
    }
}
