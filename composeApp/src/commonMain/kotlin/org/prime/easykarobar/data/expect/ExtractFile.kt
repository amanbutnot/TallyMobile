package org.prime.easykarobar.data.expect

expect class ZipExtractor() {
    suspend fun extractSingle(zipBytes: ByteArray): ByteArray
}
