package org.prime.tally.data.expect

expect class ZipExtractor() {
    suspend fun extractSingle(zipBytes: ByteArray): ByteArray
}
