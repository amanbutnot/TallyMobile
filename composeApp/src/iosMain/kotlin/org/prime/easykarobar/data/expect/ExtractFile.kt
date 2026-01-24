package org.prime.easykarobar.data.expect

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi

actual class ZipExtractor actual constructor() {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun extractSingle(zipBytes: ByteArray): ByteArray {
    }
}
