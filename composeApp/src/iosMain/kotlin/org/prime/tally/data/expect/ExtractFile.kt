package org.prime.tally.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy
import platform.zlib.Z_OK
import platform.zlib.z_stream

actual class ZipExtractor actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun extractSingle(zipBytes: ByteArray): ByteArray {
        val sourceData = zipBytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = zipBytes.size.toULong())
        }
        val zStream = z_stream()

        val status = platform.zlib.inflateInit2_(zStream.ptr, 15 + 32)
        if (status != Z_OK) {
            throw Exception("inflateInit2_ failed with status $status")
        }

        val result = mutableListOf<Byte>()
        val buffer = ByteArray(1024)

        try {
            sourceData.bytes?.let { bytes ->
                zStream.next_in = bytes.reinterpret()
                zStream.avail_in = sourceData.length.toUInt()

                while (zStream.avail_in > 0u) {
                    buffer.usePinned { pinned ->
                        zStream.next_out = pinned.addressOf(0)
                        zStream.avail_out = buffer.size.toUInt()
                        val inflateStatus = platform.zlib.inflate(zStream.ptr, platform.zlib.Z_NO_FLUSH)
                        if (inflateStatus != Z_OK && inflateStatus != platform.zlib.Z_STREAM_END) {
                            throw Exception("inflate failed with status $inflateStatus")
                        }
                        val have = buffer.size - zStream.avail_out.toInt()
                        result.addAll(buffer.take(have))
                    }
                }
            }
        } finally {
            platform.zlib.inflateEnd(zStream.ptr)
        }

        return result.toByteArray()
    }
}
